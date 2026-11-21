-- Coursework note: title page, task sheet, table of contents and table captions.
-- Same filter for DOCX and HTML/PDF.
--
-- Metadata (strings; lists where noted):
--   ministry, university, department   title page head lines
--   discipline, topic                  "з дисципліни «…»" and "на тему «…»"
--   student-name, group                "Виконав: студент групи <group>, <student-name>"
--   supervisor-label, supervisor       list of lines, default label "Керівник курсової роботи:"
--   city-year                          "Київ – 2026"
--   task-initial                       list: initial data of the task sheet
--   task-contents                      list: contents of the note (a default outline is built in)
--   task-issued, task-due              dates on the task sheet
--
-- Body markup:
--   ::: toc          the table of contents (headings of levels 1-3 without the class .unlisted)
--   ::: tcap         two paragraphs: "Таблиця 1.1" and the table title, put right above the table
--   ::: {.pair}      two images in a row with a caption (as in the report filter)
-- Environment (set by build.sh, HTML/PDF only):
--   KR_TOC_OUT       write "index<TAB>level<TAB>text" of every listed heading to this file
--   KR_TOC_PAGES     read "index<TAB>page" lines from this file; without it the page numbers read 00
-- The DOCX contents is built by kr-docx-post.py from the headings of the finished DOCX.

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

local function esc(text)
  return (text:gsub("&", "&amp;"):gsub("<", "&lt;"):gsub(">", "&gt;"))
end

local function para(style, text)
  local attr = pandoc.Attr("", { "tp", "tp-" .. style:lower():gsub(" ", "-") }, { ["custom-style"] = style })
  return pandoc.Div({ pandoc.Para(pandoc.Inlines(text)) }, attr)
end

local DEFAULT_CONTENTS = {
  "Вступ",
  "Стратегія автоматизації предметної області",
  "Аналіз предметної області",
  "Концептуальне моделювання предметної області",
  "Логічне та фізичне проектування бази даних",
  "Проектування інформаційної системи",
  "Висновки",
  "Список використаних джерел",
  "Додатки",
}

-- DOCX only: what kr.css does for the PDF but a reference style cannot reach.
local function docx_layout(doc)
  return doc:walk({
    Table = function(tbl)
      local function restyle(cell)
        if #cell.contents == 0 then return cell end
        local blocks = pandoc.List()
        for _, b in ipairs(cell.contents) do
          blocks:insert(b.t == "Plain" and pandoc.Para(b.content) or b)
        end
        cell.contents = { pandoc.Div(blocks, pandoc.Attr("", {}, { ["custom-style"] = "Table Text" })) }
        return cell
      end
      local function each_row(rows)
        for _, row in ipairs(rows) do
          for i, cell in ipairs(row.cells) do row.cells[i] = restyle(cell) end
        end
      end
      each_row(tbl.head.rows)
      for _, body in ipairs(tbl.bodies) do
        each_row(body.head)
        each_row(body.body)
      end
      each_row(tbl.foot.rows)
      return tbl
    end,
    Header = function(h)
      -- Unlisted sub-subsections look like heading 3 but stay out of the outline and the contents.
      if h.classes:includes("unlisted") and h.level == 3 then
        return pandoc.Div({ pandoc.Para(h.content) },
          pandoc.Attr("", {}, { ["custom-style"] = "Subheading Unlisted" }))
      end
    end,
    Div = function(div)
      if div.classes:includes("formula") and #div.content >= 2 then
        -- Two paragraphs (formula, number) become one line: centre tab, formula, right tab, number.
        local tab = pandoc.RawInline("openxml", "<w:r><w:tab/></w:r>")
        local inl = pandoc.Inlines({ tab })
        inl:extend(div.content[1].content)
        inl:insert(tab)
        inl:extend(div.content[2].content)
        return pandoc.Div({ pandoc.Para(inl) }, pandoc.Attr("", {}, { ["custom-style"] = "Formula" }))
      end
      if div.classes:includes("tcap") then
        local out = {}
        for i, b in ipairs(div.content) do
          local style = i == 1 and "Table Number" or "Table Title"
          table.insert(out, pandoc.Div({ b }, pandoc.Attr("", {}, { ["custom-style"] = style })))
        end
        return out
      end
      if div.classes:includes("pair") then
        local out = {}
        for _, b in ipairs(div.content) do
          local style = "Image Caption"
          if b.t == "Para" then
            for _, el in ipairs(b.content) do
              if el.t == "Image" then
                style = "Figure"
                -- Two 48 % images plus a space are a hair too wide for one line in Word and LibreOffice.
                local w = el.attributes.width
                if w and w:match("^(%d+)%%$") and tonumber(w:match("^(%d+)")) >= 48 and #b.content > 2 then
                  el.attributes.width = "46%"
                end
              end
            end
          end
          table.insert(out, pandoc.Div({ b }, pandoc.Attr("", {}, { ["custom-style"] = style })))
        end
        return out
      end
    end,
    CodeBlock = function(block)
      if not block.classes:includes("small") then return nil end
      local runs = {}
      for line in (block.text .. "\n"):gmatch("(.-)\n") do
        table.insert(runs, '<w:r><w:t xml:space="preserve">' .. esc(line) .. "</w:t></w:r>")
      end
      return pandoc.RawBlock("openxml", '<w:p><w:pPr><w:pStyle w:val="SmallCode"/></w:pPr>'
        .. table.concat(runs, "<w:r><w:br/></w:r>") .. "</w:p>")
    end,
  })
end

-- Listed headings in document order: { level, text }.
local function collect_headings(doc)
  local list = {}
  doc:walk({
    Header = function(h)
      if h.level <= 3 and not h.classes:includes("unlisted") then
        table.insert(list, { level = h.level, text = stringify(h.content) })
      end
    end,
  })
  return list
end

local function read_pages()
  local path = os.getenv("KR_TOC_PAGES")
  local pages = {}
  if not path or path == "" then return pages end
  local f = io.open(path, "r")
  if not f then return pages end
  for line in f:lines() do
    local i, p = line:match("^(%d+)\t(%d+)$")
    if i then pages[tonumber(i)] = p end
  end
  f:close()
  return pages
end

local function write_headings(list)
  local path = os.getenv("KR_TOC_OUT")
  if not path or path == "" then return end
  local f = assert(io.open(path, "w"))
  for i, h in ipairs(list) do f:write(i, "\t", h.level, "\t", h.text, "\n") end
  f:close()
end

local function toc_block(list, pages)
  if FORMAT:match("docx") then
    -- The heading and a marker paragraph: kr-docx-post.py builds the TOC field (entries, links, pages of the DOCX itself).
    return pandoc.RawBlock("openxml",
      '<w:p><w:pPr><w:pStyle w:val="TOCHeading"/></w:pPr><w:r><w:t>Зміст</w:t></w:r></w:p>'
      .. '<w:p><w:pPr><w:pStyle w:val="TOC1"/></w:pPr><w:r><w:t>KR-TOC-MARKER</w:t></w:r></w:p>')
  end
  local html = { '<div class="toc"><h1 class="unlisted toc-title">Зміст</h1>' }
  for i, h in ipairs(list) do
    table.insert(html, '<div class="toc-row toc-' .. h.level .. '"><span class="t">' .. esc(h.text)
      .. '</span><span class="l"></span><span class="n">' .. (pages[i] or "00") .. '</span></div>')
  end
  table.insert(html, "</div>")
  return pandoc.RawBlock("html", table.concat(html))
end

local function front_matter(m)
  local title, sheet = {}, {}
  local docx = FORMAT:match("docx")

  -- Title page
  local top, main, sign = {}, {}, {}
  table.insert(top, para("Title Top", m.ministry and stringify(m.ministry) or "МІНІСТЕРСТВО ОСВІТИ І НАУКИ УКРАЇНИ"))
  table.insert(top, para("Title Org", stringify(m.university)))
  if m.department then table.insert(top, para("Title Top", stringify(m.department))) end

  table.insert(main, para("Title Report", "Курсова робота"))
  if m.discipline then
    table.insert(main, para("Title Line", "з дисципліни «" .. stringify(m.discipline) .. "»"))
  end
  if m.topic then table.insert(main, para("Title Topic", "на тему «" .. stringify(m.topic) .. "»")) end

  local first = true
  local function add_sign(text, gap)
    local style = first and "Title Sign First" or (gap and "Title Sign Gap" or "Title Sign")
    table.insert(sign, para(style, text))
    first = false
  end
  add_sign(m["student-label"] and stringify(m["student-label"]) or "Виконав:")
  if m.group then add_sign("студент групи " .. stringify(m.group)) end
  if m["student-name"] then add_sign(stringify(m["student-name"])) end
  if m.supervisor then
    add_sign(m["supervisor-label"] and stringify(m["supervisor-label"]) or "Керівник курсової роботи:", true)
    for _, l in ipairs(lines(m.supervisor)) do add_sign(l) end
  end
  local city = { para("Title City", m["city-year"] and stringify(m["city-year"]) or "Київ – 2026") }

  local function group(cls, blocks)
    if docx then
      for _, b in ipairs(blocks) do table.insert(title, b) end
    else
      table.insert(title, pandoc.Div(blocks, pandoc.Attr("", { cls })))
    end
  end
  group("tp-top", top)
  group("tp-main", main)
  group("tp-sign", sign)
  group("tp-bottom", city)

  -- Task sheet (Q2: own one-page form)
  local student_line = "Студент: " .. (m["student-name"] and stringify(m["student-name"]) or "")
    .. (m.group and (", група " .. stringify(m.group)) or "")
  table.insert(sheet, para("Task Heading", "Завдання на курсове проектування"))
  if m.discipline then
    table.insert(sheet, para("Task Line", "з дисципліни «" .. stringify(m.discipline) .. "»"))
  end
  table.insert(sheet, para("Task Line", student_line))
  if m.topic then table.insert(sheet, para("Task Line", "Тема роботи: «" .. stringify(m.topic) .. "»")) end
  local initial = lines(m["task-initial"])
  if #initial > 0 then
    table.insert(sheet, para("Task Label", "Вихідні дані:"))
    for i, t in ipairs(initial) do table.insert(sheet, para("Task Item", i .. ". " .. t)) end
  end
  table.insert(sheet, para("Task Label", "Зміст пояснювальної записки:"))
  local contents = m["task-contents"] and lines(m["task-contents"]) or DEFAULT_CONTENTS
  -- A list given in the metadata is printed as written (it carries the numbers of the note); the default one is numbered.
  local given = m["task-contents"] ~= nil
  for i, t in ipairs(contents) do table.insert(sheet, para("Task Item", given and t or (i .. ". " .. t))) end
  if m["task-issued"] then table.insert(sheet, para("Task Line", "Дата видачі завдання: " .. stringify(m["task-issued"]))) end
  if m["task-due"] then table.insert(sheet, para("Task Line", "Термін подання роботи: " .. stringify(m["task-due"]))) end
  local sup = m.supervisor and lines(m.supervisor) or {}
  table.insert(sheet, para("Task Sign", "Керівник курсової роботи ______________ " .. (sup[#sup] or "")))
  table.insert(sheet, para("Task Sign", "Завдання отримав ______________ " .. (m["student-name"] and stringify(m["student-name"]) or "")))

  local blocks = {}
  if docx then
    for _, b in ipairs(title) do table.insert(blocks, b) end
    for _, b in ipairs(sheet) do table.insert(blocks, b) end
    -- Close section 1 (title page and task sheet): it has no header, so no page number.
    table.insert(blocks, pandoc.RawBlock("openxml",
      '<w:p><w:pPr><w:spacing w:before="0" w:after="0" w:line="20" w:lineRule="exact"/><w:sectPr>'
      .. '<w:pgSz w:w="11906" w:h="16838"/>'
      .. '<w:pgMar w:top="1134" w:right="567" w:bottom="1134" w:left="1134" w:header="567" w:footer="567" w:gutter="0"/>'
      .. '</w:sectPr></w:pPr></w:p>'))
  else
    table.insert(blocks, pandoc.Div(title, pandoc.Attr("", { "title-page" })))
    table.insert(blocks, pandoc.Div(sheet, pandoc.Attr("", { "task-sheet" })))
  end
  return blocks
end

function Pandoc(doc)
  if FORMAT:match("docx") then doc = docx_layout(doc) end
  local m = doc.meta

  local headings = collect_headings(doc)
  write_headings(headings)
  local pages = read_pages()
  doc = doc:walk({
    Div = function(div)
      if div.classes:includes("toc") then return toc_block(headings, pages) end
    end,
  })

  if m.university == nil then return doc end
  local blocks = front_matter(m)
  for _, b in ipairs(doc.blocks) do table.insert(blocks, b) end
  doc.blocks = blocks
  return doc
end
