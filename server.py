import zmq
import os
import time

def run_server():
    context = zmq.Context()
    socket = context.socket(zmq.REP)
    
    
    socket.bind("tcp://*:5555")

    log_file = "android_data.log"
    
    
    if os.path.exists(log_file):
        os.remove(log_file)
        
    packet_counter = 0

    print("--- ZMQ СЕРВЕР ЗАПУЩЕН ---")
    print("Порт: 5555 | Ожидание данных...")

    try:
        while True:
            
            message = socket.recv_string()
            packet_counter += 1
            
            current_time = time.strftime("%H:%M:%S")
            
            
            with open(log_file, "a", encoding="utf-8") as f:
                f.write(f"Пакет #{packet_counter} | {message} | {current_time}\n")
            
            
            print(f"[{current_time}] Получено сообщение #{packet_counter}: {message}")

            
            socket.send_string("Hello from Server!")

    except KeyboardInterrupt:
        print("\nСервер остановлен.")
        
        if os.path.exists(log_file):
            print("\n--- ИТОГОВЫЙ ЛОГ ИЗ ФАЙЛА ---")
            with open(log_file, "r", encoding="utf-8") as f:
                print(f.read())
    finally:
        socket.close()
        context.term()

if __name__ == "__main__":
    run_server()