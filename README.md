# Android part
Анисимов Тимур Юрьевич, ИКС-431

## Ссылки на репозитории
- Android-проект (этот репозиторий): `visprog`
- Бэкенд для ПАК: [https://github.com/dokbrawn/vis_backENDer.git](https://github.com/dokbrawn/vis_backENDer.git)

## Описание проекта
Репозиторий содержит Android-приложение по курсу «Визуальное программирование и человеко-машинное взаимодействие».

Главный экран (`MainActivity`) работает как hub и содержит переходы в модули:
- **Calculator**
- **Player**
- **Location**
- **Telephony**
- **ZMQ**

Пример из кода (переходы между Activity):
```kotlin
bGoToPlayerActivity.setOnClickListener {
    startActivity(Intent(this, MediaPlayerActivity::class.java))
}

bCalculator.setOnClickListener {
    startActivity(Intent(this, Calculator::class.java))
}
```

---

## Практики (названия)
1. **Основы ООП. «Ходячий»**
2. **Наследование**
3. **Интерфейсы**
4. **Разработка простейшего калькулятора**
4.5. **Рефакторинг. Разделение по Activities**
5. **Разработать MediaPlayer для воспроизведения музыки**
6. **Местоположение смартфона. Location**

---

## Что реализовано в текущем Android-проекте (с примерами из кода)

### 1) Hub-активность
- `MainActivity` — стартовая активность с кнопками перехода в остальные разделы.

```kotlin
override fun onResume() {
    super.onResume()
    bLocationExample.setOnClickListener {
        startActivity(Intent(this, LocationActivity::class.java))
    }
}
```

### 2) Калькулятор
- `Calculator` + `activity_calculator.xml`.
- Базовые операции: `+`, `-`, `*`, `/`, десятичная точка, `C`, `=`.
- Обработка деления на ноль с выводом `Err`.

```kotlin
private fun calculateResult() {
    val currentValue = tvResult.text.toString().toDoubleOrNull() ?: 0.0
    val result = when (currentOperator) {
        '+' -> lastValue + currentValue
        '-' -> lastValue - currentValue
        '*' -> lastValue * currentValue
        '/' -> if (currentValue != 0.0) lastValue / currentValue else Double.NaN
        else -> currentValue
    }
    tvResult.text = if (result.isNaN()) "Err" else result.toString()
}
```

### 3) MediaPlayer (каркас)
- `MediaPlayerActivity` + `activity_media_player.xml`.
- Реализованы кнопки `play/pause`, подготовлена логика для `MediaPlayer`.

```kotlin
findViewById<ImageButton>(R.id.btn_play_pause).setOnClickListener {
    if (isPlaying) pauseMusic() else playMusic()
}
```

```kotlin
private fun playMusic() {
    if (mediaPlayer == null) {
        trackTitle.text = "Воспроизведение... (ресурс не найден)"
    }
    mediaPlayer?.start()
}
```

### 4) Location
- `LocationActivity` + `LocationService`.
- Запрос runtime-permission для геолокации.
- Получение координат через `FusedLocationProviderClient`.
- Обновление данных на экране (`latitude`, `longitude`, `altitude`).
- Логирование локации в файл `location_log_service.json` в `filesDir`.

```kotlin
if (permissionsToRequest.isEmpty()) {
    startLocationService()
} else {
    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
}
```

```kotlin
val file = File(filesDir, "location_log_service.json")
val json = data.toJSONObject().toString() + "\n"
file.appendText(json)
```

### 5) Telephony / Mobile Data
- `TelephonyActivity` + `TelephonyService`.
- Сбор данных о сотах (LTE/NR/GSM), уровне сигнала, usage трафика.
- Поддержка фонового режима через foreground-service.
- Передача данных через ZMQ-сокеты (`PUSH/PULL`) на внешний адрес, который задается пользователем в UI.
- Локальная шина событий (`TelephonyDataBus`) для отображения данных в интерфейсе.

```kotlin
push = ctx.createSocket(SocketType.PUSH).apply { connect("tcp://$ip:5558") }
pull = ctx.createSocket(SocketType.PULL).apply { connect("tcp://$ip:5559") }
```

```kotlin
val nets = netMgr.getMobileNetworkData().networks.filter {
    (it.type == "LTE" && fLte) || (it.type == "NR" && fNr) || (it.type == "GSM")
}
```

### 6) ZMQ-демо
- `SocketsActivity` — тестовый экран с локальным REP-сокетом и клиентским REQ-сокетом.
- Демонстрация обмена сообщениями и ACK-ответов.

```kotlin
ctx.createSocket(SocketType.REP).use { s ->
    s.bind("tcp://*:2222")
    s.recvStr(ZMQ.DONTWAIT)?.let { msg ->
        s.send("ACK")
    }
}
```

### 7) Дополнительные экраны
- `LifeCycleActivity` — логирование жизненного цикла Activity.
- `ViewExamples` — примеры работы с `TextView`, `Button`, `SeekBar`.
- `ServiceActivity` + `BackgroundService` — пример фоновой задачи и обмена с Activity через broadcast.

```kotlin
Log.d(TAG, "onCreate")
Log.d(TAG, "onStart")
Log.d(TAG, "onResume")
```

---

## Структура проекта

```text
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/visprog/
│   │   ├── ui/           # Activity (экранные модули)
│   │   ├── services/     # Foreground/background сервисы
│   │   ├── network/      # Сетевые менеджеры и клиенты
│   │   ├── data/         # Data-классы
│   │   └── utils/        # Вспомогательные компоненты
│   └── res/
│       ├── layout/
│       ├── drawable/
│       ├── mipmap/
│       ├── values/
│       └── xml/
└── build.gradle.kts
```

---

## Ключевые файлы

### UI
- `ui/MainActivity.kt` — переходы между модулями.
- `ui/Calculator.kt` — логика калькулятора.
- `ui/MediaPlayerActivity.kt` — управление воспроизведением.
- `ui/LocationActivity.kt` — отображение и управление трекингом геопозиции.
- `ui/TelephonyActivity.kt` — мониторинг мобильной сети и управление сервисом.
- `ui/SocketsActivity.kt` — тестирование ZMQ-обмена.

### Services
- `services/LocationService.kt` — foreground-сервис геолокации с логированием в файл.
- `services/TelephonyService.kt` — сбор телеметрии и отправка на сервер через ZMQ.
- `services/BackgroundService.kt` — пример длительной фоновой задачи.

### Network
- `network/ApiClient.kt` — хранение/чтение адреса сервера.
- `network/MobileNetworkManager.kt` — сбор параметров сотовой сети.
- `network/NetworkTrafficManager.kt` — статистика мобильного трафика и аномалии.
- `network/ZmqManager.kt` — обертка для отправки данных через ZMQ.
- `network/WebSocketClient.kt` — клиент WebSocket (заготовка под альтернативный канал).

### Data + Utils
- `data/LocationData.kt`, `data/MobileNetworkData.kt` — модели данных + сериализация в JSON.
- `utils/TelephonyDataBus.kt` — SharedFlow-шина для событий между service/UI.

---

## Разрешения (Manifest)
Проект использует permissions:
- `INTERNET`
- `READ_PHONE_STATE`
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `FOREGROUND_SERVICE_DATA_SYNC`

Пример из `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Сборка и запуск
1. Открыть проект в Android Studio.
2. Sync Gradle.
3. Запустить на устройстве/эмуляторе (Android 10+ рекомендуется).
4. Для модулей `Location/Telephony` выдать runtime permissions.
5. Для `TelephonyService` указать IP сервера в `Telephony` экране.
