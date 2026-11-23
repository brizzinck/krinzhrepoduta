#!/usr/bin/env python3
"""Локальні моделі механізмів вразливостей із ЛР 4-5 (лише стандартна бібліотека, без мережі).

Це не стенд PortSwigger: стенди живуть онлайн, і їх неможливо відтворити офлайн.
Тут кожна вразливість показана на крихітному «сервері» з однією хибою і з виправленою версією.
Використання:  python3 -I scripts/models.py enum | access | jwt | oauth
"""
import base64
import hashlib
import hmac
import json
import sys


def b64(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def unb64(text: str) -> bytes:
    return base64.urlsafe_b64decode(text + "=" * (-len(text) % 4))


def enum():
    """Вправа 1: різні відповіді на неіснуюче імʼя й на невірний пароль."""
    users = {"user7": "pass7", "user9": "pass9"}  # вигадані імена моделі

    def login_bad(user, password):  # хиба: різні повідомлення
        if user not in users:
            return 200, "Invalid username"
        return (302, "") if users[user] == password else (200, "Incorrect password")

    def login_ok(user, password):  # виправлення: одне повідомлення
        return (302, "") if users.get(user) == password else (200, "Invalid username or password")

    page = lambda msg: "<html>" + "x" * 3000 + f"<p class=is-warning>{msg}</p></html>"
    names = ["administrator", "adm", "user", "user7", "info", "test"]
    passwords = ["123456", "password", "pass7", "qwerty"]
    for label, login in (("Вразливий вхід", login_bad), ("Виправлений вхід", login_ok)):
        print(f"{label}:")
        sizes = {n: len(page(login(n, "wrong-password")[1])) for n in names}
        base = max(set(sizes.values()), key=list(sizes.values()).count)
        odd = [n for n, s in sizes.items() if s != base]
        print("  довжина відповіді для кожного імені:", ", ".join(f"{n}={s}" for n, s in sizes.items()))
        print("  імена з іншою довжиною:", odd or "немає")
        if odd:
            hit = [p for p in passwords if login(odd[0], p)[0] == 302]
            print(f"  перебір паролів для {odd[0]}: код 302 дає пароль {hit[0]}")
            print("  (імена й паролі в моделі вигадані: це показ різних відповідей, а не перебір на стенді; імʼя access і пароль nicole взято зі стенду, див. рис. 1)")


def access():
    """Вправи 2 і 3: приховування адреси замість перевірки прав; масове присвоєння roleid."""
    print("Вправа 2. robots.txt розкриває адресу адмінки:")
    robots = "User-agent: *\nDisallow: /administrator-panel"
    path = [l.split(": ")[1] for l in robots.splitlines() if l.startswith("Disallow")][0]
    print("  знайдено шлях", path)

    def panel_bad(session):  # немає перевірки ролі
        return 200

    def panel_ok(session):
        return 200 if session.get("roleid") == 2 else 403

    guest = {"user": "wiener", "roleid": 1}
    print(f"  GET {path} без прав: вразливий {panel_bad(guest)}, виправлений {panel_ok(guest)}")

    print("Вправа 3. POST /my-account/change-email із зайвим полем roleid:")
    def change_bad(user, body):  # хиба: сервер зберігає всі поля тіла
        user.update(body)
    def change_ok(user, body):  # виправлення: білий список полів
        if "email" in body:
            user["email"] = body["email"]
    for label, fn in (("вразливий", change_bad), ("виправлений", change_ok)):
        user = {"user": "wiener", "email": "w@x.test", "roleid": 1}
        fn(user, {"email": "w2@x.test", "roleid": 2})
        print(f"  {label}: roleid став {user['roleid']}, доступ до адмінки {panel_ok(user)}")


def jwt():
    """Вправи 4 і 5: токен без перевірки підпису й токен з alg=none."""
    key = b"server-secret"

    def make(header, payload, signed=True):
        h, p = b64(json.dumps(header, separators=(",", ":")).encode()), b64(json.dumps(payload, separators=(",", ":")).encode())
        sig = b64(hmac.new(key, f"{h}.{p}".encode(), hashlib.sha256).digest()) if signed else ""
        return f"{h}.{p}.{sig}"

    def parse(token):
        h, p, s = token.split(".")
        return json.loads(unb64(h)), json.loads(unb64(p)), s

    def server_no_check(token):  # вправа 4: підпис не перевіряється взагалі
        return parse(token)[1]["sub"]

    def server_trusts_alg(token):  # вправа 5: довіряє alg із заголовка токена
        head, payload, sig = parse(token)
        if head["alg"] == "none":
            return payload["sub"]
        return correct(token)

    def correct(token):  # виправлення: фіксований алгоритм і обовʼязковий підпис (у моделі HS256; у звіті рекомендовано фіксований RS256)
        head, payload, sig = parse(token)
        h, p, _ = token.split(".")
        good = b64(hmac.new(key, f"{h}.{p}".encode(), hashlib.sha256).digest())
        if head["alg"] != "HS256" or not hmac.compare_digest(sig, good):
            return None
        return payload["sub"]

    def show(label, server, token):
        print(f"  {label}: {server(token) or 'відхилено (401)'}")

    wiener = make({"alg": "HS256", "typ": "JWT"}, {"sub": "wiener"})
    h, p, s = wiener.split(".")
    print("Модельний токен для wiener (не стенд): підписано HS256 локальним ключем моделі; у лабораторіях PortSwigger токен мав alg RS256:")
    print("  header :", unb64(h).decode())
    print("  payload:", unb64(p).decode())
    print("  підпис :", s[:20] + "...")
    admin_payload = b64(json.dumps({"sub": "administrator"}, separators=(",", ":")).encode())
    print("Вправа 4. Змінено лише payload (sub=administrator), підпис лишився старий:")
    forged = f"{h}.{admin_payload}.{s}"
    print("  payload:", unb64(forged.split(".")[1]).decode())
    show("сервер без перевірки підпису бачить", server_no_check, forged)
    show("сервер із перевіркою підпису бачить", correct, forged)
    print("Вправа 5. alg=none, sub=administrator, підпис видалено (токен виду header.payload.):")
    none_head = b64(json.dumps({"alg": "none", "typ": "JWT"}, separators=(",", ":")).encode())
    nonetoken = f"{none_head}.{admin_payload}."
    print("  токен:", nonetoken)
    show("сервер, що довіряє alg, бачить", server_trusts_alg, nonetoken)
    show("сервер із фіксованим алгоритмом бачить", correct, nonetoken)


def oauth():
    """Вправа 6: клієнт довіряє email із тіла запиту /authenticate, а не власнику токена."""
    tokens = {"tok-wiener": {"email": "wiener@normal-user.net", "username": "wiener"}}  # відповідь /userinfo провайдера

    def authenticate_bad(body):  # хиба: email береться з тіла
        return body["email"]

    def authenticate_ok(body):  # виправлення: особа з /userinfo за токеном
        info = tokens.get(body["token"])
        return info["email"] if info else None

    body = {"email": "wiener@normal-user.net", "username": "wiener", "token": "tok-wiener"}
    print("Штатний запит /authenticate:", authenticate_bad(body), "та", authenticate_ok(body))
    body["email"] = "carlos@carlos-montoya.net"
    print("Той самий токен, але email жертви:")
    print("  вразливий клієнт створює сесію для:", authenticate_bad(body))
    print("  виправлений клієнт створює сесію для:", authenticate_ok(body))


if __name__ == "__main__":
    {"enum": enum, "access": access, "jwt": jwt, "oauth": oauth}[sys.argv[1]]()
