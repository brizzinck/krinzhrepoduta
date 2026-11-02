#!/usr/bin/env python3
"""Генерує колекцію Postman (формат v2.1) для лабораторної роботи №3 за текстом методички."""
import json
from urllib.parse import quote

BASE = "{{baseUrl}}"


def test(lines):
    return {"listen": "test", "script": {"type": "text/javascript", "exec": lines}}


def url(path, query=None):
    q = [{"key": k, "value": v} for k, v in (query or [])]
    raw = BASE + path
    if q:
        # у raw значення кодуються, у масиві query лишаються як ввів користувач
        raw += "?" + "&".join(f"{k}={quote(v, safe='')}" for k, v in (query or []))
    u = {"raw": raw, "host": [BASE], "path": path.strip("/").split("/")}
    if q:
        u["query"] = q
    return u


def get(name, path, query=None, headers=None, tests=None):
    return {
        "name": name,
        "event": [test(tests)],
        "request": {
            "method": "GET",
            "header": [{"key": k, "value": v} for k, v in (headers or [])],
            "url": url(path, query),
        },
    }


def post(name, path, body, tests):
    return {
        "name": name,
        "event": [test(tests)],
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "raw",
                "raw": json.dumps(body, indent=4, ensure_ascii=False),
                "options": {"raw": {"language": "json"}},
            },
            "url": url(path),
        },
    }


STATUS = lambda code: [
    f'pm.test("Status code is {code}", function () {{',
    f"    pm.response.to.have.status({code});",
    "});",
]

items = [
    get("01 - GET Query Parameters", "/get",
        query=[("student", "QAStudent"), ("course", "SoftwareTesting"), ("year", "3")],
        tests=STATUS(200) + [
            "",
            'pm.test("Response is JSON", function () {',
            "    pm.response.to.be.json;",
            "});",
            "",
            'pm.test("Query parameters are correct", function () {',
            "    const body = pm.response.json();",
            "",
            '    pm.expect(body.args.student).to.eql("QAStudent");',
            '    pm.expect(body.args.course).to.eql("SoftwareTesting");',
            '    pm.expect(body.args.year).to.eql("3");',
            "});",
        ]),
    get("02 - GET Special Characters", "/get", query=[("search", "API testing & QA")],
        tests=[
            'pm.test("Special characters are preserved", function () {',
            "    const body = pm.response.json();",
            '    pm.expect(body.args.search).to.eql("API testing & QA");',
            "});",
        ]),
    get("02b - GET Empty Search Parameter", "/get", query=[("search", "")],
        tests=[
            'pm.test("Empty parameter is returned", function () {',
            "    const args = pm.response.json().args;",
            "",
            '    pm.expect(args).to.have.property("search");',
            '    pm.expect(args.search).to.eql("");',
            "});",
        ]),
    get("03 - GET Request Headers", "/headers", headers=[("X-QA-Student", "Student-03")],
        tests=STATUS(200) + [
            "",
            'pm.test("Header X-QA-Student is returned", function () {',
            "    const headers = pm.response.json().headers;",
            "    // Postman Echo повертає імена заголовків у нижньому регістрі",
            '    pm.expect(headers["x-qa-student"]).to.eql("Student-03");',
            "});",
        ]),
    post("04 - POST JSON Object", "/post",
         {"name": "Test Student", "email": "student@example.com", "role": "QA", "active": True},
         STATUS(200) + [
             "",
             'pm.test("All sent values are returned", function () {',
             "    const json = pm.response.json().json;",
             "",
             '    pm.expect(json.name).to.eql("Test Student");',
             '    pm.expect(json.email).to.eql("student@example.com");',
             '    pm.expect(json.role).to.eql("QA");',
             "    pm.expect(json.active).to.eql(true);",
             "});",
         ]),
    post("05 - POST JSON Data Types", "/post",
         {"studentId": 305, "name": "QA Student", "skills": ["API", "SQL"],
          "active": True, "middleName": None, "comment": ""},
         [
             'pm.test("Required fields are present", function () {',
             "    const json = pm.response.json().json;",
             "",
             '    pm.expect(json).to.have.property("studentId");',
             '    pm.expect(json).to.have.property("name");',
             '    pm.expect(json).to.have.property("skills");',
             '    pm.expect(json).to.have.property("active");',
             "});",
             "",
             'pm.test("Values have correct types and formats", function () {',
             "    const json = pm.response.json().json;",
             "",
             '    pm.expect(json.studentId).to.be.a("number");',
             '    pm.expect(json.name).to.be.a("string");',
             '    pm.expect(json.skills).to.be.an("array");',
             '    pm.expect(json.active).to.be.a("boolean");',
             "    pm.expect(json.middleName).to.be.null;",
             '    pm.expect(json.comment).to.eql("");',
             "});",
         ]),
    get("06 - GET Expected 404", "/status/404", tests=STATUS(404)),
    get("07 - GET Expected 500", "/status/500", tests=STATUS(500)),
]

collection = {
    "info": {
        "name": "API Testing - Student(Lavrishko)",
        "description": "Лабораторна робота №3. Тестування REST API за допомогою Postman Echo.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "variable": [{"key": "baseUrl", "value": "https://postman-echo.com", "type": "string"}],
    "item": items,
}

with open("API_Testing_Student.postman_collection.json", "w", encoding="utf-8") as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)
    f.write("\n")
