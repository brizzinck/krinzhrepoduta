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

-- DOCX only: what report.css does for the PDF but a reference.docx style cannot reach.
local function docx_layout(doc)
  return doc:walk({
    -- Cell text is 12 pt with single spacing; plain Compact would inherit 1.5 from Normal.
    Table = function(tbl)
      local function restyle(cell)
        if #cell.contents == 0 then return cell end -- pandoc itself adds the required empty paragraph
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
    -- Forced page breaks written in report.md for the PDF. The tiny paragraph opens the next
    -- page by itself, so a full page cannot push it over and leave an empty one.
    Div = function(div)
      if (div.attributes.style or ""):match("break%-before:%s*page") then
        return pandoc.RawBlock("openxml", '<w:p><w:pPr><w:pageBreakBefore/>'
          .. '<w:spacing w:before="0" w:after="0" w:line="20" w:lineRule="exact"/>'
          .. '<w:rPr><w:sz w:val="2"/></w:rPr></w:pPr></w:p>')
      end
    end,
    -- .small listings are 8 pt in report.css. Pandoc always forces its own style on code
    -- blocks, so these become plain paragraphs (no syntax colours in the DOCX).
    CodeBlock = function(block)
      if not block.classes:includes("small") then return nil end
      local runs = {}
      for line in (block.text .. "\n"):gmatch("(.-)\n") do
        local text = line:gsub("&", "&amp;"):gsub("<", "&lt;"):gsub(">", "&gt;")
        table.insert(runs, '<w:r><w:t xml:space="preserve">' .. text .. "</w:t></w:r>")
      end
      return pandoc.RawBlock("openxml", '<w:p><w:pPr><w:pStyle w:val="SmallCode"/></w:pPr>'
        .. table.concat(runs, "<w:r><w:br/></w:r>") .. "</w:p>")
    end,
  })
end

function Pandoc(doc)
  if FORMAT:match("docx") then doc = docx_layout(doc) end
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

  local city = para("Title City", m["city-year"] and stringify(m["city-year"]) or "Київ – 2026")
  if FORMAT:match("docx") then
    -- The break lives in the last title paragraph: a separate paragraph would spill onto
    -- the next page of a full title page and leave it empty.
    table.insert(city.content[1].content, pandoc.RawInline("openxml", '<w:r><w:br w:type="page"/></w:r>'))
  end
  table.insert(bottom, city)

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
  if not FORMAT:match("docx") then
    table.insert(blocks, pandoc.RawBlock("html", '<div class="page-break"></div>'))
  end
  for _, b in ipairs(doc.blocks) do table.insert(blocks, b) end
  doc.blocks = blocks
  return doc
end
