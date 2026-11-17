# ДОДАТОК В {app=Керівництво системного програміста|ДУІКТ.ТЦР-0008.045440.01.32}

## В.1 Загальні відомості {-}

Документ описує одноразове встановлення того, що працює зараз. Власного сервера й бази даних немає. Система складається з трьох частин:

- статичний застосунок (тека `app`) на Cloudflare Pages, проєкт `emobility`, гілка `main`, адреса https://emobility.pages.dev;
- функція Cloudflare Pages `/tg/webhook` (тека `functions`), яка відповідає на повідомлення бота кнопкою «Відкрити»;
- зовнішні ресурси: скрипт Telegram Mini Apps (telegram.org) і плитки карти OpenStreetMap.

Бот @emobility_ua_bot створюється в @BotFather. Дані користувачів на сервері не зберігаються.

## В.2 Вимоги до середовища {-}

- обліковий запис Cloudflare з доступом до Pages;
- Node.js версії 22 або новішої, з нього запускається `wrangler` 4.158.0 (`npx wrangler@4.158.0`);
- обліковий запис Telegram для створення бота в @BotFather;
- `curl` для перевірок, Python 3 для локального запуску (необов'язково).

## В.3 Встановлення {-}

Усі команди виконуються з теки `digital_tech/prototype` репозиторію, щоб до розгортання потрапила тека `functions`. Токен і секрет у документі позначено `<TOKEN>` і `<SECRET>`, справжні значення в репозиторій не записуються.

1. Створити бота: у @BotFather виконати `/newbot`, назва «єМобільність», ім'я користувача `emobility_ua_bot` (якщо зайняте, найближче вільне, воно закінчується на `bot`). Отриманий токен зберегти лише в менеджері паролів.
2. Увійти в Cloudflare: `npx wrangler@4.158.0 login`.
3. Створити проєкт: `npx wrangler@4.158.0 pages project create emobility --production-branch main`.
4. Розгорнути застосунок:

```
npx wrangler@4.158.0 pages deploy app --project-name emobility --branch main --commit-dirty=true
```

5. Записати секрети (значення вводяться в запит і нікуди не виводяться):

```
npx wrangler@4.158.0 pages secret put BOT_TOKEN --project-name emobility
npx wrangler@4.158.0 pages secret put WEBHOOK_SECRET --project-name emobility
```

Секрет вебхука це випадковий рядок із літер, цифр, `_` і `-`, наприклад `head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n'`.

6. Налаштувати бота викликами Bot API (замість `<TOKEN>` і `<SECRET>` підставити значення):

```
API=https://api.telegram.org/bot<TOKEN>
curl -X POST $API/setWebhook -H 'Content-Type: application/json' -d '{"url":"https://emobility.pages.dev/tg/webhook","secret_token":"<SECRET>","allowed_updates":["message"],"drop_pending_updates":true}'
curl -X POST $API/setChatMenuButton -H 'Content-Type: application/json' -d '{"menu_button":{"type":"web_app","text":"Відкрити","web_app":{"url":"https://emobility.pages.dev"}}}'
curl -X POST $API/setMyCommands -H 'Content-Type: application/json' -d '{"commands":[{"command":"start","description":"Відкрити єМобільність"}]}'
```

7. Розгорнути ще раз тією самою командою з кроку 4: нове розгортання підхоплює секрети.
8. У @BotFather відкрити `/mybots`, бота, «Bot Settings», «Configure Mini App», увімкнути й вказати адресу https://emobility.pages.dev. Тільки робочу адресу, ніколи адресу перегляду `<hash>.emobility.pages.dev`: Telegram вимикає методи Mini App на інших походженнях.

## В.4 Перевірка встановлення {-}

Після розгортання виконуються перевірки:

```
curl -sI https://emobility.pages.dev/ | grep -i -E "referrer-policy|cache-control"
curl -s -o /dev/null -w '%{http_code}\n' https://emobility.pages.dev/tg/webhook
curl -s -o /dev/null -w '%{http_code}\n' -X POST https://emobility.pages.dev/tg/webhook
```

Очікується `strict-origin-when-cross-origin` і `no-cache` (карта OpenStreetMap потребує Referer), потім 405 на GET і 401 на POST без секрету. Далі відкрити адресу в браузері у світлій і темній темі, надіслати боту будь-яке повідомлення, перевірити кнопку «Відкрити», посилання `https://t.me/emobility_ua_bot?startapp=s2` і запит геолокації. Стан вебхука показує `getWebhookInfo`.

## В.5 Повідомлення функції {-}

Функція вебхука відповідає:

- 200 і текст `ok`: запит із правильним секретним заголовком, навіть якщо тіло не вдалося розібрати (Telegram повторює запити після помилок);
- 401 і `unauthorized`: заголовка `X-Telegram-Bot-Api-Secret-Token` немає, він хибний або секрет `WEBHOOK_SECRET` не записано;
- 405 і `method not allowed`: запит GET.

Якщо бот не відповідає, частіше за все секрети записано після останнього розгортання, вебхук не встановлено або секрет у `setWebhook` не збігається із записаним.
