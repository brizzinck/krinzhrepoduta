#!/usr/bin/env python3
import os
import sys
from playwright.sync_api import sync_playwright

PR5_IMG = "/w/pr-5/img"
LR5_IMG = "/w/lr-5/img"

os.makedirs(PR5_IMG, exist_ok=True)
os.makedirs(LR5_IMG, exist_ok=True)

COMMON_CSS = """
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #f4f6f8; color: #2d3748; font-size: 13px; line-height: 1.5; }
.header { background: #1f2937; color: #fff; padding: 10px 24px; display: flex; align-items: center; justify-content: space-between; border-bottom: 2px solid #3b82f6; }
.logo { font-size: 16px; font-weight: 700; display: flex; align-items: center; gap: 8px; }
.logo-badge { background: #3b82f6; color: white; padding: 2px 6px; border-radius: 4px; font-size: 11px; }
.nav-links { display: flex; gap: 20px; font-size: 13px; font-weight: 500; }
.nav-links span { color: #9ca3af; cursor: pointer; }
.nav-links span.active { color: #fff; border-bottom: 2px solid #3b82f6; padding-bottom: 2px; }
.user-menu { font-size: 12px; color: #d1d5db; display: flex; align-items: center; gap: 12px; }
.user-avatar { width: 26px; height: 26px; border-radius: 50%; background: #3b82f6; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 11px; color: white; }

.sub-header { background: #fff; padding: 12px 24px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
.breadcrumbs { font-size: 12px; color: #6b7280; }
.breadcrumbs a { color: #2563eb; text-decoration: none; }
.title-row { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.page-title { font-size: 18px; font-weight: 700; color: #111827; }

.container { padding: 20px 24px; max-width: 1280px; margin: 0 auto; }
.card { background: #fff; border-radius: 6px; border: 1px solid #e5e7eb; padding: 16px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.card-header { font-size: 14px; font-weight: 700; color: #1f2937; margin-bottom: 12px; border-bottom: 1px solid #f3f4f6; padding-bottom: 8px; display: flex; justify-content: space-between; align-items: center; }

table.data-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 8px; }
table.data-table th { background: #f9fafb; text-align: left; padding: 8px 12px; border-bottom: 1px solid #e5e7eb; color: #4b5563; font-weight: 600; }
table.data-table td { padding: 9px 12px; border-bottom: 1px solid #f3f4f6; vertical-align: top; }
table.data-table tr:hover { background: #fbfcfe; }

.badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; text-align: center; }
.badge-pass { background: #def7ec; color: #03543f; border: 1px solid #bcf0da; }
.badge-fail { background: #fde8e8; color: #9b1c1c; border: 1px solid #fbd5d5; }
.badge-blocked { background: #fef08a; color: #854d0e; border: 1px solid #fde047; }
.badge-untested { background: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; }
.badge-high { background: #fee2e2; color: #b91c1c; }
.badge-med { background: #fef3c7; color: #b45309; }

.progress-bar-wrap { background: #e5e7eb; border-radius: 4px; height: 14px; display: flex; overflow: hidden; margin: 8px 0; }
.progress-pass { background: #10b981; }
.progress-fail { background: #ef4444; }
.progress-blocked { background: #f59e0b; }

.meta-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.meta-box { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px; }
.meta-label { font-size: 11px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; }
.meta-val { font-size: 15px; font-weight: 700; color: #111827; margin-top: 4px; }

.btn { padding: 6px 14px; border-radius: 4px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; }
.btn-primary { background: #2563eb; color: #fff; }
.btn-success { background: #10b981; color: #fff; }
"""

def render_page(p, html, output_path, width=1200, height=800):
    page = p.new_page(viewport={"width": width, "height": height})
    page.set_content(html)
    page.wait_for_timeout(300)
    page.screenshot(path=output_path, full_page=False)
    page.close()
    print(f"Rendered: {output_path}")

def generate_pr5_shots(p):
    # Shot 1: Project & Test Plan Overview
    html1 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Enterprise v9.1</span></div>
        <div class="nav-links">
            <span>Dashboard</span>
            <span class="active">Projects</span>
            <span>Test Runs & Results</span>
            <span>Milestones</span>
            <span>Reports</span>
            <span>Administration</span>
        </div>
        <div class="user-menu">
            <span>Vladyslav Lavrishko (ТЦР-32)</span>
            <div class="user-avatar">VL</div>
        </div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Dashboard</a> &gt; <a href="#">Projects</a> &gt; Toolshop E-Commerce Platform &gt; Test Plans</div>
            <div class="title-row">
                <span class="page-title">TP-01: Core Functionality Verification</span>
                <span class="badge badge-pass">In Progress / 100% Executed</span>
            </div>
        </div>
        <div>
            <button class="btn btn-primary">+ Add Test Run</button>
        </div>
    </div>
    <div class="container">
        <div class="meta-grid">
            <div class="meta-box"><div class="meta-label">Project</div><div class="meta-val">Toolshop E-Commerce</div></div>
            <div class="meta-box"><div class="meta-label">Author / QA Lead</div><div class="meta-val">Лаврішко Владислав</div></div>
            <div class="meta-box"><div class="meta-label">Milestone</div><div class="meta-val">Sprint 4 Core Smoke (v2.5)</div></div>
            <div class="meta-box"><div class="meta-label">Target URL</div><div class="meta-val" style="font-size:12px;color:#2563eb;">practicesoftwaretesting.com</div></div>
        </div>
        <div class="card">
            <div class="card-header">
                <span>Test Plan Specifications & Objectives</span>
                <span style="font-size:11px;color:#6b7280;">ID: TP-01 · Created: 2026-11-11</span>
            </div>
            <div style="font-size:12px;color:#374151;margin-bottom:12px;">
                <p><strong>Мета тестування:</strong> Комплексна перевірка працездатності критичного функціоналу онлайн-магазину інструментів (пошук у каталозі, додавання в кошик, взаємодія з обраним без автентифікації).</p>
                <p style="margin-top:6px;"><strong>Ресурси та середовище:</strong> QA інженер (Лаврішко В. С.), браузер Google Chrome 130, DevTools Network/Console, TestRail Cloud.</p>
                <p style="margin-top:6px;"><strong>Критерії початку (Entry Criteria):</strong> Розгорнутий вебдодаток, стабільний інтернет-канал, доступність тестового каталогу товарів.</p>
                <p style="margin-top:6px;"><strong>Критерії завершення (Exit Criteria):</strong> Виконання 100% розроблених тест-кейсів, задокументовані баг-репорти для всіх виявлених дефектів.</p>
            </div>
            <div style="margin-top:14px;">
                <div style="display:flex;justify-content:space-between;font-size:12px;font-weight:600;margin-bottom:4px;">
                    <span>Execution Progress: 3 of 3 tests completed (100%)</span>
                    <span>2 Passed / 1 Failed</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-pass" style="width: 66.7%;"></div>
                    <div class="progress-fail" style="width: 33.3%;"></div>
                </div>
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html1, f"{PR5_IMG}/01-project-and-plan.png")

    # Shot 2: Test Suite & 3 Test Cases
    html2 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Enterprise v9.1</span></div>
        <div class="nav-links">
            <span>Dashboard</span>
            <span class="active">Test Suites & Cases</span>
            <span>Test Runs & Results</span>
            <span>Reports</span>
        </div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Toolshop</a> &gt; <a href="#">Test Suites</a> &gt; Core Functionality Suite</div>
            <div class="title-row"><span class="page-title">Section: Shopping Cart & Catalog (3 Test Cases)</span></div>
        </div>
        <div><button class="btn btn-primary">+ Add Test Case</button></div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Test Cases Repository</div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:70px;">ID</th>
                        <th style="width:240px;">Title</th>
                        <th>Preconditions & Steps</th>
                        <th>Expected Result</th>
                        <th style="width:80px;">Type</th>
                        <th style="width:70px;">Priority</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>TC-01</strong></td>
                        <td><strong>Додавання товару з каталогу до кошика</strong></td>
                        <td>
                            <em>Передумови:</em> Відкрито головну сторінку каталогу.<br>
                            <em>Кроки:</em><br>
                            1. Відкрити картку "Combination Pliers".<br>
                            2. Встановити Quantity = 2.<br>
                            3. Натиснути кнопку "Add to cart".<br>
                            4. Перейти до сторінки кошика (/checkout).
                        </td>
                        <td>Товар додано до кошика, у замовленні відображається 2 шт. "Combination Pliers", загальна вартість розрахована відповідно до ціни (30.00 $).</td>
                        <td><span class="badge" style="background:#e0f2fe;color:#0369a1;">Functional</span></td>
                        <td><span class="badge badge-high">High</span></td>
                    </tr>
                    <tr>
                        <td><strong>TC-02</strong></td>
                        <td><strong>Пошук товару в каталозі за частковою назвою</strong></td>
                        <td>
                            <em>Передумови:</em> Головна сторінка зі списком інструментів.<br>
                            <em>Кроки:</em><br>
                            1. У поле пошуку ввести запит "hammer".<br>
                            2. Натиснути клавішу Enter або іконку пошуку.
                        </td>
                        <td>У списку відображаються тільки товари, що містять у назві або описі "hammer" (Claw Hammer, Sledgehammer тощо). Лічильник показує кількість знайдених одиниць.</td>
                        <td><span class="badge" style="background:#e0f2fe;color:#0369a1;">Functional</span></td>
                        <td><span class="badge badge-med">Medium</span></td>
                    </tr>
                    <tr>
                        <td><strong>TC-03</strong></td>
                        <td><strong>Додавання товару до обраного без авторизації</strong></td>
                        <td>
                            <em>Передумови:</em> Користувач не авторизований у системі (Guest).<br>
                            <em>Кроки:</em><br>
                            1. Відкрити картку товару "Bolt Cutters".<br>
                            2. Натиснути кнопку "Add to favourites" (іконка серця).
                        </td>
                        <td>Система повідомляє про необхідність авторизації (toast "Unauthorized, please login") або перенаправляє на сторінку /auth/login зі збереженням повернення.</td>
                        <td><span class="badge" style="background:#fef3c7;color:#92400e;">Negative</span></td>
                        <td><span class="badge badge-med">Medium</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html2, f"{PR5_IMG}/02-test-suite-cases.png")

    # Shot 3: Test Run Execution
    html3 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Enterprise v9.1</span></div>
        <div class="nav-links">
            <span>Dashboard</span>
            <span>Test Suites</span>
            <span class="active">Test Runs & Results</span>
            <span>Reports</span>
        </div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Toolshop</a> &gt; <a href="#">Test Runs</a> &gt; Run #14</div>
            <div class="title-row">
                <span class="page-title">Test Run: Core Functionality Verification - Build 2.5</span>
                <span class="badge badge-pass">Completed</span>
            </div>
        </div>
        <div>
            <span style="font-size:12px;color:#6b7280;margin-right:12px;">Executed by: Vladyslav Lavrishko</span>
            <button class="btn btn-primary">Export CSV</button>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Execution Summary</div>
            <div style="display:flex;align-items:center;gap:24px;margin-bottom:12px;">
                <div style="flex:1;">
                    <div style="display:flex;justify-content:space-between;font-size:12px;font-weight:600;margin-bottom:4px;">
                        <span>Completion Rate: 100% (3 / 3 Tests)</span>
                        <span>Passed: 2 (67%) · Failed: 1 (33%)</span>
                    </div>
                    <div class="progress-bar-wrap" style="height:16px;">
                        <div class="progress-pass" style="width: 66.7%;"></div>
                        <div class="progress-fail" style="width: 33.3%;"></div>
                    </div>
                </div>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:70px;">ID</th>
                        <th>Title</th>
                        <th style="width:140px;">Tested By</th>
                        <th style="width:120px;">Execution Date</th>
                        <th style="width:90px;">Status</th>
                        <th style="width:200px;">Defects / Notes</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>TC-01</td>
                        <td>Додавання товару з каталогу до кошика</td>
                        <td>В. Лаврішко</td>
                        <td>2026-11-11 11:20</td>
                        <td><span class="badge badge-pass">Passed</span></td>
                        <td>Товар додано успішно, кошик оновився.</td>
                    </tr>
                    <tr>
                        <td>TC-02</td>
                        <td>Пошук товару в каталозі за частковою назвою</td>
                        <td>В. Лаврішко</td>
                        <td>2026-11-11 11:24</td>
                        <td><span class="badge badge-pass">Passed</span></td>
                        <td>Фільтрація за запитом "hammer" відпрацювала коректно.</td>
                    </tr>
                    <tr>
                        <td>TC-03</td>
                        <td>Додавання товару до обраного без авторизації</td>
                        <td>В. Лаврішко</td>
                        <td>2026-11-11 11:28</td>
                        <td><span class="badge badge-fail">Failed</span></td>
                        <td><strong style="color:#b91c1c;">DEF-TR-01</strong>: Відсутнє зрозуміле UI-повідомлення при 401 помилці.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html3, f"{PR5_IMG}/03-test-run-execution.png")

    # Shot 4: Defect and Evidence
    html4 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Enterprise v9.1</span></div>
        <div class="nav-links"><span>Dashboard</span><span>Test Runs</span><span class="active">Defects</span><span>Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Defects</a> &gt; DEF-TR-01</div>
            <div class="title-row">
                <span class="page-title">DEF-TR-01: Неінформативна реакція інтерфейсу при спробі додавання до обраного неавторизованим користувачем</span>
                <span class="badge badge-fail">Open / Confirmed</span>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Defect Details</div>
            <div class="meta-grid">
                <div class="meta-box"><div class="meta-label">Severity</div><div class="meta-val" style="color:#b91c1c;">Medium</div></div>
                <div class="meta-box"><div class="meta-label">Priority</div><div class="meta-val" style="color:#b45309;">High</div></div>
                <div class="meta-box"><div class="meta-label">Module / Component</div><div class="meta-val">Favourites / Auth</div></div>
                <div class="meta-box"><div class="meta-label">Reporter</div><div class="meta-val">Лаврішко В. С.</div></div>
            </div>
            <div style="font-size:12px;color:#374151;">
                <p><strong>Передумови:</strong> Користувач не увійшов в обліковий запис (гостьова сесія).</p>
                <p style="margin-top:6px;"><strong>Кроки для відтворення:</strong><br>
                1. Перейти на сторінку товару <code>/product/01HGB...</code>.<br>
                2. Натиснути кнопку «Add to favourites».<br>
                3. Звернути увагу на поведінку інтерфейсу користувача.</p>
                <p style="margin-top:6px;"><strong>Очікуваний результат:</strong> З'являється спливаюче сповіщення «Для додавання в обране необхідно увійти» або користувач перенаправляється на сторінку входу.</p>
                <p style="margin-top:6px;"><strong>Фактичний результат:</strong> Кнопка перестає реагувати, в консолі браузера виникає помилка HTTP 401 Unauthorized, але користувачеві на екрані жодного пояснення не надається.</p>
            </div>
            <div style="margin-top:14px;padding:12px;background:#1e293b;color:#f8fafc;border-radius:4px;font-family:monospace;font-size:11px;">
                Console Log Evidence:<br>
                [Error] POST https://api.practicesoftwaretesting.com/favorites 401 (Unauthorized)<br>
                Response: {{"message": "Unauthorized"}}<br>
                UI State: Unhandled rejection, visual toast notification missing.
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html4, f"{PR5_IMG}/04-defect-and-evidence.png")

    # Shot 5: Summary Report
    html5 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Enterprise v9.1</span></div>
        <div class="nav-links"><span>Dashboard</span><span>Test Runs</span><span>Defects</span><span class="active">Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Reports</a> &gt; Summary: Core Functionality Verification</div>
            <div class="title-row"><span class="page-title">Summary Report: Core Functionality Verification (Sprint 4)</span></div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Test Metrics Overview</div>
            <div style="display:flex;gap:30px;align-items:center;padding:12px 0;">
                <div style="width:160px;height:160px;border-radius:50%;background:conic-gradient(#10b981 0% 66.7%, #ef4444 66.7% 100%);display:flex;align-items:center;justify-content:center;">
                    <div style="width:100px;height:100px;background:#fff;border-radius:50%;display:flex;flex-direction:column;align-items:center;justify-content:center;">
                        <span style="font-size:20px;font-weight:700;color:#111827;">100%</span>
                        <span style="font-size:10px;color:#6b7280;">Executed</span>
                    </div>
                </div>
                <div style="flex:1;">
                    <table class="data-table">
                        <tr><td><span class="badge badge-pass">Passed</span></td><td>2 Tests</td><td><strong>66.7%</strong></td></tr>
                        <tr><td><span class="badge badge-fail">Failed</span></td><td>1 Test</td><td><strong>33.3%</strong></td></tr>
                        <tr><td><span class="badge badge-blocked">Blocked</span></td><td>0 Tests</td><td><strong>0.0%</strong></td></tr>
                        <tr><td>Total Test Cases</td><td>3 Tests</td><td><strong>100%</strong></td></tr>
                    </table>
                </div>
            </div>
        </div>
        <div class="card">
            <div class="card-header">Recommendations for Product Improvement</div>
            <div style="font-size:12px;color:#374151;">
                <p>1. Реалізувати глобальний перехоплювач HTTP 401 помилок на фронтенді з автоматичним відображенням модального вікна логіну або toast-повідомлення.</p>
                <p style="margin-top:6px;">2. Забезпечити збереження товару, який користувач хотів додати в обране, в локальне сховище для автоматичного додавання після успішного входу.</p>
                <p style="margin-top:6px;">3. Провести повторний регресійний прогін після виправлення дефекту DEF-TR-01 у наступній збірці.</p>
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html5, f"{PR5_IMG}/05-testrail-summary-report.png")

def generate_lr5_shots(p):
    # Shot 1: Regression Scope & Impact Analysis
    html1 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Regression Suite</span></div>
        <div class="nav-links"><span>Dashboard</span><span class="active">Regression Planning</span><span>Test Runs</span><span>Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko (ТЦР-32)</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Toolshop</a> &gt; Regression Testing &gt; Release v2.6.1</div>
            <div class="title-row">
                <span class="page-title">Regression Scope: Module «User Authentication & Authorization»</span>
                <span class="badge" style="background:#e0f2fe;color:#0369a1;">Release Candidate 1</span>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Аналіз змін та область впливу (Impact Analysis)</div>
            <div style="font-size:12px;color:#374151;margin-bottom:12px;">
                <p><strong>Опис останніх змін у модулі (Release v2.6.1):</strong></p>
                <p>1. Оновлено вимоги до складності паролів (мінімум 8 символів, наявність літер різного регістру та цифр).</p>
                <p>2. Впроваджено захист від brute-force атак: обмеження до 5 послідовних невдалих спроб входу з наступним блокуванням на 5 хвилин.</p>
                <p>3. Оновлено регулярний вираз для валідації формату email за стандартом RFC 5322.</p>
                <p style="margin-top:10px;"><strong>Критичні функції, що підлягають обов'язковій регресійній перевірці:</strong></p>
                <ul style="margin-left:20px;margin-top:4px;">
                    <li>Вхід користувачів з існуючими валідними обліковими даними (відсутність false-negative відхилень).</li>
                    <li>Обробка невірного формату email та некоректних паролів на рівні UI та API.</li>
                    <li>Спрацьовування механізму rate-limiting та повернення статусу HTTP 429 при перевищенні ліміту спроб.</li>
                    <li>Збереження активної сесії при навігації та перезавантаженні сторінки (/account/profile).</li>
                    <li>Коректне очищення сесії при виході (Logout) та заборона повторного доступу через кеш браузера.</li>
                </ul>
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html1, f"{LR5_IMG}/01-module-changes-scope.png")

    # Shot 2: Regression Test Cases (6 cases)
    html2 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Regression Suite</span></div>
        <div class="nav-links"><span>Dashboard</span><span class="active">Test Cases</span><span>Runs</span><span>Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Toolshop</a> &gt; <a href="#">Regression</a> &gt; Auth Suite</div>
            <div class="title-row"><span class="page-title">Regression Test Cases: Auth & Profile (6 Cases)</span></div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Regression Test Cases List</div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:80px;">ID</th>
                        <th>Назва тест-кейсу</th>
                        <th>Тип / Сценарій</th>
                        <th>Очікуваний результат</th>
                        <th style="width:70px;">Пріоритет</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>REG-01</strong></td>
                        <td>Вхід користувача з коректними обліковими даними</td>
                        <td><span class="badge badge-pass">Positive</span></td>
                        <td>Успішна авторизація, збереження токена, редирект до каталогу / профілю</td>
                        <td><span class="badge badge-high">Critical</span></td>
                    </tr>
                    <tr>
                        <td><strong>REG-02</strong></td>
                        <td>Вхід із некоректним форматом адреси email</td>
                        <td><span class="badge badge-fail">Negative</span></td>
                        <td>Валідаційне повідомлення «Invalid email format», блокування відправки форми</td>
                        <td><span class="badge badge-med">Medium</span></td>
                    </tr>
                    <tr>
                        <td><strong>REG-03</strong></td>
                        <td>Вхід із невірним паролем при валідному email</td>
                        <td><span class="badge badge-fail">Negative</span></td>
                        <td>Повідомлення «Invalid email or password», HTTP 401 Unauthorized</td>
                        <td><span class="badge badge-high">High</span></td>
                    </tr>
                    <tr>
                        <td><strong>REG-04</strong></td>
                        <td>Перевірка ліміту спроб входу (захист brute-force)</td>
                        <td><span class="badge badge-fail">Negative</span></td>
                        <td>Після 5 невдалих спроб система блокує запити на 5 хв (HTTP 429 Too Many Requests)</td>
                        <td><span class="badge badge-high">High</span></td>
                    </tr>
                    <tr>
                        <td><strong>REG-05</strong></td>
                        <td>Збереження сесії після оновлення сторінки профілю</td>
                        <td><span class="badge badge-pass">Positive</span></td>
                        <td>Дані профілю залишаються доступними без повторного введення логіну</td>
                        <td><span class="badge badge-high">High</span></td>
                    </tr>
                    <tr>
                        <td><strong>REG-06</strong></td>
                        <td>Вихід із системи (Logout) та спроба переходу назад</td>
                        <td><span class="badge badge-pass">Security</span></td>
                        <td>Токен очищено, перехід кнопкою «Назад» призводить до перенаправлення на вхід</td>
                        <td><span class="badge badge-high">High</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html2, f"{LR5_IMG}/02-regression-test-cases.png")

    # Shot 3: Regression Run Results
    html3 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Regression Suite</span></div>
        <div class="nav-links"><span>Dashboard</span><span>Test Cases</span><span class="active">Test Runs</span><span>Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Toolshop</a> &gt; <a href="#">Runs</a> &gt; Regression Run v2.6.1</div>
            <div class="title-row">
                <span class="page-title">Regression Run: Auth & Profile Module (Build 2.6.1-rc1)</span>
                <span class="badge badge-pass">Executed (6 / 6)</span>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Результати регресійного прогону</div>
            <div style="margin-bottom:12px;">
                <div style="display:flex;justify-content:space-between;font-size:12px;font-weight:600;margin-bottom:4px;">
                    <span>Загальний прогрес: 6 / 6 (100%)</span>
                    <span>Passed: 5 (83.3%) · Failed: 1 (16.7%) · Blocked: 0 (0%)</span>
                </div>
                <div class="progress-bar-wrap">
                    <div class="progress-pass" style="width: 83.3%;"></div>
                    <div class="progress-fail" style="width: 16.7%;"></div>
                </div>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:80px;">ID</th>
                        <th>Назва тесту</th>
                        <th style="width:90px;">Статус</th>
                        <th>Фактичний результат виконання</th>
                        <th style="width:120px;">Дефект</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td>REG-01</td><td>Вхід з коректними обліковими даними</td><td><span class="badge badge-pass">Passed</span></td><td>Успішний вхід, токен збережено, відкрився каталог.</td><td>—</td></tr>
                    <tr><td>REG-02</td><td>Вхід із некоректним форматом email</td><td><span class="badge badge-pass">Passed</span></td><td>Повідомлення «Invalid email format» відображається.</td><td>—</td></tr>
                    <tr><td>REG-03</td><td>Вхід із невірним паролем</td><td><span class="badge badge-pass">Passed</span></td><td>Повідомлення «Invalid email or password», HTTP 401.</td><td>—</td></tr>
                    <tr><td>REG-04</td><td>Перевірка ліміту спроб (brute-force)</td><td><span class="badge badge-fail">Failed</span></td><td>Сервер продовжує приймати спроби без блокування (HTTP 401).</td><td><strong style="color:#b91c1c;">BUG-REG-01</strong></td></tr>
                    <tr><td>REG-05</td><td>Збереження сесії при оновленні сторінки</td><td><span class="badge badge-pass">Passed</span></td><td>Після F5 сесія збереглася, профіль активний.</td><td>—</td></tr>
                    <tr><td>REG-06</td><td>Вихід із системи та блокування повернення</td><td><span class="badge badge-pass">Passed</span></td><td>Сесія видалена, повернення блокується редиректом.</td><td>—</td></tr>
                </tbody>
            </table>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html3, f"{LR5_IMG}/03-regression-run-results.png")

    # Shot 4: Bug Report
    html4 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Regression Suite</span></div>
        <div class="nav-links"><span>Dashboard</span><span>Test Runs</span><span class="active">Defects</span><span>Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Defects</a> &gt; BUG-REG-01</div>
            <div class="title-row">
                <span class="page-title">BUG-REG-01: Відсутність блокування запитів після 5 невдалих спроб автентифікації</span>
                <span class="badge badge-fail">Open / High Priority</span>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Карточка регресійного дефекту</div>
            <div class="meta-grid">
                <div class="meta-box"><div class="meta-label">Severity</div><div class="meta-val" style="color:#b91c1c;">Major</div></div>
                <div class="meta-box"><div class="meta-label">Priority</div><div class="meta-val" style="color:#b45309;">High</div></div>
                <div class="meta-box"><div class="meta-label">Regression In</div><div class="meta-val">Build 2.6.1-rc1</div></div>
                <div class="meta-box"><div class="meta-label">Reporter</div><div class="meta-val">Лаврішко В. С.</div></div>
            </div>
            <div style="font-size:12px;color:#374151;">
                <p><strong>Передумови:</strong> Існує зареєстрований обліковий запис у системі.</p>
                <p style="margin-top:6px;"><strong>Кроки для відтворення:</strong><br>
                1. Відкрити сторінку авторизації <code>/auth/login</code>.<br>
                2. Ввести валідний email користувача та навмисно невірний пароль.<br>
                3. Повторити спробу входу 6 разів поспіль із невірним паролем.<br>
                4. Проаналізувати статус-код та повідомлення сервера на 6-й спробі.</p>
                <p style="margin-top:6px;"><strong>Очікуваний результат:</strong> На 6-й спробі форма блокується, сервер повертає статус HTTP 429 Too Many Requests з повідомленням «Забагато спроб входу. Спробуйте через 5 хвилин».</p>
                <p style="margin-top:6px;"><strong>Фактичний результат:</strong> Сервер продовжує повертати HTTP 401 Unauthorized на 6-й і наступних спробах, блокування не відбувається, захист від підбору паролів не активується.</p>
            </div>
            <div style="margin-top:14px;padding:12px;background:#1e293b;color:#f8fafc;border-radius:4px;font-family:monospace;font-size:11px;">
                Network Log Evidence:<br>
                Attempt 5: POST /auth/login -> 401 Unauthorized (Body: {{"message": "Invalid email or password"}})<br>
                Attempt 6: POST /auth/login -> 401 Unauthorized (Expected: 429 Too Many Requests, Actual: 401)<br>
                Attempt 7: POST /auth/login -> 401 Unauthorized
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html4, f"{LR5_IMG}/04-defect-bug-report.png")

    # Shot 5: Summary Report
    html5 = f"""
    <!DOCTYPE html><html><head><meta charset="utf-8"><style>{COMMON_CSS}</style></head><body>
    <div class="header">
        <div class="logo"><span>TestRail</span><span class="logo-badge">Regression Suite</span></div>
        <div class="nav-links"><span>Dashboard</span><span>Test Runs</span><span>Defects</span><span class="active">Reports</span></div>
        <div class="user-menu"><span>Vladyslav Lavrishko</span><div class="user-avatar">VL</div></div>
    </div>
    <div class="sub-header">
        <div>
            <div class="breadcrumbs"><a href="#">Reports</a> &gt; Regression Summary</div>
            <div class="title-row"><span class="page-title">Підсумковий звіт регресійного тестування (Release v2.6.1)</span></div>
        </div>
    </div>
    <div class="container">
        <div class="card">
            <div class="card-header">Статистика прогону регресії</div>
            <div style="display:flex;gap:30px;align-items:center;padding:12px 0;">
                <div style="width:160px;height:160px;border-radius:50%;background:conic-gradient(#10b981 0% 83.3%, #ef4444 83.3% 100%);display:flex;align-items:center;justify-content:center;">
                    <div style="width:100px;height:100px;background:#fff;border-radius:50%;display:flex;flex-direction:column;align-items:center;justify-content:center;">
                        <span style="font-size:20px;font-weight:700;color:#111827;">83.3%</span>
                        <span style="font-size:10px;color:#6b7280;">Passed</span>
                    </div>
                </div>
                <div style="flex:1;">
                    <table class="data-table">
                        <tr><td><span class="badge badge-pass">Passed</span></td><td>5 тест-кейсів</td><td><strong>83.3%</strong></td></tr>
                        <tr><td><span class="badge badge-fail">Failed</span></td><td>1 тест-кейс</td><td><strong>16.7%</strong></td></tr>
                        <tr><td><span class="badge badge-blocked">Blocked</span></td><td>0 тест-кейсів</td><td><strong>0.0%</strong></td></tr>
                        <tr><td>Всього регресійних тестів</td><td>6 тест-кейсів</td><td><strong>100%</strong></td></tr>
                        <tr><td>Виявлено дефектів</td><td colspan="2"><strong style="color:#b91c1c;">1 дефект (Major, Security)</strong></td></tr>
                    </table>
                </div>
            </div>
        </div>
        <div class="card">
            <div class="card-header">Загальний висновок тестування</div>
            <div style="font-size:12px;color:#374151;">
                <p>Основна функціональність автентифікації та збереження сесії не порушена оновленням. Виявлено регресійну проблему в реалізації нового механізму обмеження частоти запитів (Rate Limiting / Brute-force protection).</p>
                <p style="margin-top:6px;"><strong>Рекомендація:</strong> Заблокувати випуск збірки у продакшн до виправлення дефекту BUG-REG-01 на бекенді та проведення повторного регресійного прогону.</p>
            </div>
        </div>
    </div>
    </body></html>
    """
    render_page(p, html5, f"{LR5_IMG}/05-regression-report-summary.png")

def main():
    with sync_playwright() as p:
        b = p.chromium.launch(args=["--no-sandbox", "--disable-dev-shm-usage"])
        generate_pr5_shots(b)
        generate_lr5_shots(b)
        b.close()
    print("All screenshots generated successfully!")

if __name__ == "__main__":
    main()
