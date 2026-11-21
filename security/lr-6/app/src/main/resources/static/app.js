// Токен лише в змінній сторінки: після закриття вкладки він зникає, і його немає в localStorage,
// де його легко забрав би XSS-скрипт цього сайту.
let accessToken = null;
let expiresAt = 0;
let timer = null;

const form = document.getElementById('login-form');
const message = document.getElementById('message');
const session = document.getElementById('session');
const countdown = document.getElementById('countdown');
const profileOut = document.getElementById('profile-out');

function show(text, cls) {
  message.textContent = text;
  message.className = cls || '';
}

function tick() {
  const left = Math.max(0, Math.round((expiresAt - Date.now()) / 1000));
  countdown.textContent = left > 0 ? 'Токен дійсний ще ' + left + ' с' : 'Токен прострочено';
  if (left === 0 && timer) {
    clearInterval(timer);
    timer = null;
  }
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  profileOut.textContent = '';
  const body = JSON.stringify({
    login: document.getElementById('login').value,
    password: document.getElementById('password').value,
  });
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body,
  });
  if (!res.ok) {
    accessToken = null;
    session.hidden = true;
    show(res.status === 401 ? 'Неправильний логін або пароль' : 'Некоректні дані форми', 'err');
    return;
  }
  const data = await res.json();
  accessToken = data.accessToken;
  expiresAt = Date.now() + data.expiresIn * 1000;
  document.getElementById('password').value = '';
  show('Вхід виконано', 'ok');
  session.hidden = false;
  if (timer) clearInterval(timer);
  tick();
  timer = setInterval(tick, 1000);
});

document.getElementById('profile-btn').addEventListener('click', async () => {
  const res = await fetch('/api/profile', {
    headers: accessToken ? { Authorization: 'Bearer ' + accessToken } : {},
  });
  const data = await res.json().catch(() => ({}));   // у відповіді 401 від контролера тіла немає
  if (res.ok) {
    profileOut.textContent = JSON.stringify(data, null, 2);
    show('Профіль отримано (HTTP ' + res.status + ')', 'ok');
  } else {
    profileOut.textContent = JSON.stringify(data, null, 2);
    show('Доступ заборонено (HTTP ' + res.status + ')' + (data.reason ? ': ' + data.reason : ''), 'err');
  }
});
