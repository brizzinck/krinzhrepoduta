#!/bin/bash
# Клієнтську валідацію обходять запитом без браузера: сервер має відхилити некоректні дані сам.
. "$(dirname "$0")/lib.sh"

post_form "Порожня форма: жодного поля (JavaScript не потрібен)" login= firstName= email= password= phone= birthDate=
post_form "Некоректні значення в кожному полі" login=abc firstName=Ol3na email=olena@example password=secret phone=abc birthDate=2099-01-01
post_form "Порожній логін і некоректні решта полів: помилки формату не ховаються" login= firstName=Ol3na email=olena@example password=secret phone=abc birthDate=2099-01-01
post_form "Пароль без цифри / без великої літери / коротший за 8" login=client01 firstName=Олена email=o@example.com password=SecretPass phone=+380501234567 birthDate=1998-04-12
post_form "Дата не існує в календарі і не-дата" login=client01 firstName=Олена email=o@example.com password=Secret123 phone=+380501234567 birthDate=2026-02-31
post_form "Mass assignment: корисне навантаження + зайве поле role=ADMIN (має ігноруватися)" login=attacker1 firstName=Олена email=att@example.com password=Secret123 phone=+380501234567 birthDate=1998-04-12 role=ADMIN
echo
echo "# Що збережено в БД для attacker1 (роль не змінилася)"
echo "\$ mysql ... -e \"SELECT login, role, LEFT(password_hash,7) AS hash_prefix FROM users WHERE login='attacker1'\""
$DBEXEC -e "SELECT login, role, LEFT(password_hash,7) AS hash_prefix FROM users WHERE login='attacker1'" 2>/dev/null
