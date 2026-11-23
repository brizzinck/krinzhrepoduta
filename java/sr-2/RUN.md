# СР 2: запуск і показ

Потрібен Docker і доступ до інтернету (зовнішній сервіс DummyJSON). Maven і застосунок працюють у контейнерах на образі `maven:3.9-eclipse-temurin-21`. Ручних кроків немає: `make show` працює з чистого стану (збірка, запуск контейнера, база й очищення робляться самі, попередньо запущений застосунок не потрібен; зайнятий порт дасть повідомлення). У кінці `show` зупиняє й видаляє контейнер, порт звільняється. Усі команди з теки `java/sr-2` (шлях від кореня репозиторію). Порт задає змінна `PORT` (за замовчуванням 8081), ім'я контейнера `NAME` (за замовчуванням `sr2-app`): `make show PORT=8090`.

## Пайплайн

```sh
cd java/sr-2
make build      # зібрати jar (зупиняє контейнер, якщо він працює)
make up         # лише запустити застосунок (збере jar, якщо його немає)
make info       # старі ендпоїнти з СР 1
make import     # POST /import: камери з DummyJSON у H2
make saved      # GET /saved і GET /saved/1
make delete     # DELETE /saved/1 і GET /saved/1
make reimport   # повторний імпорт, дублів немає
make restart    # перезапуск, дані лишились у файлі
make consoles   # Swagger UI і H2 Console
make down       # зупинити й видалити контейнер
make clean      # те саме й прибрати demo/target
```

`make show` виконує все по черзі й видаляє контейнер, `make help` показує цілі. Кожну ціль можна запускати окремо з чистого стану: вона сама збере jar (якщо його немає) і запустить застосунок, а залежні кроки виконає перед собою (`make delete` спершу виконає `import`, `make restart` усю послідовність до себе). Контейнер лишається працювати після `up`, `info`, `import`, `saved`, `delete`, `reimport`, `restart` і `consoles`, доки не виконано `make down` (його також зупиняють `make build`, `make clean` і кінець `make show`). База лежить у `demo/target/data` і щоразу створюється наново, файли репозиторію не змінюються. Застосунок працює в мережі хоста, бо H2 Console приймає лише локальні з'єднання.

## Кроки

- `make build`: `../../tools/mvn.sh clean package` у `demo/`, результат `demo/target/demo-0.0.1-SNAPSHOT.jar`.
- `make up`: якщо `demo/target/demo-0.0.1-SNAPSHOT.jar` немає, спершу збирає його; потім контейнер з `java -jar` і `--spring.datasource.url=jdbc:h2:file:./target/data/cameradb`, очікування `/v3/api-docs`.
- `make info`, `make import`, `make saved`, `make delete`, `make reimport`: запити `curl` до `CameraController`.
- `make restart`: `docker stop`, новий контейнер на тій самій базі.
- `make consoles`: відповіді `/swagger-ui/index.html` і `/h2-console/`.

## Що показати

1. `make build`: `BUILD SUCCESS`, `Compiling 10 source files`.
2. `make info`: `/info` дає `HTTP/1.1 200`, `/snapshot` дає `HTTP/1.1 200`, `Content-Type: image/jpeg`, `Content-Length: 106972`. Старі ендпоїнти працюють.
3. `make import`: `HTTP 200` і чотири камери, `location` розкладено на `room` і `floor`, `device_type` став `deviceType`.
4. `make saved`: `"id":1 "id":2 "id":3 "id":4`, потім `GET /saved/1` дає `HTTP 200`.
5. `make delete`: `DELETE` дає `HTTP 204`, наступний `GET /saved/1` дає `HTTP 404`.
6. `make reimport`: `HTTP 200`, у базі знову `"id":1 "id":2 "id":3 "id":4` без дублів. Журнал Hibernate: `select: 4, insert: 1`, тобто `saveAll` шукає кожну камеру за `id`, а вставляє лише видалену.
7. `make restart`: після перезапуску без імпорту `GET /saved` знову дає чотири `id`. База лежить у файлі.
8. `make consoles`: `HTTP 200` для Swagger UI і H2 Console. У браузері H2 Console відкривається за `http://localhost:8081/h2-console/`, адреса бази `jdbc:h2:file:./target/data/cameradb`, користувач `sa`, пароль порожній, запит `SELECT * FROM CAMERAS`.

## Звідки що береться

- `demo/pom.xml`: Spring Boot, OpenFeign, springdoc, Spring Data JPA, H2 зі scope `runtime`.
- `demo/src/main/java/com/example/demo`: `entity/CameraEntity`, `repository/CameraRepository`, `service/CameraService` (`importCameras`, читання, видалення), `controller/CameraController`, `dto/CameraItemDto` і `CameraListDto`, `client/CameraClient`.
- `demo/src/main/resources/application.properties`: H2, JPA, шлях списку камер, таймаути Feign. Порт і адресу бази `make` перевизначає аргументами запуску.
- `demo/results/`: логи прогону, показаного у звіті; `demo/screenshots/`: знімки для звіту.
