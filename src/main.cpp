#include <GL/glew.h>
#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>

#include <iostream>
#include <string>
#include <thread>
#include <mutex>
#include <atomic>
#include <chrono>
#include <windows.h>
#include <clocale>

#include <zmq.hpp>
#include <nlohmann/json.hpp>

#include "imgui.h"
#include "backends/imgui_impl_sdl2.h"
#include "backends/imgui_impl_opengl3.h"

using json = nlohmann::json;

struct LocationData {
    std::mutex mtx;
    double latitude = 0.0;
    double longitude = 0.0;
    double altitude = 0.0;
    long long timestamp = 0;
    bool has_data = false;
};

void run_server_thread(LocationData* shared_loc, std::atomic<bool>* should_stop) {
    const std::string endpoint = "tcp://0.0.0.0:5555";
    
    while (!should_stop->load()) {
        zmq::context_t context(1);
        zmq::socket_t socket(context, zmq::socket_type::rep);
        
        try {
            socket.bind(endpoint);
            std::cout << "[SERVER] Сервер запущен: " << endpoint << std::endl;
            
            while (!should_stop->load()) {
                zmq::message_t request;
                auto result = socket.recv(request, zmq::recv_flags::none);
                
                if (!result) {
                    if (should_stop->load()) break;
                    continue;
                }
                
                std::string msg_str = request.to_string();
                json received_json = json::parse(msg_str);
                
                {
                    std::lock_guard<std::mutex> lock(shared_loc->mtx);
                    shared_loc->latitude = received_json.value("latitude", 0.0);
                    shared_loc->longitude = received_json.value("longitude", 0.0);
                    shared_loc->altitude = received_json.value("altitude", 0.0);
                    shared_loc->timestamp = received_json.value("time", 0LL);
                    shared_loc->has_data = true;
                }
                
                socket.send(zmq::buffer("OK"), zmq::send_flags::none);
            }
        } catch (const zmq::error_t& e) {
            std::cerr << "[SERVER] Ошибка: " << e.what() << ". Переподключение через 2 сек..." << std::endl;
            std::this_thread::sleep_for(std::chrono::seconds(2));
            continue;
        }
        
        if (socket) {
            try { socket.close(); } catch (...) {}
        }
        if (!should_stop->load()) {
            std::this_thread::sleep_for(std::chrono::seconds(1));
        }
    }
}

void run_gui_thread(LocationData* shared_loc, std::atomic<bool>* should_stop) {
    if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_TIMER) != 0) {
        std::cerr << "[GUI] Failed to initialize SDL: " << SDL_GetError() << std::endl;
        return;
    }

    SDL_Window* window = SDL_CreateWindow(
        "Location Tracker",
        SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED,
        900, 500,
        SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE
    );
    if (!window) {
        std::cerr << "[GUI] Failed to create window: " << SDL_GetError() << std::endl;
        return;
    }

    SDL_GLContext gl_context = SDL_GL_CreateContext(window);
    SDL_GL_MakeCurrent(window, gl_context);
    SDL_GL_SetSwapInterval(1);

    if (glewInit() != GLEW_OK) {
        std::cerr << "[GUI] Failed to initialize GLEW" << std::endl;
        return;
    }

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
    io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;
    ImGui::StyleColorsDark();

    ImGui_ImplSDL2_InitForOpenGL(window, gl_context);
    ImGui_ImplOpenGL3_Init("#version 330");

    std::cout << "[GUI] Interface started" << std::endl;

    while (!should_stop->load()) {
        SDL_Event event;
        while (SDL_PollEvent(&event)) {
            ImGui_ImplSDL2_ProcessEvent(&event);
            if (event.type == SDL_QUIT) {
                should_stop->store(true);
            }
        }

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplSDL2_NewFrame();
        ImGui::NewFrame();

        {
            ImGui::Begin("Location Data");
            
            double lat, lon, alt;
            long long ts;
            bool has_data;
            {
                std::lock_guard<std::mutex> lock(shared_loc->mtx);
                lat = shared_loc->latitude;
                lon = shared_loc->longitude;
                alt = shared_loc->altitude;
                ts = shared_loc->timestamp;
                has_data = shared_loc->has_data;
            }

            if (has_data) {
                ImGui::Text("Status: Connected");
                ImGui::Separator();
                
                auto time_point = std::chrono::system_clock::time_point(std::chrono::milliseconds(ts));
                auto time_t_val = std::chrono::system_clock::to_time_t(time_point);
                struct tm time_info;
                localtime_s(&time_info, &time_t_val);
                
                char time_buf[64];
                std::strftime(time_buf, sizeof(time_buf), "%Y-%m-%d %H:%M:%S", &time_info);
                
                ImGui::Text("Time: %s", time_buf);
                ImGui::Text("Latitude:  %.6f°", lat);
                ImGui::Text("Longitude: %.6f°", lon);
                ImGui::Text("Altitude:  %.2f m", alt);
                
                ImGui::Separator();
                ImGui::Text("Coordinates for map:");
                ImGui::InputDouble("##lat", &lat, 0.0, 0.0, "%.6f", ImGuiInputTextFlags_ReadOnly);
                ImGui::InputDouble("##lon", &lon, 0.0, 0.0, "%.6f", ImGuiInputTextFlags_ReadOnly);
            } else {
                ImGui::Text("Status: Waiting for data...");
                ImGui::Text("Connect Android app to server");
            }
            
            ImGui::End();
        }

        ImGui::Render();
        glClearColor(0.15f, 0.15f, 0.18f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        SDL_GL_SwapWindow(window);
        
        std::this_thread::sleep_for(std::chrono::milliseconds(16));
    }

    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplSDL2_Shutdown();
    ImGui::DestroyContext();
    SDL_GL_DeleteContext(gl_context);
    SDL_DestroyWindow(window);
    SDL_Quit();
}

int main() {
    SetConsoleOutputCP(CP_UTF8);
    setlocale(LC_ALL, "ru_RU.UTF-8");
    
    static LocationData location_info;
    static std::atomic<bool> should_stop{false};

    std::cout << "[MAIN] Запуск потоков..." << std::endl;

    std::thread server_thread(run_server_thread, &location_info, &should_stop);
    std::thread gui_thread(run_gui_thread, &location_info, &should_stop);

    gui_thread.join();
    should_stop.store(true);
    
    if (server_thread.joinable()) {
        server_thread.join();
    }

    std::cout << "[MAIN] Завершение работы" << std::endl;
    return 0;
}