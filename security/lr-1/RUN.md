# ЛР 1: запуск і показ

Потрібен Docker. Усі команди з теки `security/lr-1`. Скрипти Python виконуються в контейнері `python:3.12-slim`, Threat Dragon у `owasp/threat-dragon:v2.2.0`. Результати пишуться лише в `target/`, файли репозиторію `make` не змінює.

Інтерфейс Threat Dragon інтерактивний: модель у ньому будувалася й зберігалася вручну (кнопка Save), знімки зроблено з живого інтерфейсу. Тому `make` показує те, що можна перевірити локально: збережену модель, її повторну генерацію, таблиці звіту й сам запущений Threat Dragon. Малювати діаграму заново з терміналу не потрібно.

## Пайплайн

```sh
cd security/lr-1   # шлях відносно кореня репозиторію
make check     # склад моделі й підсумки загроз проти чисел зі звіту
make rebuild   # модель для імпорту, згенерована з rental_model.py, проти файлу в репозиторії
make tables    # threats.md і architecture.md, згенеровані з JSON, проти файлів у model/
make figures   # рисунки звіту й усі знімки інтерфейсу
make up        # Threat Dragon на http://localhost:3000 (PORT=3001 make up для іншого порту)
make down      # зупинити Threat Dragon
make open      # відкрити report.pdf і схему DFD
```

`make show` виконує все по черзі (крім `open`) і в кінці зупиняє контейнер, `make help` показує цілі, `make clean` видаляє `target/` і контейнер.

## Кроки

- `make check`: `threat-dragon/check_model.py` читає `model/rental-threat-model.json` (збережено кнопкою Save), рахує елементи, межі, потоки, записи загроз, перевіряє дозволені Threat Dragon типи STRIDE для кожного виду елемента і порівнює підсумки з числами розділу 3 звіту.
- `make rebuild`: `threat-dragon/build_model.py` будує JSON із даних `threat-dragon/rental_model.py` у `target/` і порівнює (`diff`) з `threat-dragon/rental-threat-model.input.json`, який імпортувався в Threat Dragon.
- `make tables`: `threat-dragon/td2md.py` читає збережену модель і пише `threats.md`, `architecture.md` у `target/model/`; `diff` з тими, що лежать у `model/`.
- `make figures`: три рисунки звіту (`img/dfd.png`, `img/td05_properties_p3_crop.png`, `img/td08_report_summary.png`) і решта знімків Threat Dragon з `img/`.
- `make up`: `threat-dragon/run.sh start` (при першому запуску створює `threat-dragon/td.env` з випадковими секретами; файл не потрапляє в репозиторій), чекає відповіді HTTP 200. Далі в браузері: Login to Local Session, Open Existing Model, вставити вміст `model/rental-threat-model.json`.

## Що показати

1. `make check`: склад діаграми `зовнішні учасники (EI): 5`, `процеси (P): 6`, `сховища (DS): 4`, `межі довіри: 6`, `потоки (F): 18`; `записів за елементами (рядків у Threat Dragon): 55`; `записи Open / Mitigated / N/A: 37 / 14 / 4`; `різних загроз T: 29`; `відхилених FP: 4`; `за рівнем High / Medium / Low: 11 / 17 / 1`; `за статусом Open / Mitigated: 21 / 8`; далі 11 загроз високого рівня (T01, T02, T04, T05, T08, T11, T14, T15, T16, T18, T28) і рядок `Усі числа збігаються зі звітом.`
2. `make rebuild`: `готово: target/rental-threat-model.input.json (39 клітинок, 55 записів загроз)` і `Збігається з threat-dragon/rental-threat-model.input.json.`
3. `make tables`: `model/threats.md збігається.` і `model/architecture.md збігається.`, `Рядки реєстру T01-T29 і FP1-FP4: 33` (29 загроз і 4 відхилені).
4. `make figures`: `Рисунок 1: img/dfd.png (290904 байт)`, `Рисунок 2: img/td05_properties_p3_crop.png (93416 байт)`, `Рисунок 3: img/td08_report_summary.png (48614 байт)`, далі список усіх знімків `img/`.
5. `make up`: `HTTP 200, контейнер td_lr1, адреса http://localhost:3000`; у браузері відкривається Threat Dragon v2.2.0 (знімки: `img/td01_home.png`, `img/td02_dashboard.png`).

## Звідки що береться

- `model/rental-threat-model.json`: модель, збережена з інтерфейсу Threat Dragon (кнопка Save); `model/threats.md` і `model/architecture.md` генеруються з неї.
- `threat-dragon/rental_model.py`: дані моделі (елементи, межі, потоки, загрози, оцінки L і I); `build_model.py` перетворює їх на JSON для імпорту.
- `img/`: знімки живого інтерфейсу Threat Dragon; діаграма `img/dfd.png` знята з полотна.
- Онлайн нічого не потрібне, крім першого завантаження образів Docker. Файл `.tm7` не створюється: цей формат є лише в Windows-інструменті, Threat Dragon зберігає JSON.
