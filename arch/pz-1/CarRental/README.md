# CarRental: прокат автомобілів (Clean Architecture та DDD)

ASP.NET Core API для прокату автомобілів: автопарк, орендарі, оренди. Проєкт будується поетапно на практичних заняттях з «Архітектури ПЗ»; цей знімок відповідає практичному заняттю №1**.

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

Усі правила живуть у Domain (перевірки без БД і HTTP).

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
| Application | `CarRental.Application` | сценарії (`CarService`, `RenterService`, `RentalService`), контракти (`ICarStore`, `IRentalStore`, `IFleetGateway`, ...), DTO |
| Infrastructure | `CarRental.Infrastructure` | EF Core + SQLite, реалізації контрактів, повідомлення й аналітика |
| WebApi | `CarRental.WebApi` | контролери, обробка помилок, точка складання (`Program.cs`) |

```text
Application    залежить від Domain
Infrastructure залежить від Application (Domain доступний транзитивно)
WebApi         залежить від Application і Infrastructure
Domain         без залежностей
```

Контролери тонкі: приймають HTTP, викликають сервіс, повертають результат. Бізнес-правил немає ні в контролерах, ні в сховищах.

## Запуск (усе в docker, без встановлення .NET)

```sh
./serve.sh start      # API на http://127.0.0.1:5080, БД у ./.data
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
