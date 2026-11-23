# СР 1: запуск і показ

Потрібні Docker (образ `maven:3.9-eclipse-temurin-21`, залежності Maven з тома `duikt-m2`), `curl` і доступ до інтернету: застосунок звертається до `https://dummyjson.com`. Усі команди з теки `java/sr-1` репозиторію. Збірка й файли знімка пишуться в `demo/target/`, файли репозиторію не змінюються. Застосунок стає на порт `PORT` (типово 8081), якщо він зайнятий: `make show PORT=8082`.

## Пайплайн

```sh
cd java/sr-1   # тека роботи відносно кореня репозиторію
make build      # mvn clean package у demo
make up         # застосунок у docker на порту 8081
make info       # GET /info
make snapshot   # POST /snapshot, Accept: image/jpeg
make notjson    # POST /snapshot, Accept: application/json
make logs       # логи CameraService
make down       # зупинити застосунок
make stage1     # етап 1: JSON із Base64, окрема копія проєкту
```

Кожна ціль працює й окремо: якщо застосунок не запущено, `info`, `snapshot`, `notjson` і `logs` самі збирають проєкт і запускають його (контейнер лишається, поки не виконано `make down`). `make show` виконує все по черзі й видаляє контейнер, `make help` показує цілі, `make clean` зупиняє контейнер і видаляє `demo/target/`. Swagger UI відкривається на `http://localhost:8081/swagger-ui/index.html`, поки працює `make up`.

## Кроки

- `build`: `../../tools/mvn.sh clean package` у `demo/`, шість класів у пакетах `client`, `controller`, `dto`, `service`.
- `up`: контейнер `sr1-app` (`NAME`) запускає `java -jar target/demo-0.0.1-SNAPSHOT.jar`, порт 8080 контейнера відкрито як `PORT`.
- `info`: `curl` до `GET /api/v1/camera/info`, `CameraController` через `CameraService` і Feign-клієнт `CameraClient` бере дані з DummyJSON.
- `snapshot`: `POST /api/v1/camera/snapshot` з `Accept: image/jpeg`. Контролер декодує поле `image` (Base64) в байти. Файл лягає в `demo/target/snapshot.jpg`, його хеш порівнюється з `demo/results/snapshot.jpg`.
- `notjson`: той самий запит з `Accept: application/json`.
- `logs`: рядки `CameraService` з `docker logs`.
- `stage1`: копія проєкту в `demo/target/stage1` із контролером `demo/stages/CameraController.stage1.java`, який повертає `SnapshotDto`. Довжину Base64 і розмір після декодування рахує `demo/check_image.sh`.

## Що показати

1. `make build`: `Compiling 6 source files` і `BUILD SUCCESS`.
2. `make info`: `HTTP/1.1 200`, `Content-Type: application/json` і
   `{"id":123,"device_type":"camera","status":"online","features":["motion_detection","night_vision","audio"],"location":{"room":"office","floor":2}}`.
3. `make snapshot`: `HTTP/1.1 200`, `Content-Type: image/jpeg`, `Content-Length: 106972`, `JPEG image data, 900x600`. Обидва SHA-256 однакові: `ba6a02acbdac0f745f7bbe7ff52529aa87b727b8454ec3370a901a78edfd67c6`. Файл відкривається як зображення.
4. `make notjson`: `HTTP 406`, бо ендпоїнт оголошує лише `image/jpeg`.
5. `make logs`: `Getting camera info`, `Camera info received: CameraInfoDto[id=123, ...`, `Capturing snapshot`, `Snapshot received`. Повідомлення йдуть до виклику клієнта й після відповіді.
6. `make stage1`: `Content-Type: application/json`, `довжина поля image: 142632 символи`, `після декодування: 106972 байти, початок: ffd8ff`. На етапі 1 відповідь це JSON із Base64, у фіналі те саме зображення віддається байтами. Base64 на третину більший за двійкові дані: 142632 проти 106972.

## Звідки що береться

- `demo/src/main/java/com/example/demo/`: `client`, `controller`, `dto`, `service`, `DemoApplication`.
- `demo/pom.xml`: Spring Boot 3.3.5, Spring Cloud 2023.0.3, springdoc, OpenFeign.
- `demo/src/main/resources/application.properties`: назва застосунку й порт 8080.
- `demo/results/snapshot.jpg`: еталонний файл знімка для порівняння хешу.
- `../../tools/*.sh`: Maven у docker.
