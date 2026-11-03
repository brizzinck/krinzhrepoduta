# CarRental: прокат автомобілів (Clean Architecture та DDD)

ASP.NET Core API для прокату автомобілів: автопарк, орендарі, оренди. Проєкт будується поетапно на практичних заняттях з «Архітектури ПЗ»; цей знімок відповідає **практичному заняттю №5** (Repository та Unit of Work).

## Мова домену (Ubiquitous Language)

| Термін | Значення |
|---|---|
| Автомобіль (Car) | одиниця автопарку з номером, моделлю, класом, добовою ставкою, пробігом і станом |
| Орендар (Renter) | людина з посвідченням водія, яка може брати автомобілі в оренду |
| Оренда (Rental) | використання одного автомобіля одним орендарем протягом періоду |
| Період оренди | проміжок [початок, повернення): день повернення не зайнятий, у цей день можна видати авто знову |
| Ставка | вартість доби в гривнях; у оренді фіксується на момент оформлення |
| Пробіг | показник одометра в кілометрах; не зменшується |

## Bounded context

Система поділена на два контексти. Спільне ядро (Shared Kernel) лише `Money`, `CarClass`, `DomainRuleException`.

| Контекст | Відповідає за | Модель |
|---|---|---|
| Fleet (автопарк) | які автомобілі є, їхній стан, ставка, пробіг | `Car` (корінь агрегату), `LicensePlate` |
| Rentals (оренда) | хто, що, коли і за скільки орендує | `Rental` (корінь), `Renter` (окремий корінь агрегату), `RentalPeriod`, `DriverLicense`, `RentableCar` |

Один об'єкт реального світу в різних контекстах моделюється по-різному: у Fleet це `Car` з номером, моделлю й станом, у Rentals це `RentableCar` (Id, ставка, клас, пробіг, чи можна орендувати). Rentals не бачить `Car` і не посилається на його внутрішній стан, а отримує потрібне через порт `IFleetGateway` (інтерфейс належить Application, реалізація `FleetGateway` лежить в Infrastructure). Агрегати посилаються один на одного лише за `Guid` (`Rental.CarId`, `Rental.RenterId`).

## Бізнес-правила

Правила 1–11 живуть у Domain (перевірки без БД і HTTP). Унікальність номера (12) потребує даних про всі авто, тому її перевіряють `CarService` і унікальний індекс БД.

| № | Правило | Де |
|---|---|---|
| 1 | Номер авто: формат `AA1234BB`, нормалізація Trim/ToUpper | `LicensePlate` |
| 2 | Ставка більша за 0, пробіг не від'ємний | `Car.Register` |
| 3 | Пробіг не зменшується | `Car.RecordMileage` |
| 4 | Авто на обслуговуванні не здається | `Car.IsRentable`, `Rental.Open` |
| 5 | Орендарю не менше 21 року на дату початку | `Renter.EnsureCanRent` |
| 6 | Посвідчення діє до кінця оренди | `Renter.EnsureCanRent` |
| 7 | Оренда від 1 до 30 днів | `RentalPeriod` |
| 8 | Початок оренди не в минулому | `Rental.Open` |
| 9 | Немає перетину з активними орендами авто | `Rental.Open` (дані надає Application) |
| 10 | Завершити можна лише активну оренду, кінцевий пробіг не менший за початковий | `Rental.Complete` |
| 11 | Скасувати можна лише активну оренду до дати початку | `Rental.Cancel` |
| 12 | Унікальність номера авто | `CarService` + унікальний індекс БД |

## Шари й залежності

| Шар | Проєкт | Відповідальність |
|---|---|---|
| Domain | `CarRental.Domain` | сутності, Value Objects, правила; без посилань на інші проєкти й пакети |
| Application | `CarRental.Application` | сценарії (`CarService`, `RenterService`, `*Handler`), контракти (`IRepository<T>`, `IUnitOfWork`, `ICarRepository`, `IRentalRepository`, `IFleetGateway`, `IClock`, ...), DTO, реєстрація `AddApplication()` |
| Infrastructure | `CarRental.Infrastructure` | EF Core + SQLite, реалізації контрактів, шина подій, годинник, повідомлення й аналітика, реєстрація `AddInfrastructure()` |
| WebApi | `CarRental.WebApi` | контролери, обробка помилок, точка складання (`Program.cs`: виклики `AddApplication()` і `AddInfrastructure()`) |

```text
Application    залежить від Domain (+ пакет абстракцій DI)
Infrastructure залежить від Application (Domain доступний транзитивно)
WebApi         залежить від Application і Infrastructure
Domain         без залежностей
```

Контролери тонкі: приймають HTTP, викликають сервіс, повертають результат. Бізнес-правил немає ні в контролерах, ні в сховищах.

## Події (Event-Driven Design)

Сценарій оренди після збереження публікує факт через `IEventBus` і не знає, хто на нього реагує.

| Подія (минулий час, мінімум даних) | Обробник | Реакція |
|---|---|---|
| `RentalOpened(RentalId, CarId, RenterId, StartDate, EndDate, Total)` | `NotifyRenterOnRentalOpened` | «лист» орендарю (Rentals) |
| | `TrackRentalOnRentalOpened` | лічильники аналітики |
| | `AuditLogHandler` | рядок у журналі аудиту |
| `RentalCompleted(RentalId, CarId, EndMileage)` | `UpdateCarMileageOnRentalCompleted` | **Fleet** оновлює пробіг авто |
| | `AuditLogHandler` | рядок у журналі аудиту |
| `RentalCancelled(RentalId, CarId)` | `AuditLogHandler` | рядок у журналі аудиту |

`InProcessEventBus` (Infrastructure, Singleton) на кожну публікацію створює власну область контейнера й послідовно викликає всі зареєстровані `IEventHandler<T>` у порядку реєстрації в `AddApplication()`. Події не виконують бізнес-логіку: перевірка доступності авто лишається синхронною частиною `Rental.Open`. Журнал аудиту пишеться у файл `CarRental:AuditLogPath` (у docker: `.data/audit.log`).

Обмеження: подія та її обробники працюють без транзакції й черги (немає гарантій доставки); виняток в обробнику повертається клієнтові як помилка вже після збереження оренди.

## Dependency Injection (ПЗ 4)

Усі залежності створює контейнер; `Program.cs` (точка складання) викликає `AddApplication()` та `AddInfrastructure()` і вмикає `ValidateScopes` та `ValidateOnBuild`.

| Цикл | Компоненти |
|---|---|
| Singleton | `IClock`, `IAnalyticsService`, `IAuditLog`, `IEventBus` |
| Scoped | `CarRentalDbContext`, `IUnitOfWork`, `ICarRepository`, `IRenterRepository`, `IRentalRepository`/`IRentalReader`, `IFleetGateway`, сценарії (`*Handler`, `CarService`, `RenterService`, `RentalQueryService`), обробники подій |
| Transient | `IPricingRule`, `IPricingService`, `IRentalMapper`, `INotificationService` |

Правила: конструкторна ін'єкція, без `static` і service locator у бізнес-коді (контейнер напряму використовує лише `InProcessEventBus`), час через `IClock`. У режимі Development доступний `GET /api/_diag/lifetimes` (ідентифікатори екземплярів для демонстрації життєвих циклів).

## SOLID-рефакторинг (ПЗ 3)

Поведінка API не змінена; змінилась структура Application.

| Принцип | Що зроблено |
|---|---|
| SRP | `RentalService` розділено на `OpenRentalHandler`, `CompleteRentalHandler`, `CancelRentalHandler`, `RentalQueryService`; тариф винесено в `PricingService`; перетворення на DTO в `RentalMapper` |
| OCP | тариф складається з правил `IPricingRule` (`ClassMultiplierRule`, `LongRentalDiscountRule`); нове правило `WeekendSurchargeRule` додано окремим файлом і реєстрацією (вмикається `Pricing:WeekendSurcharge=true`) |
| DIP | журнал аудиту: `AuditLogHandler` залежить від `IAuditLog`, файл пише `FileAuditLog` в Infrastructure; сценарії залежать від `IPricingService` |
| ISP | `IRentalRepository` розділено: `IRentalReader` (читання) і `IRentalRepository : IRepository<Rental>, IRentalReader` (запис); `RentalQueryService` бачить лише читання |
| LSP | підтипи (`Car`, `Rental`, `Renter` як `Entity`) підставні: перевизначення `Describe()` не змінює контракт |

## Запуск (усе в docker, без встановлення .NET)

```sh
./serve.sh start      # API на http://127.0.0.1:5080, БД у ./.data
ASPNETCORE_ENVIRONMENT=Development ./serve.sh start   # з діагностикою життєвих циклів
./demo.sh             # сценарії: успіх, 409, 400, 404
./serve.sh logs       # журнал контейнера
./serve.sh restart    # перезапуск; дані зберігаються
./serve.sh stop
../../../tools/dotnet.sh test   # з теки CarRental; потрібен tools/dotnet.sh репозиторію
```

## Маршрути

| Метод і адреса | Призначення |
|---|---|
| `GET /api/cars`, `GET /api/cars/{id}` | автопарк |
| `POST /api/cars` | реєстрація авто `{plate, model, class, dailyRate, mileage}` |
| `POST /api/cars/{id}/maintenance` | відправити на обслуговування |
| `POST /api/renters`, `GET /api/renters/{id}` | орендарі |
| `POST /api/rentals` | відкрити оренду `{carId, renterId, start, end}` |
| `GET /api/rentals/{id}`, `GET /api/rentals?renterId=` | перегляд |
| `POST /api/rentals/{id}/complete` | завершити `{endMileage}` |
| `POST /api/rentals/{id}/cancel` | скасувати |

Помилки: правило порушено, 400; не знайдено, 404; перетин оренд, дубль номера або одночасна зміна, 409.
