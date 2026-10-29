import os
import re
import zipfile
import html
import xml.sax.saxutils as saxutils

def escape_xml(text):
    return saxutils.escape(str(text))

def build_docx(filename, title, doc_code, week_num, week_topic, deliverable_spec, md_content):
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>"""

    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""

    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    core_props = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>{escape_xml(title)}</dc:title>
  <dc:subject>DevOps 15-Week Capstone Engineering Deliverable</dc:subject>
  <dc:creator>DevOps Engineering Team</dc:creator>
  <cp:lastModifiedBy>DevOps Engineering Team</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">2026-10-28T12:00:00Z</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">2026-10-28T12:00:00Z</dcterms:modified>
</cp:coreProperties>"""

    app_props = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office Word</Application>
  <DocSecurity>0</DocSecurity>
  <Company>Automated Residential Visitor Gate-Pass System Project</Company>
</Properties>"""

    styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:color w:val="334155"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:after="140" w:line="276" w:lineRule="auto"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>

  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:pPr>
      <w:spacing w:before="360" w:after="140"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
      <w:b/>
      <w:color w:val="1E3A8A"/>
      <w:sz w:val="34"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:pPr>
      <w:spacing w:before="260" w:after="100"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
      <w:b/>
      <w:color w:val="2563EB"/>
      <w:sz w:val="28"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading3">
    <w:name w:val="heading 3"/>
    <w:pPr>
      <w:spacing w:before="200" w:after="80"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
      <w:b/>
      <w:color w:val="1D4ED8"/>
      <w:sz w:val="24"/>
    </w:rPr>
  </w:style>
</w:styles>"""

    # Helper to parse inline formatting into WordprocessingML runs
    def parse_inline_runs(text):
        runs_xml = []
        # Pattern to split by bold, code, and links
        tokens = re.split(r'(\*\*.*?\*\*|`.*?`|\[.*?\]\(.*?\))', text)
        for token in tokens:
            if not token:
                continue
            if token.startswith('**') and token.endswith('**') and len(token) >= 4:
                inner = token[2:-2]
                runs_xml.append(f'<w:r><w:rPr><w:b/><w:color w:val="0F172A"/></w:rPr><w:t xml:space="preserve">{escape_xml(inner)}</w:t></w:r>')
            elif token.startswith('`') and token.endswith('`') and len(token) >= 2:
                inner = token[1:-1]
                runs_xml.append(f'<w:r><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="20"/><w:szCs w:val="20"/><w:color w:val="0F172A"/><w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/></w:rPr><w:t xml:space="preserve">{escape_xml(inner)}</w:t></w:r>')
            elif token.startswith('[') and '](' in token and token.endswith(')'):
                match = re.match(r'\[(.*?)\]\((.*?)\)', token)
                if match:
                    label, url = match.groups()
                    runs_xml.append(f'<w:r><w:rPr><w:color w:val="2563EB"/><w:u w:val="single"/></w:rPr><w:t xml:space="preserve">{escape_xml(label)}</w:t></w:r>')
                else:
                    runs_xml.append(f'<w:r><w:t xml:space="preserve">{escape_xml(token)}</w:t></w:r>')
            else:
                runs_xml.append(f'<w:r><w:t xml:space="preserve">{escape_xml(token)}</w:t></w:r>')
        return "".join(runs_xml)

    # Process markdown lines
    body_elements = []

    # 1. Header Banner / Meta Box
    header_table = f"""<w:tbl>
  <w:tblPr>
    <w:tblW w:w="9360" w:type="dxa"/>
    <w:tblBorders>
      <w:top w:val="single" w:sz="12" w:space="0" w:color="1E3A8A"/>
      <w:left w:val="single" w:sz="12" w:space="0" w:color="1E3A8A"/>
      <w:bottom w:val="single" w:sz="12" w:space="0" w:color="1E3A8A"/>
      <w:right w:val="single" w:sz="12" w:space="0" w:color="1E3A8A"/>
      <w:insideH w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>
      <w:insideV w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>
    </w:tblBorders>
    <w:tblCellMar>
      <w:top w:w="140" w:type="dxa"/>
      <w:bottom w:w="140" w:type="dxa"/>
      <w:left w:w="180" w:type="dxa"/>
      <w:right w:w="180" w:type="dxa"/>
    </w:tblCellMar>
  </w:tblPr>
  <w:tr>
    <w:tc>
      <w:tcPr>
        <w:tcW w:w="9360" w:type="dxa"/>
        <w:gridSpan w:val="2"/>
        <w:shd w:val="clear" w:color="auto" w:fill="1E3A8A"/>
      </w:tcPr>
      <w:p>
        <w:pPr><w:jc w:val="center"/><w:spacing w:before="120" w:after="60"/></w:pPr>
        <w:r>
          <w:rPr><w:b/><w:sz w:val="32"/><w:szCs w:val="32"/><w:color w:val="FFFFFF"/></w:rPr>
          <w:t>Automated Residential Visitor Gate-Pass System</w:t>
        </w:r>
      </w:p>
      <w:p>
        <w:pPr><w:jc w:val="center"/><w:spacing w:after="120"/></w:pPr>
        <w:r>
          <w:rPr><w:sz w:val="22"/><w:szCs w:val="22"/><w:color w:val="93C5FD"/></w:rPr>
          <w:t>15-Week End-to-End DevOps Capstone Project | Curriculum Deliverable</w:t>
        </w:r>
      </w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="2600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/></w:tcPr>
      <w:p><w:r><w:rPr><w:b/><w:color w:val="1E293B"/></w:rPr><w:t>Curriculum Week:</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="6760" w:type="dxa"/></w:tcPr>
      <w:p><w:r><w:rPr><w:b/><w:color w:val="1D4ED8"/></w:rPr><w:t>Week {week_num}: {escape_xml(week_topic)}</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="2600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/></w:tcPr>
      <w:p><w:r><w:rPr><w:b/><w:color w:val="1E293B"/></w:rPr><w:t>Document Code:</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="6760" w:type="dxa"/></w:tcPr>
      <w:p><w:r><w:t>{escape_xml(doc_code)} (Release Baseline v1.0.0)</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
  <w:tr>
    <w:tc>
      <w:tcPr><w:tcW w:w="2600" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/></w:tcPr>
      <w:p><w:r><w:rPr><w:b/><w:color w:val="1E293B"/></w:rPr><w:t>Syllabus Deliverable:</w:t></w:r></w:p>
    </w:tc>
    <w:tc>
      <w:tcPr><w:tcW w:w="6760" w:type="dxa"/></w:tcPr>
      <w:p><w:r><w:rPr><w:i/><w:color w:val="0F172A"/></w:rPr><w:t>{escape_xml(deliverable_spec)}</w:t></w:r></w:p>
    </w:tc>
  </w:tr>
</w:tbl>
<w:p><w:pPr><w:spacing w:after="240"/></w:pPr></w:p>"""
    body_elements.append(header_table)

    lines = md_content.splitlines()
    in_code_block = False
    code_lines = []
    in_table = False
    table_lines = []

    def flush_table(tbl_lines):
        if not tbl_lines:
            return ""
        parsed_rows = []
        for l in tbl_lines:
            cells = [c.strip() for c in l.split('|')[1:-1]]
            if cells:
                # check if separator row like :--- or ---
                if all(re.match(r'^:?-+:?$', c) for c in cells):
                    continue
                parsed_rows.append(cells)
        if not parsed_rows:
            return ""
        
        num_cols = max(len(r) for r in parsed_rows)
        col_width = int(9360 / num_cols)

        tbl_xml = ['<w:tbl>', '<w:tblPr>', '<w:tblW w:w="9360" w:type="dxa"/>',
                   '<w:tblBorders>',
                   '<w:top w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '<w:left w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '<w:bottom w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '<w:right w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>',
                   '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>',
                   '</w:tblBorders>',
                   '<w:tblCellMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="160" w:type="dxa"/><w:right w:w="160" w:type="dxa"/></w:tblCellMar>',
                   '</w:tblPr>']
        
        for idx, row in enumerate(parsed_rows):
            tbl_xml.append('<w:tr>')
            is_header = (idx == 0)
            fill_color = "1E3A8A" if is_header else ("F8FAFC" if idx % 2 == 1 else "FFFFFF")
            text_color = "FFFFFF" if is_header else "1E293B"

            for cell in row:
                tbl_xml.append(f'<w:tc><w:tcPr><w:tcW w:w="{col_width}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="{fill_color}"/></w:tcPr>')
                tbl_xml.append('<w:p><w:pPr><w:spacing w:after="60" w:before="60"/></w:pPr>')
                if is_header:
                    tbl_xml.append(f'<w:r><w:rPr><w:b/><w:color w:val="{text_color}"/></w:rPr><w:t xml:space="preserve">{escape_xml(cell)}</w:t></w:r>')
                else:
                    runs = parse_inline_runs(cell)
                    tbl_xml.append(runs)
                tbl_xml.append('</w:p></w:tc>')
            tbl_xml.append('</w:tr>')
        tbl_xml.append('</w:tbl>')
        tbl_xml.append('<w:p><w:pPr><w:spacing w:after="160"/></w:pPr></w:p>')
        return "\n".join(tbl_xml)

    def flush_code(lines):
        if not lines:
            return ""
        code_xml = []
        for line in lines:
            code_xml.append('<w:p><w:pPr>')
            code_xml.append('<w:pBdr><w:left w:val="single" w:sz="18" w:space="15" w:color="2563EB"/></w:pBdr>')
            code_xml.append('<w:shd w:val="clear" w:color="auto" w:fill="F1F5F9"/>')
            code_xml.append('<w:spacing w:before="40" w:after="40" w:line="220" w:lineRule="auto"/>')
            code_xml.append('<w:ind w:left="240" w:right="240"/>')
            code_xml.append('</w:pPr>')
            code_xml.append(f'<w:r><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="19"/><w:szCs w:val="19"/><w:color w:val="0F172A"/></w:rPr><w:t xml:space="preserve">{escape_xml(line)}</w:t></w:r>')
            code_xml.append('</w:p>')
        code_xml.append('<w:p><w:pPr><w:spacing w:after="140"/></w:pPr></w:p>')
        return "\n".join(code_xml)

    for line in lines:
        stripped = line.strip()

        # Check code block fences
        if stripped.startswith('```'):
            if in_code_block:
                body_elements.append(flush_code(code_lines))
                code_lines = []
                in_code_block = False
            else:
                if in_table:
                    body_elements.append(flush_table(table_lines))
                    table_lines = []
                    in_table = False
                in_code_block = True
                code_lines = []
            continue

        if in_code_block:
            code_lines.append(line)
            continue

        # Check table lines
        if stripped.startswith('|') and stripped.endswith('|'):
            in_table = True
            table_lines.append(stripped)
            continue
        elif in_table:
            body_elements.append(flush_table(table_lines))
            table_lines = []
            in_table = False

        if not stripped:
            continue

        # Horizontal rules
        if re.match(r'^-{3,}$', stripped) or re.match(r'^\*{3,}$', stripped):
            body_elements.append('<w:p><w:pPr><w:pBdr><w:bottom w:val="single" w:sz="6" w:space="1" w:color="CBD5E1"/></w:pBdr><w:spacing w:before="120" w:after="160"/></w:pPr></w:p>')
            continue

        # Headings
        if stripped.startswith('# '):
            h_text = stripped[2:].strip()
            # Skip repeating main document title if redundant
            if "Week " in h_text and week_topic in h_text:
                continue
            body_elements.append(f'<w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>{escape_xml(h_text)}</w:t></w:r></w:p>')
        elif stripped.startswith('## '):
            h_text = stripped[3:].strip()
            body_elements.append(f'<w:p><w:pPr><w:pStyle w:val="Heading2"/></w:pPr><w:r><w:t>{escape_xml(h_text)}</w:t></w:r></w:p>')
        elif stripped.startswith('### '):
            h_text = stripped[4:].strip()
            body_elements.append(f'<w:p><w:pPr><w:pStyle w:val="Heading3"/></w:pPr><w:r><w:t>{escape_xml(h_text)}</w:t></w:r></w:p>')
        elif stripped.startswith('#### '):
            h_text = stripped[5:].strip()
            body_elements.append(f'<w:p><w:pPr><w:spacing w:before="160" w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="1E293B"/></w:rPr><w:t>{escape_xml(h_text)}</w:t></w:r></w:p>')
        # Bullet list items
        elif stripped.startswith('- ') or stripped.startswith('* '):
            item_text = stripped[2:].strip()
            runs = parse_inline_runs(item_text)
            body_elements.append(f'<w:p><w:pPr><w:ind w:left="480" w:hanging="240"/><w:spacing w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="2563EB"/></w:rPr><w:t>• </w:t></w:r>{runs}</w:p>')
        # Numbered list items
        elif re.match(r'^\d+\.\s', stripped):
            num_match = re.match(r'^(\d+\.)\s*(.*)', stripped)
            prefix = num_match.group(1)
            item_text = num_match.group(2)
            runs = parse_inline_runs(item_text)
            body_elements.append(f'<w:p><w:pPr><w:ind w:left="480" w:hanging="240"/><w:spacing w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="1E3A8A"/></w:rPr><w:t>{escape_xml(prefix)} </w:t></w:r>{runs}</w:p>')
        # Blockquote / Notes
        elif stripped.startswith('> '):
            quote_text = stripped[2:].strip()
            runs = parse_inline_runs(quote_text)
            body_elements.append(f'<w:p><w:pPr><w:pBdr><w:left w:val="single" w:sz="24" w:space="15" w:color="F59E0B"/></w:pBdr><w:shd w:val="clear" w:color="auto" w:fill="FEF3C7"/><w:ind w:left="240" w:right="240"/><w:spacing w:before="60" w:after="80"/></w:pPr>{runs}</w:p>')
        else:
            # Regular paragraph
            runs = parse_inline_runs(stripped)
            body_elements.append(f'<w:p><w:pPr><w:spacing w:after="120"/></w:pPr>{runs}</w:p>')

    if in_table:
        body_elements.append(flush_table(table_lines))
    if in_code_block:
        body_elements.append(flush_code(code_lines))

    # Add Section Properties (Letter, Margins, Header/Footer spacing)
    section_xml = """<w:sectPr>
  <w:pgSz w:w="12240" w:h="15840"/>
  <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
</w:sectPr>"""

    doc_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    {"".join(body_elements)}
    {section_xml}
  </w:body>
</w:document>"""

    os.makedirs(os.path.dirname(os.path.abspath(filename)), exist_ok=True)
    with zipfile.ZipFile(filename, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types)
        docx.writestr('_rels/.rels', rels)
        docx.writestr('docProps/core.xml', core_props)
        docx.writestr('docProps/app.xml', app_props)
        docx.writestr('word/_rels/document.xml.rels', doc_rels)
        docx.writestr('word/styles.xml', styles)
        docx.writestr('word/document.xml', doc_xml)

    print(f"Generated: {filename} ({os.path.getsize(filename)} bytes)")

WEEKS_METADATA = [
    {
        "week": 1,
        "topic": "Problem Definition and Scope",
        "doc_code": "DOC-W01-PDS",
        "file_name": "Week_01_Problem_Definition_and_Scope.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/01_problem_definition.md",
        "deliverable": "Problem statement, stakeholder list, objectives, constraints and approved MVP scope."
    },
    {
        "week": 2,
        "topic": "Agile Planning and DevOps Workflow",
        "doc_code": "DOC-W02-APW",
        "file_name": "Week_02_Agile_Planning_and_DevOps_Workflow.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/02_agile_planning.md",
        "deliverable": "Backlog, task board, sprint plan, Definition of Done and DevOps workflow diagram."
    },
    {
        "week": 3,
        "topic": "Requirements, Architecture and Technology Setup",
        "doc_code": "DOC-W03-RAT",
        "file_name": "Week_03_Requirements_Architecture_and_Technology_Setup.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/03_requirements_architecture.md",
        "deliverable": "SRS summary, use-case diagram, architecture diagram, data model/API list and working local setup."
    },
    {
        "week": 4,
        "topic": "Git and GitHub Repository Initialization",
        "doc_code": "DOC-W04-GGW",
        "file_name": "Week_04_Git_and_GitHub_Repository_Initialization.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/04_git_github_workflow.md",
        "deliverable": "Repository URL, README, initial commits, issues and branch policy."
    },
    {
        "week": 5,
        "topic": "Feature Development with Branching",
        "doc_code": "DOC-W05-FDB",
        "file_name": "Week_05_Feature_Development_with_Branching.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/05_feature_development.md",
        "deliverable": "Working feature 1, feature branch, pull request, review comments and merge evidence."
    },
    {
        "week": 6,
        "topic": "MVP Completion and Git Collaboration",
        "doc_code": "DOC-W06-MVP",
        "file_name": "Week_06_MVP_Completion_and_Git_Collaboration.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/06_mvp_implementation.md",
        "deliverable": "Functional MVP, resolved merge conflict, tagged version and updated backlog."
    },
    {
        "week": 7,
        "topic": "Jenkins Installation and Continuous Integration Job",
        "doc_code": "DOC-W07-JCI",
        "file_name": "Week_07_Jenkins_Installation_and_Continuous_Integration_Job.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/07_jenkins_ci.md",
        "deliverable": "Configured Jenkins job, successful build log, trigger evidence and archived artefact."
    },
    {
        "week": 8,
        "topic": "Pipeline as Code and Server Deployment",
        "doc_code": "DOC-W08-PAC",
        "file_name": "Week_08_Pipeline_as_Code_and_Server_Deployment.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/08_pipeline_deployment.md",
        "deliverable": "Jenkinsfile, successful pipeline run, deployed application URL/screenshot and configuration evidence."
    },
    {
        "week": 9,
        "topic": "Selenium Test Design and Local Execution",
        "doc_code": "DOC-W09-STD",
        "file_name": "Week_09_Selenium_Test_Design_and_Local_Execution.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/09_selenium_testing.md",
        "deliverable": "Test plan, Selenium scripts, local test report and failure screenshot mechanism."
    },
    {
        "week": 10,
        "topic": "Continuous Testing in Jenkins",
        "doc_code": "DOC-W10-CTJ",
        "file_name": "Week_10_Continuous_Testing_in_Jenkins.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/10_continuous_testing.md",
        "deliverable": "Jenkins test report, failed pipeline evidence, defect correction commit and successful rerun."
    },
    {
        "week": 11,
        "topic": "Docker Image and Container Lifecycle",
        "doc_code": "DOC-W11-DIC",
        "file_name": "Week_11_Docker_Image_and_Container_Lifecycle.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/11_docker_lifecycle.md",
        "deliverable": "Dockerfile, image details, Docker command log and running container evidence."
    },
    {
        "week": 12,
        "topic": "Jenkins-Docker Continuous Deployment",
        "doc_code": "DOC-W12-JDD",
        "file_name": "Week_12_Jenkins_Docker_Continuous_Deployment.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/12_docker_cd.md",
        "deliverable": "Versioned image, registry evidence and end-to-end commit-to-container pipeline."
    },
    {
        "week": 13,
        "topic": "Configuration Management Script",
        "doc_code": "DOC-W13-CMS",
        "file_name": "Week_13_Configuration_Management_Script.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/13_configuration_management.md",
        "deliverable": "Configuration specification, Puppet manifest or Ansible playbook and first execution log."
    },
    {
        "week": 14,
        "topic": "Automated Provisioning and Reliability Validation",
        "doc_code": "DOC-W14-APR",
        "file_name": "Week_14_Automated_Provisioning_and_Reliability_Validation.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/14_provisioning_reliability.md",
        "deliverable": "Provisioned node, idempotency evidence, health-check result and rollback/recovery demonstration."
    },
    {
        "week": 15,
        "topic": "Final End-to-End Release, Documentation and Viva",
        "doc_code": "DOC-W15-FRV",
        "file_name": "Week_15_Final_End_to_End_Release_Documentation_and_Viva.docx",
        "md_file": "d:/devops/visitor-gate-pass/docs/15_final_release_viva.md",
        "deliverable": "Final repository, live demonstration, complete report, screenshots/video, presentation and viva."
    }
]

def main():
    dest_dir_devops = "d:/devops/weekly_word_documents"
    dest_dir_subfolder = "d:/devops/visitor-gate-pass/docs/word_documents"
    
    os.makedirs(dest_dir_devops, exist_ok=True)
    os.makedirs(dest_dir_subfolder, exist_ok=True)

    for item in WEEKS_METADATA:
        md_path = item["md_file"]
        if not os.path.exists(md_path):
            print(f"Warning: {md_path} not found!")
            continue
        
        with open(md_path, 'r', encoding='utf-8') as f:
            content = f.read()

        title = f"Week {item['week']}: {item['topic']}"

        # 1. Output to d:/devops/weekly_word_documents/
        target_path_devops = os.path.join(dest_dir_devops, item["file_name"])
        build_docx(target_path_devops, title, item["doc_code"], item["week"], item["topic"], item["deliverable"], content)

        # 2. Output to d:/devops/visitor-gate-pass/docs/word_documents/
        target_path_subfolder = os.path.join(dest_dir_subfolder, item["file_name"])
        build_docx(target_path_subfolder, title, item["doc_code"], item["week"], item["topic"], item["deliverable"], content)

    print("\nAll 15 Word documents generated successfully in both locations!")

if __name__ == '__main__':
    main()
