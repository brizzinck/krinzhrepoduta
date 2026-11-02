// Клієнтська валідація форми реєстрації. Лише для зручності користувача:
// той самий набір правил повторно перевіряється на сервері, оскільки цей скрипт легко обійти.
(function () {
  'use strict';

  var LOGIN = /^[A-Za-z0-9_.-]+$/;
  var NAME = /^\p{L}+$/u;
  var EMAIL = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+$/;
  var PHONE = /^\+?[0-9]{10,15}$/;

  // Кожне правило повертає текст помилки або порожній рядок, якщо значення коректне
  var rules = {
    login: function (v) {
      if (!v) return 'Вкажіть логін';
      if (v.length < 5 || v.length > 32) return 'Логін має містити від 5 до 32 символів';
      if (!LOGIN.test(v)) return 'Логін може містити лише латинські літери, цифри та символи _ . -';
      return '';
    },
    firstName: function (v) {
      if (!v) return "Вкажіть ім'я";
      if (v.length > 50) return "Ім'я не може бути довшим за 50 символів";
      if (!NAME.test(v)) return "Ім'я має містити лише літери";
      return '';
    },
    email: function (v) {
      if (!v) return 'Вкажіть e-mail';
      if (v.length > 254) return 'E-mail не може бути довшим за 254 символи';
      if (!EMAIL.test(v)) return "E-mail має містити @ та доменне ім'я, наприклад name@example.com";
      return '';
    },
    password: function (v) {
      if (!v) return 'Вкажіть пароль';
      if (v.length < 8) return 'Пароль має містити від 8 до 72 символів';
      if (new TextEncoder().encode(v).length > 72) return 'Пароль задовгий: допустимо не більше 72 байтів у UTF-8';
      if (!/\p{Lu}/u.test(v)) return 'Пароль має містити принаймні одну велику літеру';
      if (!/[0-9]/.test(v)) return 'Пароль має містити принаймні одну цифру';
      return '';
    },
    phone: function (v) {
      if (!v) return 'Вкажіть номер телефону';
      if (!PHONE.test(v)) return "Телефон: необов'язковий + і від 10 до 15 цифр, без пробілів";
      return '';
    },
    birthDate: function (v) {
      if (!v) return 'Вкажіть дату народження у форматі РРРР-ММ-ДД';
      var m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(v);
      if (!m) return 'Некоректна дата: використовуйте формат РРРР-ММ-ДД';
      var y = +m[1], mo = +m[2], d = +m[3];
      var date = new Date(Date.UTC(y, mo - 1, d));
      if (date.getUTCFullYear() !== y || date.getUTCMonth() !== mo - 1 || date.getUTCDate() !== d) {
        return 'Некоректна дата: такого числа в календарі немає';
      }
      var now = new Date();
      var today = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
      if (date.getTime() > today) return 'Дата народження не може бути в майбутньому';
      if (y < 1900) return 'Дата народження не може бути раніше 1900 року';
      var adult = Date.UTC(now.getFullYear() - 18, now.getMonth(), now.getDate());
      if (date.getTime() > adult) return 'Орендувати авто можна з 18 років';
      return '';
    }
  };

  function show(form, name, message) {
    var input = form.elements[name];
    var out = form.querySelector('.err[data-for="' + name + '"]');
    if (out) out.textContent = message;
    input.classList.toggle('invalid', message !== '');
    input.classList.toggle('valid', message === '');
    input.setAttribute('aria-invalid', message === '' ? 'false' : 'true');
  }

  function check(form, name) {
    var message = rules[name](form.elements[name].value);
    show(form, name, message);
    return message === '';
  }

  document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('reg');
    if (!form) return;
    Object.keys(rules).forEach(function (name) {
      var input = form.elements[name];
      input.addEventListener('blur', function () { check(form, name); });
      input.addEventListener('input', function () {
        if (input.classList.contains('invalid')) check(form, name);
      });
    });
    form.addEventListener('submit', function (event) {
      var ok = true;
      Object.keys(rules).forEach(function (name) { ok = check(form, name) && ok; });
      if (!ok) {
        event.preventDefault();
        var first = form.querySelector('input.invalid');
        if (first) first.focus();
      }
    });
  });
})();
