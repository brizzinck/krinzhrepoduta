-- Report title page built from YAML metadata (same output for DOCX and HTML/PDF).
--
-- Fields (all strings; optional ones may be omitted):
--   ministry        ministry line (default "МІНІСТЕРСТВО ОСВІТИ І НАУКИ УКРАЇНИ"); false hides it
--   university      university name
--   institute       institute (optional)
--   department      department
--   report          e.g. "з лабораторної роботи № 5"
--   discipline      course name (without quotes)
--   topic           work topic (without quotes)
--   variant         e.g. "Варіант 2" (optional)
--   student-label   default "Виконав:"
--   student         list of lines: ["студент групи ТЦР-32", "Лаврішко В. С."]
--   teacher-label   "Перевірив:" or "Перевірила:"
--   teacher         list of lines
--   city-year       "Київ – 2026"
--   report-heading  replaces the word "Звіт", e.g. "ПРАКТИЧНА РОБОТА № 2.1" (optional)
--   topic-plain     true: topic without the "на тему: «…»" wrapper
-- A page break follows the title page. Other metadata (title etc.) is not printed.

local stringify = pandoc.utils.stringify

local function lines(value)
  if value == nil then return {} end
  if pandoc.utils.type(value) == "List" then
    local out = {}
    for _, item in ipairs(value) do table.insert(out, stringify(item)) end
    return out
  end
  return { stringify(value) }
end

local function para(style, text)
  local attr = pandoc.Attr("", { "tp", "tp-" .. style:lower():gsub(" ", "-") }, { ["custom-style"] = style })
  return pandoc.Div({ pandoc.Para(pandoc.Inlines(text)) }, attr)
end

local function page_break()
  if FORMAT:match("docx") then
    return pandoc.RawBlock("openxml", '<w:p><w:r><w:br w:type="page"/></w:r></w:p>')
  end
  return pandoc.RawBlock("html", '<div class="page-break"></div>')
end

function Pandoc(doc)
  local m = doc.meta
  if m.university == nil then return doc end

  local top, main, sign, bottom = {}, {}, {}, {}
  if m.ministry ~= false then
    table.insert(top, para("Title Top", m.ministry and stringify(m.ministry) or "МІНІСТЕРСТВО ОСВІТИ І НАУКИ УКРАЇНИ"))
  end
  table.insert(top, para("Title Org", stringify(m.university)))
  if m.institute then table.insert(top, para("Title Top", stringify(m.institute))) end
  if m.department then table.insert(top, para("Title Top", stringify(m.department))) end

  table.insert(main, para("Title Report", m["report-heading"] and stringify(m["report-heading"]) or "Звіт"))
  if m.report then table.insert(main, para("Title Line", stringify(m.report))) end
  if m.discipline then
    table.insert(main, para("Title Line", "з дисципліни «" .. stringify(m.discipline) .. "»"))
  end
  if m.topic then
    local t = stringify(m.topic)
    if m["topic-plain"] ~= true then t = "на тему: «" .. t .. "»" end
    table.insert(main, para("Title Topic", t))
  end
  if m.variant then table.insert(main, para("Title Line", stringify(m.variant))) end

  local first = true
  local function add_sign(text)
    table.insert(sign, para(first and "Title Sign First" or "Title Sign", text))
    first = false
  end
  add_sign(m["student-label"] and stringify(m["student-label"]) or "Виконав:")
  for _, l in ipairs(lines(m.student)) do add_sign(l) end
  if m.teacher then
    add_sign(m["teacher-label"] and stringify(m["teacher-label"]) or "Перевірив:")
    for _, l in ipairs(lines(m.teacher)) do add_sign(l) end
  end

  table.insert(bottom, para("Title City", m["city-year"] and stringify(m["city-year"]) or "Київ – 2026"))

  local title = {}
  local function group(cls, blocks)
    if FORMAT:match("docx") then
      for _, b in ipairs(blocks) do table.insert(title, b) end
    else
      table.insert(title, pandoc.Div(blocks, pandoc.Attr("", { cls })))
    end
  end
  group("tp-top", top)
  group("tp-main", main)
  group("tp-sign", sign)
  group("tp-bottom", bottom)

  local blocks = {}
  if FORMAT:match("docx") then
    for _, b in ipairs(title) do table.insert(blocks, b) end
  else
    table.insert(blocks, pandoc.Div(title, pandoc.Attr("", { "title-page" })))
  end
  table.insert(blocks, page_break())
  for _, b in ipairs(doc.blocks) do table.insert(blocks, b) end
  doc.blocks = blocks
  return doc
end
