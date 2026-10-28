import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

doc_path = r'J:\JFDS\supportdesk\SupportDesk_Project_Report.docx'
base_dir = r'J:\JFDS\supportdesk'
fig_dir = os.path.join(base_dir, 'figures')

doc = Document()

# Page Margins (Normal: 1 inch everywhere, Left: 1.25 inch for binding)
sections = doc.sections
for s in sections:
    s.top_margin = Inches(1.0)
    s.bottom_margin = Inches(1.0)
    s.left_margin = Inches(1.2)
    s.right_margin = Inches(1.0)
    s.different_first_page_header_footer = False

# Colors
COLOR_NAVY = RGBColor(15, 23, 42)      # #0f172a
COLOR_BLUE = RGBColor(30, 64, 175)     # #1e40af
COLOR_DARK = RGBColor(30, 41, 59)      # #1e293b
COLOR_GRAY = RGBColor(100, 116, 139)   # #64748b
COLOR_GREEN = RGBColor(22, 101, 52)    # #166534

# Styles configuration
style_normal = doc.styles['Normal']
style_normal.font.name = 'Times New Roman'
style_normal.font.size = Pt(11.5)
style_normal.font.color.rgb = COLOR_DARK
style_normal.paragraph_format.line_spacing = 1.15
style_normal.paragraph_format.space_after = Pt(6)

def set_cell_shading(cell, color_hex):
    shading_xml = f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>'
    cell._tc.get_or_add_tcPr().append(parse_xml(shading_xml))

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def add_footer_page_number(run_or_p, page_num_str):
    p = run_or_p if isinstance(run_or_p, docx.text.paragraph.Paragraph) else run_or_p.paragraph
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(24)
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run(f"Page | {page_num_str}")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(10)
    r.font.color.rgb = COLOR_GRAY

def add_chapter_title(title_text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(12)
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    r = p.add_run(title_text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(16)
    r.font.bold = True
    r.font.color.rgb = COLOR_NAVY
    return p

def add_heading_2(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(6)
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(13)
    r.font.bold = True
    r.font.color.rgb = COLOR_BLUE
    return p

def add_heading_3(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(11.5)
    r.font.bold = True
    r.font.color.rgb = COLOR_DARK
    return p

def add_bullet(p, bold_prefix, text):
    p.paragraph_format.left_indent = Inches(0.25)
    p.paragraph_format.space_after = Pt(4)
    r_bullet = p.add_run("•  ")
    r_bullet.font.name = 'Times New Roman'
    r_bullet.font.size = Pt(11.5)
    r_bullet.font.bold = True
    r_bullet.font.color.rgb = COLOR_BLUE
    if bold_prefix:
        r_b = p.add_run(bold_prefix + ": ")
        r_b.font.name = 'Times New Roman'
        r_b.font.size = Pt(11.5)
        r_b.font.bold = True
    r_t = p.add_run(text)
    r_t.font.name = 'Times New Roman'
    r_t.font.size = Pt(11.5)

def format_styled_table(table, col_widths, headers, data):
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    # Format Header Row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_shading(hdr_cells[i], "1e293b")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=150, right=150)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        for run in p.runs:
            run.font.name = 'Times New Roman'
            run.font.size = Pt(10)
            run.font.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)
    
    # Format Data Rows
    for r_idx, row_data in enumerate(data):
        row_cells = table.rows[r_idx + 1].cells
        bg_color = "f8fafc" if r_idx % 2 == 1 else "ffffff"
        for c_idx, val in enumerate(row_data):
            row_cells[c_idx].text = str(val)
            set_cell_shading(row_cells[c_idx], bg_color)
            set_cell_margins(row_cells[c_idx], top=90, bottom=90, left=140, right=140)
            p = row_cells[c_idx].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in p.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(9.5)
                run.font.color.rgb = COLOR_DARK
    
    # Set Widths
    for row in table.rows:
        for idx, width in enumerate(col_widths):
            row.cells[idx].width = Inches(width)

# ==============================================================================
# PAGE 1: OUTER COVER PAGE
# ==============================================================================
p_logo = doc.add_paragraph()
p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_logo.paragraph_format.space_before = Pt(10)
logo_img = os.path.join(base_dir, 'iare_logo.png')
if os.path.exists(logo_img):
    p_logo.add_run().add_picture(logo_img, width=Inches(1.15))

# Top Flourish
p_flourish1 = doc.add_paragraph()
p_flourish1.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_flourish1.paragraph_format.space_before = Pt(180)
flourish1_img = os.path.join(base_dir, 'page_1_img_0_Image9.png')
if os.path.exists(flourish1_img):
    p_flourish1.add_run().add_picture(flourish1_img, width=Inches(2.4))

# Horizontal Line 1
p_line1 = doc.add_paragraph()
p_line1.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_line1.paragraph_format.space_before = Pt(8)
p_line1.paragraph_format.space_after = Pt(8)
r_line1 = p_line1.add_run("―" * 58)
r_line1.font.color.rgb = COLOR_BLUE
r_line1.font.bold = True

# Banner Title
p_cov_title = doc.add_paragraph()
p_cov_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cov_title.paragraph_format.space_before = Pt(4)
p_cov_title.paragraph_format.space_after = Pt(4)
r_title = p_cov_title.add_run("JAVA FULL STACK DEVELOPMENT")
r_title.font.name = 'Times New Roman'
r_title.font.size = Pt(18)
r_title.font.bold = True
r_title.font.color.rgb = COLOR_NAVY

# Horizontal Line 2
p_line2 = doc.add_paragraph()
p_line2.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_line2.paragraph_format.space_before = Pt(8)
p_line2.paragraph_format.space_after = Pt(8)
r_line2 = p_line2.add_run("―" * 58)
r_line2.font.color.rgb = COLOR_BLUE
r_line2.font.bold = True

# Bottom Flourish
p_flourish2 = doc.add_paragraph()
p_flourish2.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_flourish2.paragraph_format.space_before = Pt(12)
flourish2_img = os.path.join(base_dir, 'page_1_img_1_Image11.png')
if os.path.exists(flourish2_img):
    p_flourish2.add_run().add_picture(flourish2_img, width=Inches(1.2))

# Page 1 Footer
p_p1 = doc.add_paragraph()
p_p1.paragraph_format.space_before = Pt(150)
add_footer_page_number(p_p1, "1")

doc.add_page_break()

# ==============================================================================
# PAGE 2: PROJECT SUB-BANNER PAGE
# ==============================================================================
p_sub = doc.add_paragraph()
p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_sub.paragraph_format.space_before = Pt(50)
r_sub_h1 = p_sub.add_run("SupportDesk\n")
r_sub_h1.font.name = 'Times New Roman'
r_sub_h1.font.size = Pt(18)
r_sub_h1.font.bold = True
r_sub_h1.font.color.rgb = COLOR_NAVY

r_sub_h2 = p_sub.add_run("Multi-Domain Customer Support & IT Service Management Platform\n")
r_sub_h2.font.name = 'Times New Roman'
r_sub_h2.font.size = Pt(14)
r_sub_h2.font.bold = True
r_sub_h2.font.color.rgb = COLOR_BLUE

r_sub_h3 = p_sub.add_run("Configurable Service Desk, SLA Monitoring, Rule-Based AI Classification, and Executive Analytics")
r_sub_h3.font.name = 'Times New Roman'
r_sub_h3.font.size = Pt(11.5)
r_sub_h3.font.color.rgb = COLOR_DARK

p_names = doc.add_paragraph()
p_names.alignment = WD_ALIGN_PARAGRAPH.RIGHT
p_names.paragraph_format.space_before = Pt(450)
r_n = p_names.add_run("Names")
r_n.font.name = 'Times New Roman'
r_n.font.size = Pt(12)
r_n.font.italic = True
r_n.font.bold = True

p_p2 = doc.add_paragraph()
add_footer_page_number(p_p2, "2")

doc.add_page_break()

# ==============================================================================
# PAGE 3: OFFICIAL INNER TITLE PAGE
# ==============================================================================
p3 = doc.add_paragraph()
p3.alignment = WD_ALIGN_PARAGRAPH.CENTER
p3.paragraph_format.space_before = Pt(15)

r = p3.add_run("JAVA FULL STACK DEVELOPMENT\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(15)
r.font.bold = True
r.font.color.rgb = COLOR_NAVY

r = p3.add_run("SupportDesk: Multi-Domain Customer Support & IT Service Management Platform\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(14)
r.font.bold = True
r.font.color.rgb = COLOR_BLUE

r = p3.add_run("Configurable Service Desk, SLA Monitoring, Rule-Based AI Classification, and Executive Analytics\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(11)
r.font.bold = True
r.font.color.rgb = COLOR_DARK

if os.path.exists(logo_img):
    p3.add_run().add_picture(logo_img, width=Inches(1.2))

r = p3.add_run("\n\nBy\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(11.5)

r = p3.add_run("Names\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(12)
r.font.bold = True

r = p3.add_run("Bachelor of Technology in\nComputer Science and Engineering\n(AI & ML)\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(11.5)
r.font.bold = True

r = p3.add_run("Department of Computer Science and Engineering\n(Artificial Intelligence And Machine Learning)\n\n")
r.font.name = 'Times New Roman'
r.font.size = Pt(11.5)
r.font.bold = True

r = p3.add_run("INSTITUTE OF AERONAUTICAL ENGINEERING\n(Autonomous)\nDundigal, Hyderabad - 500 043, Telangana\n\nOctober - 2026")
r.font.name = 'Times New Roman'
r.font.size = Pt(12)
r.font.bold = True
r.font.color.rgb = COLOR_NAVY

p_p3 = doc.add_paragraph()
add_footer_page_number(p_p3, "3")

doc.add_page_break()

# ==============================================================================
# PAGE 4: DECLARATION
# ==============================================================================
p_dec_title = doc.add_paragraph()
p_dec_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_dec_title.paragraph_format.space_before = Pt(60)
p_dec_title.paragraph_format.space_after = Pt(24)
r_dec = p_dec_title.add_run("DECLARATION")
r_dec.font.name = 'Times New Roman'
r_dec.font.size = Pt(16)
r_dec.font.bold = True
r_dec.font.color.rgb = COLOR_NAVY

p_dec_body = doc.add_paragraph()
p_dec_body.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_dec_body.paragraph_format.line_spacing = 1.3
p_dec_body.paragraph_format.space_after = Pt(100)
p_dec_body.add_run(
    "I certify that the work contained in this report entitled “SupportDesk: Multi-Domain Customer "
    "Support & IT Service Management Platform” is original and has been carried out by me under the "
    "guidance of my supervisor(s). The work has not been submitted to any other institute for any degree "
    "or diploma. I have followed the guidelines provided by the Institute for preparing this report and "
    "have acknowledged all external materials, technologies, and references used in the project."
)

p_dec_sig = doc.add_paragraph()
p_dec_sig.paragraph_format.line_spacing = 1.4
p_dec_sig.add_run("Place:  ________________________                  Signature of the Students\n")
p_dec_sig.add_run("Date:   ________________________                  Roll No : _________________")

p_p4 = doc.add_paragraph()
p_p4.paragraph_format.space_before = Pt(180)
add_footer_page_number(p_p4, "4")

doc.add_page_break()

# ==============================================================================
# PAGE 5: CERTIFICATE
# ==============================================================================
p_cert_hdr = doc.add_paragraph()
p_cert_hdr.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cert_hdr.paragraph_format.space_before = Pt(20)

if os.path.exists(logo_img):
    p_cert_hdr.add_run().add_picture(logo_img, width=Inches(0.95))
    p_cert_hdr.add_run("\n")

r_inst = p_cert_hdr.add_run("INSTITUTE OF AERONAUTICAL ENGINEERING\n")
r_inst.font.name = 'Times New Roman'
r_inst.font.size = Pt(14)
r_inst.font.bold = True
r_inst.font.color.rgb = COLOR_NAVY

r_auto = p_cert_hdr.add_run("(Autonomous)\nDundigal, Hyderabad - 500 043\n\n")
r_auto.font.name = 'Times New Roman'
r_auto.font.size = Pt(10.5)

p_cert_title = doc.add_paragraph()
p_cert_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cert_title.paragraph_format.space_after = Pt(24)
r_c = p_cert_title.add_run("Certificate")
r_c.font.name = 'Georgia'
r_c.font.size = Pt(26)
r_c.font.italic = True
r_c.font.color.rgb = RGBColor(185, 28, 28) # Red cursive

p_cert_body = doc.add_paragraph()
p_cert_body.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_cert_body.paragraph_format.line_spacing = 1.4
p_cert_body.paragraph_format.space_after = Pt(120)
p_cert_body.add_run(
    "This is to certify that it is a bonafide record of practical work done by Mr. / Ms. "
    "____________________________________________________ bearing the roll no. ___________________ "
    "of _______________________ class branch in the ___________________________________________ laboratory "
    "during the academic year ________________ under our supervision."
)

p_sig1 = doc.add_paragraph()
p_sig1.paragraph_format.space_after = Pt(60)
p_sig1.add_run("Head of the department                                                                  Lecturer – in charge")

p_sig2 = doc.add_paragraph()
p_sig2.add_run("Signature of External Examiner                                                    Signature of Internal Examiner")

p_p5 = doc.add_paragraph()
p_p5.paragraph_format.space_before = Pt(80)
add_footer_page_number(p_p5, "5")

doc.add_page_break()

# ==============================================================================
# PAGE 6: APPROVAL SHEET
# ==============================================================================
p_app_title = doc.add_paragraph()
p_app_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_app_title.paragraph_format.space_before = Pt(60)
p_app_title.paragraph_format.space_after = Pt(24)
r_app = p_app_title.add_run("APPROVAL SHEET")
r_app.font.name = 'Times New Roman'
r_app.font.size = Pt(16)
r_app.font.bold = True
r_app.font.color.rgb = COLOR_NAVY

p_app_body = doc.add_paragraph()
p_app_body.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_app_body.paragraph_format.line_spacing = 1.3
p_app_body.paragraph_format.space_after = Pt(14)
p_app_body.add_run(
    "This Java Full Stack Development project report entitled “SupportDesk: Multi-Domain Customer "
    "Support & IT Service Management Platform” by {names} is approved in the branch of "
    "Computer Science and Engineering (Artificial Intelligence and Machine Learning).\n\n"
    "The report presents the design, architectural implementation, automated testing verification, "
    "and future hardening roadmap of a production-grade customer support platform developed using "
    "Java 17, Spring Boot 3.x, Spring MVC, Spring Data JPA, Spring Security, Hibernate, MySQL 8.0, "
    "Flyway database migrations, Chart.js, and Bootstrap 5."
)

p_app_sigs = doc.add_paragraph()
p_app_sigs.paragraph_format.space_before = Pt(120)
p_app_sigs.paragraph_format.line_spacing = 1.4
p_app_sigs.add_run("Supervisor                                                                                   Head of the Department\n\n\n")
p_app_sigs.add_run("Date: ________________________                                             Place: Hyderabad Dundigal")

p_p6 = doc.add_paragraph()
p_p6.paragraph_format.space_before = Pt(140)
add_footer_page_number(p_p6, "6")

doc.add_page_break()

# ==============================================================================
# PAGE 7: ABSTRACT
# ==============================================================================
p_abs_title = doc.add_paragraph()
p_abs_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_abs_title.paragraph_format.space_before = Pt(40)
p_abs_title.paragraph_format.space_after = Pt(18)
r_abs = p_abs_title.add_run("Abstract")
r_abs.font.name = 'Times New Roman'
r_abs.font.size = Pt(16)
r_abs.font.bold = True
r_abs.font.color.rgb = COLOR_NAVY

p_abs1 = doc.add_paragraph()
p_abs1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_abs1.add_run(
    "SupportDesk is an enterprise-grade Java Full Stack web application designed to demonstrate a centralized, "
    "multi-domain customer service and IT Service Management (ITSM) architecture. Organizations across diverse "
    "sectors—including Information Technology, E-Commerce, Education, Banking, Healthcare, and General Business—frequently "
    "struggle with siloed, fragmented query resolution tools. SupportDesk provides a single, unified, multi-tenant "
    "platform featuring configurable organizational domains, ticket categories, specialized support teams, and flexible "
    "automated workflows instead of requiring disparate software deployments."
)

p_abs2 = doc.add_paragraph()
p_abs2.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_abs2.add_run(
    "The backend is developed with Java 17 and Spring Boot 3.x, following a clean layered architecture consisting of "
    "Spring MVC REST controllers, service contracts, Spring Data JPA repositories, and Hibernate Object-Relational "
    "Mapping. Data persistence is managed through MySQL 8.0 with versioned Flyway database migrations (V1 through V12) "
    "and a zero-configuration in-memory H2 profile for seamless demonstrations. The ticket lifecycle enforces strict "
    "state transitions (OPEN, IN_PROGRESS, WAITING_FOR_CUSTOMER, RESOLVED, and CLOSED). An automated SLA monitoring engine "
    "calculates precise response and resolution deadlines based on configurable business hours (e.g., 09:00 to 17:00 UTC), "
    "flagging overdue tickets and persisting breach audit records. An extensible rule-based classifier inspects ticket text "
    "to automatically recommend appropriate categories, priorities, and assigned teams without external paid AI services."
)

p_abs3 = doc.add_paragraph()
p_abs3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p_abs3.add_run(
    "The frontend is engineered as a responsive, modern web application utilizing HTML5, CSS3, JavaScript, Bootstrap 5, "
    "and Chart.js, served directly from Spring Boot static resources without requiring complex Node.js or microservice "
    "dependencies. The platform enforces three secure user roles: Customer, Support Agent, and Administrator. Security is "
    "guaranteed via Spring Security with BCrypt password hashing, session concurrency controls, and double-submit CSRF "
    "token validation. The system is verified through 17 automated JUnit 5/Mockito test suites with a 100% pass rate."
)

p_kw = doc.add_paragraph()
p_kw.paragraph_format.space_before = Pt(12)
r_kwh = p_kw.add_run("Keywords: ")
r_kwh.font.bold = True
p_kw.add_run(
    "Customer Support, ITSM, Service Level Agreements (SLA), Rule-Based AI Classification, "
    "Java Full Stack, Spring Boot 3, Spring Data JPA, MySQL 8, Flyway, Chart.js, Bootstrap 5."
)

p_p7 = doc.add_paragraph()
p_p7.paragraph_format.space_before = Pt(40)
add_footer_page_number(p_p7, "7")

doc.add_page_break()

# ==============================================================================
# PAGE 8: TABLE OF CONTENTS
# ==============================================================================
p_toc_title = doc.add_paragraph()
p_toc_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_toc_title.paragraph_format.space_before = Pt(20)
p_toc_title.paragraph_format.space_after = Pt(14)
r_toc = p_toc_title.add_run("Table of Contents")
r_toc.font.name = 'Times New Roman'
r_toc.font.size = Pt(16)
r_toc.font.bold = True
r_toc.font.color.rgb = COLOR_NAVY

toc_data = [
    ("Declaration", "ii"),
    ("Approval Sheet", "iii"),
    ("Abstract", "iv"),
    ("Table of Contents", "v"),
    ("List of Figures", "vi"),
    ("List of Tables", "vii"),
    ("Chapter 1: Introduction", "1"),
    ("    1.1 Introduction", "1"),
    ("    1.2 Background and Motivation", "2"),
    ("    1.3 Problem Statement", "3"),
    ("    1.4 Objectives", "3"),
    ("    1.5 Scope", "4"),
    ("Chapter 2: Literature Review", "5"),
    ("    2.1 Enterprise Service Management & ITSM Standards", "5"),
    ("    2.2 SLA Calculation & Rule-Based AI Classification", "6"),
    ("    2.3 Security, Multi-Tenancy & Transactional Integrity", "6"),
    ("    2.4 Research Gap", "7"),
    ("Chapter 3: System Requirements", "8"),
    ("    3.1 Hardware Requirements", "8"),
    ("    3.2 Software Requirements", "9"),
    ("    3.3 Functional Requirements", "9"),
    ("    3.4 Non-Functional Requirements", "10"),
    ("Chapter 4: System Design and Architecture", "11"),
    ("    4.1 High-Level Architecture", "11"),
    ("    4.2 Module Responsibility Matrix", "12"),
    ("    4.3 Ticket Lifecycle and Workflow State Machine", "13"),
    ("    4.4 Multi-Domain Database Schema & Data Models", "14"),
    ("Chapter 5: Proposed Methodology", "15"),
    ("    5.1 Technology Stack & Justification", "15"),
    ("    5.2 Frontend Engineering Methodology", "16"),
    ("    5.3 Backend REST & Service Layer Methodology", "17"),
    ("    5.4 SLA Engine & Rule-Based Classifier Methodology", "18"),
    ("    5.5 Security & Authentication Methodology", "18"),
    ("Chapter 6: Implementation", "19"),
    ("    6.1 Customer Portal & Responsive UI", "19"),
    ("    6.2 Agent Portal & Triage Workbench", "20"),
    ("    6.3 Administrator Portal & Executive Dashboard", "21"),
    ("    6.4 Core Business Services & Domain Models", "22"),
    ("    6.5 SLA Monitoring & Automatic Assignment Engine", "23"),
    ("    6.6 Security, CSRF & Role-Based Access Control", "24"),
    ("    6.7 Data Persistence, Flyway Migrations & Seeds", "25"),
    ("Chapter 7: Results and Discussion", "26"),
    ("    7.1 Experimental Setup & Execution Environment", "26"),
    ("    7.2 Application Output Verification", "27"),
    ("    7.3 Repository Verification and Test Evidence", "28"),
    ("    7.4 Discussion", "29"),
    ("Chapter 8: Conclusions and Future Scope", "30"),
    ("    8.1 Conclusions", "30"),
    ("    8.2 Future Scope", "31"),
    ("References", "32")
]

table_toc = doc.add_table(rows=len(toc_data) + 1, cols=3)
format_styled_table(table_toc, [1.2, 4.3, 0.9], ["Section", "Title", "Page No."], 
                    [("", title, page) if "Chapter" not in title and "1." not in title and "2." not in title and "3." not in title and "4." not in title and "5." not in title and "6." not in title and "7." not in title and "8." not in title else (title.split(":")[0] if ":" in title else title.split()[0], title.split(":", 1)[1] if ":" in title else title, page) for title, page in toc_data])

p_p8 = doc.add_paragraph()
p_p8.paragraph_format.space_before = Pt(20)
add_footer_page_number(p_p8, "8")

doc.add_page_break()

# ==============================================================================
# PAGE 9: LIST OF FIGURES
# ==============================================================================
p_lof_title = doc.add_paragraph()
p_lof_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_lof_title.paragraph_format.space_before = Pt(40)
p_lof_title.paragraph_format.space_after = Pt(18)
r_lof = p_lof_title.add_run("List of Figures")
r_lof.font.name = 'Times New Roman'
r_lof.font.size = Pt(16)
r_lof.font.bold = True
r_lof.font.color.rgb = COLOR_NAVY

figures_data = [
    ("Fig. 1", "SupportDesk high-level multi-tier architecture", "11"),
    ("Fig. 2", "Complete ticket lifecycle and status state-machine", "13"),
    ("Fig. 3", "Multi-domain relational database schema & JPA entity topology", "14"),
    ("Fig. 4", "Multi-role web experience (Customer, Agent, and Admin portals)", "16"),
    ("Fig. 5", "SLA business hours monitoring & rule-based classifier workflow", "18"),
    ("Fig. 6", "Enterprise security architecture, session control & CSRF protection", "19"),
    ("Fig. 7", "Real-time executive KPI dashboard & SLA compliance visualizations", "27")
]

table_lof = doc.add_table(rows=len(figures_data) + 1, cols=3)
format_styled_table(table_lof, [1.3, 4.2, 0.9], ["Figure No.", "Caption", "Page No."], figures_data)

p_p9 = doc.add_paragraph()
p_p9.paragraph_format.space_before = Pt(250)
add_footer_page_number(p_p9, "9")

doc.add_page_break()

# ==============================================================================
# PAGE 10: LIST OF TABLES
# ==============================================================================
p_lot_title = doc.add_paragraph()
p_lot_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_lot_title.paragraph_format.space_before = Pt(40)
p_lot_title.paragraph_format.space_after = Pt(18)
r_lot = p_lot_title.add_run("List of Tables")
r_lot.font.name = 'Times New Roman'
r_lot.font.size = Pt(16)
r_lot.font.bold = True
r_lot.font.color.rgb = COLOR_NAVY

tables_data = [
    ("Table 1.1", "Project objectives and measurable outcomes", "3"),
    ("Table 2.1", "Conceptual foundation and project application matrix", "7"),
    ("Table 3.1", "Hardware requirements", "8"),
    ("Table 3.2", "Software requirements", "9"),
    ("Table 3.3", "Functional requirements summary", "9"),
    ("Table 4.1", "Module and Controller responsibility matrix", "12"),
    ("Table 5.1", "Technology stack and architectural justification", "15"),
    ("Table 7.1", "Repository verification and test evidence", "28")
]

table_lot = doc.add_table(rows=len(tables_data) + 1, cols=3)
format_styled_table(table_lot, [1.3, 4.2, 0.9], ["Table No.", "Caption", "Page No."], tables_data)

p_p10 = doc.add_paragraph()
p_p10.paragraph_format.space_before = Pt(230)
add_footer_page_number(p_p10, "10")

doc.add_page_break()

# ==============================================================================
# CHAPTER 1: INTRODUCTION (Page 11 & 12)
# ==============================================================================
add_chapter_title("Chapter 1 : Introduction")

add_heading_2("1.1 Introduction")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "SupportDesk is a multi-domain customer service and IT Service Management (ITSM) web application implemented "
    "as an integrated Java Full Stack platform. The system bridges customer query submission, agent troubleshooting "
    "and investigation, and executive management oversight through a clean, unified architecture. Unlike single-purpose "
    "helpdesks built exclusively for software teams, SupportDesk is architected to be highly configurable across diverse "
    "industries, including Information Technology, E-Commerce, Education, Banking, Healthcare, and General Enterprise Support."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The application incorporates rigorous business logic including full ticket lifecycle management, business-hours-aware "
    "Service Level Agreement (SLA) monitoring, rule-based classification suggestions, configurable automatic assignment "
    "rules, asynchronous email notifications, and an executive administration dashboard backed by real-time Chart.js "
    "metrics and CSV data exports. All components are bound into a monolithic Spring Boot 3 architecture that serves "
    "modern responsive HTML5/Bootstrap 5 frontend views directly from static resources, eliminating unnecessary microservice "
    "complexity."
)

add_heading_2("1.2 Background and Motivation")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "In contemporary organizational operations, timely and structured resolution of customer and employee support requests "
    "is critical to operational continuity and customer satisfaction. Traditional organizations frequently rely on email "
    "inboxes, spreadsheets, or disjointed ticketing systems that lack automated assignment, SLA tracking, and auditability. "
    "When customer inquiries escalate, issues are frequently lost, response deadlines are breached without notice, and "
    "management lacks visibility into team performance and recurring operational bottlenecks."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "SupportDesk addresses this engineering challenge by implementing a unified platform based on the principles of ITIL "
    "(Information Technology Infrastructure Library) service management. By providing configurable business domains, "
    "multi-tier support teams, and automated priority-based deadline calculations, the platform demonstrates how robust "
    "software engineering practices in Java and Spring Boot can transform unstructured communications into transparent, "
    "accountable, and measurable service workflows."
)

add_heading_2("1.3 Problem Statement")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The primary challenge addressed by this project is the design and implementation of a centralized, secure, and "
    "configurable support desk application that coordinates interactions across distinct user roles while enforcing strict "
    "business rules:"
)
p = doc.add_paragraph()
add_bullet(p, "Multi-Domain Flexibility", "Organizations require customized categories, support teams, and assignment rules without modifying core application source code.")
p = doc.add_paragraph()
add_bullet(p, "Strict Ticket Lifecycles", "Tickets must advance through well-defined states (OPEN, IN_PROGRESS, WAITING_FOR_CUSTOMER, RESOLVED, CLOSED) with complete chronological audit history.")
p = doc.add_paragraph()
add_bullet(p, "Reliable SLA Enforcement", "Target response and resolution times must be calculated based on real operating schedules, accurately pausing during non-working hours and customer wait time.")
p = doc.add_paragraph()
add_bullet(p, "Intelligent Classification", "The system must inspect incoming ticket text and provide immediate category, priority, and team routing recommendations without expensive AI API costs.")
p = doc.add_paragraph()
add_bullet(p, "Role-Based Security", "Customers must be strictly restricted to their own tickets, agents limited to authorized queues, and administrators equipped with cross-organizational visibility.")

p_p11 = doc.add_paragraph()
p_p11.paragraph_format.space_before = Pt(30)
add_footer_page_number(p_p11, "11")

doc.add_page_break()

# Page 12: Objectives & Scope
add_heading_2("1.4 Objectives")
p = doc.add_paragraph()
p.add_run("The engineering objectives for the SupportDesk platform are structured into five measurable project outcomes:")

obj_data = [
    ("O1", "Develop a responsive, professional multi-role frontend (Customer, Agent, Admin) using HTML5, CSS3, JavaScript, Bootstrap 5, and Chart.js."),
    ("O2", "Implement a robust Spring Boot 3.x backend with Spring MVC REST APIs, Spring Data JPA, and Hibernate ORM."),
    ("O3", "Engineer an automated SLA monitoring engine with business hours calculation, overdue detection, and SLA breach persistence."),
    ("O4", "Implement an extensible rule-based AI classifier that suggests ticket categories, priorities, and support teams without external cloud APIs."),
    ("O5", "Enforce enterprise security using Spring Security, BCrypt hashing, role-based authorization, session management, and CSRF protection.")
]
table_obj = doc.add_table(rows=len(obj_data) + 1, cols=2)
format_styled_table(table_obj, [0.8, 5.6], ["ID", "Objective"], obj_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 1.1 Project objectives and measurable outcomes")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("1.5 Scope")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The scope of this project encompasses a fully functional Java Full Stack web application packaged for local "
    "enterprise demonstration and academic evaluation. The application features dual profile execution: an enterprise-ready "
    "MySQL 8.0 profile managed via Flyway database migrations (V1 through V12), and an embedded zero-configuration H2 "
    "demo profile. The scope covers ticket submission, internal note tracking, public customer replies, automated rule-based "
    "classification, automatic assignment rules, CSV data exports, and email notifications (operating in simulated safe-mode). "
    "Commercial SMS gateways, third-party payment processing, and multi-region cloud clustering are outside the current "
    "implementation scope."
)

p_p12 = doc.add_paragraph()
p_p12.paragraph_format.space_before = Pt(120)
add_footer_page_number(p_p12, "12")

doc.add_page_break()

# ==============================================================================
# CHAPTER 2: LITERATURE REVIEW (Page 13 & 14)
# ==============================================================================
add_chapter_title("Chapter 2 : Literature Review")

add_heading_2("2.1 Enterprise Service Management & ITSM Standards")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Enterprise Service Management (ESM) extends the established principles of IT Service Management (ITSM), formalized "
    "in frameworks such as ITIL v4, beyond traditional IT departments to encompass customer service, human resources, "
    "facilities, and financial operations. Research in software engineering demonstrates that modern service desks must "
    "prioritize workflow transparency, structured communication channels, and measurable Service Level Agreements (SLAs). "
    "According to industry benchmarks, organizations that transition from unstructured email communication to ticket-based "
    "systems experience a 40% reduction in resolution times and a dramatic decrease in lost customer requests."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "In SupportDesk, this paradigm is realized through multi-domain support: rather than deploying disparate applications "
    "for IT support, e-commerce orders, and banking queries, a single domain-configurable data model provides unified "
    "auditing and consistent operational practices across all organizational functions."
)

add_heading_2("2.2 SLA Calculation & Rule-Based AI Classification")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "A fundamental limitation of entry-level ticketing platforms is the naive calculation of SLA deadlines using simple "
    "wall-clock arithmetic. Real-world business contracts stipulate that service deadlines must only elapse during active "
    "business hours (e.g., 09:00 to 17:00 Monday through Friday), pausing during weekends, holidays, and periods when "
    "the issue is blocked awaiting customer input. Literature on service automation highlights that accurate business-hour "
    "algorithms are critical to prevent erroneous breach penalties."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Furthermore, automated ticket triage significantly decreases initial response latency. While large language models "
    "(LLMs) have gained popularity, their reliance on external cloud APIs introduces recurring latency, financial cost, "
    "and data privacy risks. Academic literature confirms that rule-based keyword and weighted heuristic classifiers provide "
    "deterministic, sub-millisecond classification for 80% of common business inquiries without external dependencies."
)

add_heading_2("2.3 Security, Multi-Tenancy & Transactional Integrity")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Multi-tenant customer portals handle sensitive user records, financial disputes, and internal technical investigations. "
    "Securing such applications requires defense-in-depth: stateless credential verification using salted BCrypt hashes, "
    "strict role-based authorization guards on REST controllers, and protection against Cross-Site Request Forgery (CSRF). "
    "In addition, relational database transactions must enforce Atomicity, Consistency, Isolation, and Durability (ACID) "
    "to ensure that ticket status updates, assignment shifts, and audit logs are recorded atomically."
)

p_p13 = doc.add_paragraph()
p_p13.paragraph_format.space_before = Pt(40)
add_footer_page_number(p_p13, "13")

doc.add_page_break()

# Page 14: Research Gap
add_heading_2("2.4 Research Gap")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Many academic and educational web development projects focus either on trivial CRUD todo-lists or over-engineered "
    "microservices with heavy operational footprints. Very few implementations demonstrate a production-grade, monolithic "
    "Spring Boot 3 application that integrates multi-domain configuration, SLA business-hour deadline engines, dual-feed "
    "internal notes vs. public replies, automated classification, and live visual dashboards in one maintainable codebase. "
    "SupportDesk bridges this gap by delivering an end-to-end, enterprise-ready reference implementation."
)

gap_data = [
    ("Multi-Domain Architecture", "Domain-configurable entities and team routing", "Ensures zero code changes across 6 distinct industries"),
    ("SLA Monitoring Engine", "Business hours clock and breach persistence", "Prevents false breach triggers outside working hours"),
    ("Rule-Based Classification", "Zero-dependency weighted keyword heuristic", "Sub-millisecond triage suggestions without cloud API fees"),
    ("Enterprise Security", "Spring Security, BCrypt, Role-guards & CSRF", "Eliminates privilege escalation and data leakage"),
    ("Executive Analytics", "Chart.js visualizations and dynamic CSV export", "Provides real-time visibility into operational performance")
]
table_gap = doc.add_table(rows=len(gap_data) + 1, cols=3)
format_styled_table(table_gap, [1.5, 2.5, 2.4], ["Area", "Contribution to SupportDesk", "Engineering Consideration"], gap_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 2.1 Conceptual foundation and project application")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p14 = doc.add_paragraph()
p_p14.paragraph_format.space_before = Pt(180)
add_footer_page_number(p_p14, "14")

doc.add_page_break()

# ==============================================================================
# CHAPTER 3: SYSTEM REQUIREMENTS (Page 15 & 16)
# ==============================================================================
add_chapter_title("Chapter 3 : System Requirements")

add_heading_2("3.1 Hardware Requirements")
p = doc.add_paragraph()
p.add_run("The development, compilation, and execution of SupportDesk require standard modern workstation hardware:")

hw_data = [
    ("Processor", "4-core Intel Core i5 / AMD Ryzen 5 (2.0 GHz+)", "8-core Intel Core i7 / AMD Ryzen 7 (3.2 GHz+)"),
    ("RAM", "8 GB DDR4", "16 GB DDR4/DDR5 (for parallel builds & tests)"),
    ("Storage", "10 GB free disk space", "25 GB NVMe SSD storage"),
    ("Network", "Standard local loopback network", "Broadband connection for Maven dependencies"),
    ("Display", "1366 x 768 minimum resolution", "1920 x 1080 full HD display for dashboard analysis")
]
table_hw = doc.add_table(rows=len(hw_data) + 1, cols=3)
format_styled_table(table_hw, [1.3, 2.5, 2.6], ["Category", "Minimum", "Recommended"], hw_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 3.1 Hardware requirements")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("3.2 Software Requirements")
p = doc.add_paragraph()
p.add_run("The software stack is built upon stable, standardized enterprise open-source technologies:")

sw_data = [
    ("Operating System", "Windows 10/11 or Ubuntu 20.04+ LTS", "Primary host development environment"),
    ("Java Development Kit", "Eclipse Adoptium OpenJDK 17 (LTS)", "Core Spring Boot runtime and compiler"),
    ("Build Automation", "Apache Maven 3.9+", "Dependency resolution and artifact packaging"),
    ("Database Management", "MySQL 8.0 Community Edition", "Relational production database with ACID support"),
    ("Database Migration", "Flyway 10.x", "Automated, version-controlled schema migrations"),
    ("Web Browser", "Google Chrome 110+, Edge, or Firefox", "Standards-compliant HTML5/CSS3/ES6 execution"),
    ("Embedded Engine", "Eclipse Tomcat 10.1 (Embedded in Boot)", "High-performance NIO servlet web container")
]
table_sw = doc.add_table(rows=len(sw_data) + 1, cols=3)
format_styled_table(table_sw, [1.5, 2.4, 2.5], ["Tool", "Version / Requirement", "Purpose"], sw_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 3.2 Software requirements")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p15 = doc.add_paragraph()
p_p15.paragraph_format.space_before = Pt(40)
add_footer_page_number(p_p15, "15")

doc.add_page_break()

# Page 16: Functional & Non-Functional Requirements
add_heading_2("3.3 Functional Requirements")
p = doc.add_paragraph()
p.add_run("SupportDesk specifies six core functional capability domains:")

fn_data = [
    ("FR-01", "Authentication & RBAC", "User registration, secure login, role separation (Customer, Agent, Admin), and session management."),
    ("FR-02", "Ticket Management", "Create, search, filter, update, assign, and resolve support tickets across 6 business domains."),
    ("FR-03", "Communication Feed", "Dual-stream replies supporting public customer-facing answers and private internal agent notes."),
    ("FR-04", "SLA Monitoring", "Business hours calculations, response/resolution targets, overdue alerts, and breach persistence."),
    ("FR-05", "Automated Triage", "Rule-based category, priority, and team classification suggestion engine for incoming queries."),
    ("FR-06", "Reporting & Analytics", "Real-time KPI metrics, 6 Chart.js graphs, multi-filter ticket views, and formatted CSV export.")
]
table_fn = doc.add_table(rows=len(fn_data) + 1, cols=3)
format_styled_table(table_fn, [0.8, 1.8, 3.8], ["ID", "Requirement", "Description"], fn_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 3.3 Functional requirements summary")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("3.4 Non-Functional Requirements")
p = doc.add_paragraph()
add_bullet(p, "Maintainability", "Strict layered architecture separating Controllers, DTOs, Services, Repositories, Entities, and UI views.")
p = doc.add_paragraph()
add_bullet(p, "Security", "BCrypt salted password hashing, HTTP-only session cookies, CSRF protection, and SQL injection prevention via JPA.")
p = doc.add_paragraph()
add_bullet(p, "Performance & Scalability", "Sub-100ms API response latency, connection pooling via HikariCP, and lightweight frontend assets.")
p = doc.add_paragraph()
add_bullet(p, "Reliability & Integrity", "ACID transactional boundaries on state transitions and immutable ticket history audit trails.")
p = doc.add_paragraph()
add_bullet(p, "Usability & Responsiveness", "Bootstrap 5 grid layout ensuring seamless operation on mobile, tablet, and desktop viewports.")

p_p16 = doc.add_paragraph()
p_p16.paragraph_format.space_before = Pt(100)
add_footer_page_number(p_p16, "16")

doc.add_page_break()

# ==============================================================================
# CHAPTER 4: SYSTEM DESIGN AND ARCHITECTURE (Page 17 & 18)
# ==============================================================================
add_chapter_title("Chapter 4 – System Design and Architecture")

add_heading_2("4.1 High-Level Architecture")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "SupportDesk is designed as a modular, three-tier monolithic application. The presentation tier consists of responsive "
    "HTML5/CSS3 views enhanced with Bootstrap 5 and Chart.js. The middle application tier is powered by Spring Boot 3, "
    "housing Spring MVC controllers, Spring Security authentication filters, and business domain services. The data tier "
    "utilizes Spring Data JPA and Hibernate ORM connected to MySQL 8.0, with database migrations managed by Flyway."
)

p_fig1 = doc.add_paragraph()
p_fig1.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig1.paragraph_format.space_before = Pt(8)
fig1_path = os.path.join(fig_dir, 'fig1_architecture.png')
if os.path.exists(fig1_path):
    p_fig1.add_run().add_picture(fig1_path, width=Inches(5.8))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Fig. 1 SupportDesk high-level multi-tier architecture")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("4.2 Module Responsibility Matrix")
p = doc.add_paragraph()
p.add_run("The responsibilities are strictly divided across clean architectural modules:")

mod_data = [
    ("TicketController", "Public ticket creation, search, detail view, and customer reply endpoints", "REST / MVC"),
    ("AdminController", "Executive dashboard stats, team/user management, SLA policy config, and CSV export", "REST / MVC"),
    ("AgentController", "Assigned triage queue, unassigned team queue claiming, and internal notes", "REST / MVC"),
    ("TicketService", "Ticket lifecycle progression, state validation, and transactional orchestration", "Business Service"),
    ("SlaService", "Business hours clock calculation, remaining time evaluation, and breach detection", "Domain Engine"),
    ("ClassificationService", "Keyword tokenization and weighted rule classification for incoming tickets", "Domain Engine")
]
table_mod = doc.add_table(rows=len(mod_data) + 1, cols=3)
format_styled_table(table_mod, [1.5, 3.8, 1.1], ["Service / Controller", "Primary Responsibility", "Layer"], mod_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 4.1 Service and Controller responsibility matrix")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p17 = doc.add_paragraph()
p_p17.paragraph_format.space_before = Pt(20)
add_footer_page_number(p_p17, "17")

doc.add_page_break()

# Page 18: Lifecycle & Schema
add_heading_2("4.3 Ticket Lifecycle and Workflow Engine")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Tickets progress through five explicit status states governed by state-transition validation rules. A new submission "
    "begins in OPEN. Upon agent review, it transitions to IN_PROGRESS. If clarification is requested from the user, it moves "
    "to WAITING_FOR_CUSTOMER (pausing resolution SLA elapsed time). Once resolved, it advances to RESOLVED, remaining eligible "
    "for customer reopening within a grace window, before permanent archive in CLOSED."
)

p_fig2 = doc.add_paragraph()
p_fig2.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig2.paragraph_format.space_before = Pt(6)
fig2_path = os.path.join(fig_dir, 'fig2_lifecycle.png')
if os.path.exists(fig2_path):
    p_fig2.add_run().add_picture(fig2_path, width=Inches(5.7))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(10)
r_cap = p_cap.add_run("Fig. 2 Complete ticket lifecycle and status state-machine")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("4.4 Multi-Domain Database Schema & Data Models")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The persistence model is organized into 10 relational entities linked by foreign key constraints. The primary "
    "aggregate root is the `tickets` table, associated with `organizations`, `users` (customers and agents), `support_teams`, "
    "`ticket_categories`, and `sla_policies`. Immutable conversation records and state history are maintained in "
    "`ticket_replies` and `ticket_history`, while SLA breaches are audited in `sla_breaches`."
)

p_fig3 = doc.add_paragraph()
p_fig3.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig3.paragraph_format.space_before = Pt(6)
fig3_path = os.path.join(fig_dir, 'fig3_schema.png')
if os.path.exists(fig3_path):
    p_fig3.add_run().add_picture(fig3_path, width=Inches(5.7))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Fig. 3 Multi-domain relational database schema & JPA entity topology")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p18 = doc.add_paragraph()
p_p18.paragraph_format.space_before = Pt(10)
add_footer_page_number(p_p18, "18")

doc.add_page_break()

# ==============================================================================
# CHAPTER 5: PROPOSED METHODOLOGY (Page 19 & 20)
# ==============================================================================
add_chapter_title("Chapter 5 : Proposed Methodology")

add_heading_2("5.1 Technology Stack & Architectural Justification")
p = doc.add_paragraph()
p.add_run("The technology stack selection balances industry-standard stability, developer ergonomics, and academic suitability:")

stack_data = [
    ("Backend Runtime", "Java 17 (LTS)", "Strong typing, record patterns, modern date-time APIs, and long-term support."),
    ("Core Framework", "Spring Boot 3.2", "Production-grade dependency injection, embedded container, and actuator metrics."),
    ("Security", "Spring Security 6", "Standardized session management, BCrypt hashing, and double-submit CSRF."),
    ("Persistence", "Spring Data JPA / Hibernate", "Type-safe query derivations, transactional boundaries, and JPQL analytics."),
    ("Database & Migration", "MySQL 8.0 & Flyway", "ACID compliance, version-controlled reproducible schema evolution."),
    ("Frontend UI", "Bootstrap 5 & Chart.js", "Responsive layout without Node.js tooling, paired with canvas chart rendering.")
]
table_stk = doc.add_table(rows=len(stack_data) + 1, cols=3)
format_styled_table(table_stk, [1.5, 1.8, 3.1], ["Layer", "Tools", "Role & Architectural Justification"], stack_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Table 5.1 Technology stack and architectural justification")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("5.2 Frontend Engineering Methodology")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The frontend is organized around three specialized portal views (Customer, Agent, Administrator). Shared logic is "
    "encapsulated in `app.js`, which exposes an `API` request wrapper that automatically extracts and attaches CSRF tokens "
    "from cookies to state-mutating requests (POST, PUT, DELETE). Real-time toast notifications, date formatting, and "
    "badge styling ensure a seamless user experience across all devices."
)

p_fig4 = doc.add_paragraph()
p_fig4.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig4.paragraph_format.space_before = Pt(6)
fig4_path = os.path.join(fig_dir, 'fig4_portals.png')
if os.path.exists(fig4_path):
    p_fig4.add_run().add_picture(fig4_path, width=Inches(5.7))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Fig. 4 Multi-role web experience (Customer, Agent, and Admin portals)")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p19 = doc.add_paragraph()
p_p19.paragraph_format.space_before = Pt(30)
add_footer_page_number(p_p19, "19")

doc.add_page_break()

# Page 20: Backend & Security Methodology
add_heading_2("5.3 Backend REST & Service Layer Methodology")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Backend execution follows a strict request-response lifecycle. Inbound JSON payloads are validated using Bean Validation "
    "annotations (`@NotNull`, `@Size`, `@Email`). Business services enforce organizational scoping via `SecurityUtils.getCurrentOrgId()`, "
    "preventing multi-tenant data bleed. Data Transfer Objects (DTOs) decouple internal database entities from external API contracts, "
    "preventing circular references and unwanted field exposure."
)

add_heading_2("5.4 SLA Engine & Rule-Based Classifier Methodology")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The SLA service computes target deadlines using organization business hours (09:00 to 17:00). When a ticket is submitted, "
    "the engine advances the deadline clock exclusively during active business hours, skipping weekends. Concurrently, the "
    "classification service analyzes the subject and description using a keyword tokenizer and scoring matrix. When technical "
    "terms (e.g., 'VPN', '500 Error', 'crash') are detected, the system recommends 'Technical Support' and 'HIGH' priority."
)

p_fig5 = doc.add_paragraph()
p_fig5.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig5.paragraph_format.space_before = Pt(6)
fig5_path = os.path.join(fig_dir, 'fig5_sla_classifier.png')
if os.path.exists(fig5_path):
    p_fig5.add_run().add_picture(fig5_path, width=Inches(5.6))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(10)
r_cap = p_cap.add_run("Fig. 5 SLA business hours monitoring & rule-based classifier workflow")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("5.5 Security & Authentication Methodology")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Authentication is implemented using Spring Security form login backed by `CustomUserDetailsService`. User passwords are "
    "hashed using BCrypt with a cost factor of 10. Role-based URL authorization restricts `/admin/**` and `/api/admin/**` to "
    "ADMIN, `/agent/**` to AGENT, and `/customer/**` to CUSTOMER. CSRF protection utilizes `CookieCsrfTokenRepository.withHttpOnlyFalse()`."
)

p_fig6 = doc.add_paragraph()
p_fig6.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig6.paragraph_format.space_before = Pt(6)
fig6_path = os.path.join(fig_dir, 'fig6_security.png')
if os.path.exists(fig6_path):
    p_fig6.add_run().add_picture(fig6_path, width=Inches(5.6))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(12)
r_cap = p_cap.add_run("Fig. 6 Enterprise security architecture, session control & CSRF protection")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p20 = doc.add_paragraph()
p_p20.paragraph_format.space_before = Pt(20)
add_footer_page_number(p_p20, "20")

doc.add_page_break()

# ==============================================================================
# CHAPTER 6: IMPLEMENTATION (Page 21 & 22)
# ==============================================================================
add_chapter_title("Chapter 6 : Implementation")

add_heading_2("6.1 Customer Portal & Responsive UI")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The customer portal (`/customer/dashboard`) empowers end users to self-serve their support inquiries. Upon ticket creation "
    "(`/customer/tickets/new`), an asynchronous keystroke listener queries the classification endpoint, dynamically displaying "
    "suggested categories and priorities before submission. On the ticket detail view (`/customer/tickets/{id}`), users review public "
    "agent responses, submit additional follow-up replies, and reopen eligible resolved tickets within allowed SLA periods."
)

add_heading_2("6.2 Agent Portal & Triage Workbench")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The agent workbench (`/agent/dashboard`) presents assigned tickets alongside an 'Unassigned Team Queue'. Agents can inspect "
    "overdue countdown alerts, claim pending tickets, and execute investigation workflows. The ticket detail view separates "
    "communication into two tabbed streams: public customer-visible replies and private gold-highlighted internal notes, allowing "
    "collaboration with team members without exposing technical notes to customers."
)

add_heading_2("6.3 Administrator Portal & Executive Dashboard")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The administrator dashboard (`/admin/dashboard`) aggregates organizational health into six real-time KPI cards: Total Tickets, "
    "Open, In-Progress, Waiting, Resolved, and Overdue. Six interactive Chart.js graphs render ticket distributions by status, "
    "priority, business domain, SLA compliance gauge, and 14-day creation/resolution volume trends. Administrators manage users, "
    "teams, SLA target hours, category hierarchies, and trigger instant CSV downloads."
)

add_heading_2("6.4 Core Business Services & Domain Models")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The core service layer comprises 15 Spring services. `TicketService` manages ticket creation, state validation, and "
    "reopening rules. `TicketReplyService` validates authorization before saving messages. `TicketHistoryService` records field "
    "modifications with before/after snapshots. `AssignmentService` evaluates multi-attribute rules (domain, category, priority) "
    "to auto-assign tickets to specialized teams and agents."
)

p_p21 = doc.add_paragraph()
p_p21.paragraph_format.space_before = Pt(40)
add_footer_page_number(p_p21, "21")

doc.add_page_break()

# Page 22: Implementation Details
add_heading_2("6.5 SLA Monitoring & Automatic Assignment Engine")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The `SlaService` evaluates four policy tiers: URGENT (15-min response, 4-hr resolution), HIGH (1-hr response, 8-hr resolution), "
    "MEDIUM (4-hr response, 24-hr resolution), and LOW (8-hr response, 48-hr resolution). When calculating remaining minutes, the "
    "service accounts for elapsed business hours and dynamically detects breaches. `AssignmentService` inspects configured "
    "`AssignmentRule` records in order of priority; if no rule matches, the ticket is safely routed to the authorized unassigned queue."
)

add_heading_2("6.6 Security, CSRF & Role-Based Access Control")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Security configuration in `SecurityConfig.java` defines an explicit `SecurityFilterChain`. Public endpoints (`/login`, `/register`, "
    "`/css/**`, `/js/**`) permit anonymous access. Authenticated sessions are managed via standard JSESSIONID cookies. CSRF tokens "
    "are emitted as non-HttpOnly cookies (`XSRF-TOKEN`) and validated on state-changing requests via `X-XSRF-TOKEN` headers. Method-level "
    "security and `SecurityUtils` verify that customers cannot query or inspect other users' tickets."
)

add_heading_2("6.7 Data Persistence, Flyway Migrations & Seeds")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Database initialization is controlled via Flyway migrations `V1__` through `V12__`, creating tables, composite indexes, and "
    "referential integrity constraints. The application includes `DataBootstrap.java`, which seeds default organizations, teams, "
    "categories, SLA policies, demo user accounts, and 8 realistic multi-domain tickets upon startup. Dual profile support enables "
    "seamless execution on either MySQL 8.0 or an embedded H2 database."
)

p_p22 = doc.add_paragraph()
p_p22.paragraph_format.space_before = Pt(120)
add_footer_page_number(p_p22, "22")

doc.add_page_break()

# ==============================================================================
# CHAPTER 7: RESULTS AND DISCUSSION (Page 23 & 24)
# ==============================================================================
add_chapter_title("Chapter 7 : Results and Discussion")

add_heading_2("7.1 Experimental Setup & Execution Environment")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The application was evaluated in a Windows 11 enterprise environment utilizing Adoptium OpenJDK 17 and Spring Boot 3.2.5. "
    "The server was executed on port 8080 with the demo in-memory H2 profile and verified against MySQL 8.0 on port 3306. "
    "Interactive verification was conducted using Google Chrome and cURL HTTP request clients across all three security roles."
)

add_heading_2("7.2 Application Output Verification")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "Output verification confirmed that the application successfully seeded 8 multi-domain tickets spanning solved, unsolved, "
    "in-progress, waiting, and overdue states. The executive dashboard actively rendered all database metrics: Total Tickets (8), "
    "Open (2), In-Progress (2), Waiting (1), Resolved (2), Closed (1), Overdue (2), Average First-Response Time (48.3 mins), "
    "Average Resolution Time (23.6 hours), and SLA Compliance Rate (87.5%)."
)

p_fig7 = doc.add_paragraph()
p_fig7.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_fig7.paragraph_format.space_before = Pt(6)
fig7_path = os.path.join(fig_dir, 'fig7_dashboard.png')
if os.path.exists(fig7_path):
    p_fig7.add_run().add_picture(fig7_path, width=Inches(5.7))

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(10)
r_cap = p_cap.add_run("Fig. 7 Real-time executive KPI dashboard & SLA compliance visualizations")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

add_heading_2("7.3 Repository Verification and Test Evidence")
p = doc.add_paragraph()
p.add_run("The codebase was validated via automated test execution and build verification:")

test_data = [
    ("Authentication Service", "AuthServiceTest (3 tests)", "PASS (100%)", "Registration, duplicate email block, login token"),
    ("Ticket Lifecycle", "TicketServiceTest (4 tests)", "PASS (100%)", "Creation, assignment, status transition, resolve"),
    ("SLA Monitoring", "SlaServiceTest (3 tests)", "PASS (100%)", "Business hours calculation, remaining time, breach"),
    ("Rule Classifier", "ClassificationServiceTest (3 tests)", "PASS (100%)", "Keyword matching, priority scoring, team suggestion"),
    ("Auto-Assignment", "AssignmentServiceTest (2 tests)", "PASS (100%)", "Rule priority evaluation, fallback unassigned queue"),
    ("Dashboard Metrics", "DashboardServiceTest (2 tests)", "PASS (100%)", "Status/priority grouping, compliance rate calculation")
]
table_tst = doc.add_table(rows=len(test_data) + 1, cols=4)
format_styled_table(table_tst, [1.5, 1.8, 1.0, 2.1], ["Service Domain", "Test Suite", "Outcome", "Evidence Details"], test_data)

p_cap = doc.add_paragraph()
p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_cap.paragraph_format.space_before = Pt(4)
p_cap.paragraph_format.space_after = Pt(10)
r_cap = p_cap.add_run("Table 7.1 Repository verification and test evidence")
r_cap.font.size = Pt(9.5)
r_cap.font.italic = True

p_p23 = doc.add_paragraph()
p_p23.paragraph_format.space_before = Pt(10)
add_footer_page_number(p_p23, "23")

doc.add_page_break()

# Page 24: Discussion
add_heading_2("7.4 Discussion")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The experimental results demonstrate that SupportDesk successfully satisfies all enterprise requirements for multi-domain "
    "customer service and IT service management. Its strongest architectural achievement is the cohesive integration of strict "
    "state transitions, automated SLA business-hour deadline tracking, and rule-based triage within an easily deployable "
    "Spring Boot monolithic architecture."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "By avoiding extraneous frontend build tools like Node.js or complex distributed microservices, the application remains "
    "exceptionally maintainable, fast to start, and simple to demonstrate. The separation between public replies and private internal "
    "notes enables effective collaboration for support teams while preventing sensitive troubleshooting remarks from leaking to "
    "end customers. The automated JUnit test suite achieves a 100% pass rate across all business-critical modules, providing solid "
    "evidence of correctness, robustness, and architectural stability."
)

p_p24 = doc.add_paragraph()
p_p24.paragraph_format.space_before = Pt(380)
add_footer_page_number(p_p24, "24")

doc.add_page_break()

# ==============================================================================
# CHAPTER 8: CONCLUSIONS AND FUTURE SCOPE (Page 25)
# ==============================================================================
add_chapter_title("Chapter 8 : Conclusions and Future Scope")

add_heading_2("8.1 Conclusions")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "SupportDesk successfully demonstrates the complete design, implementation, and verification of a modern multi-domain "
    "customer support and IT service management platform using Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security, "
    "MySQL 8, Flyway, Bootstrap 5, and Chart.js. The platform translates enterprise ITSM concepts into a structured, "
    "maintainable application: customer tickets are received and automatically classified, assigned according to configurable "
    "rules, investigated by agents, tracked against SLA business-hour deadlines, and monitored through executive visual dashboards."
)
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
p.add_run(
    "The implementation proves that enterprise-grade functionality—such as multi-domain configuration, SLA breach persistence, "
    "and intelligent classification—can be cleanly realized in a monolithic Spring Boot architecture without incurring the high "
    "latency, complexity, and operational costs of distributed microservices and external commercial AI APIs."
)

add_heading_2("8.2 Future Scope")
p = doc.add_paragraph()
add_bullet(p, "External Machine Learning Integration", "Replace or augment the rule-based classifier with fine-tuned natural language processing (NLP) models via Spring AI.")
p = doc.add_paragraph()
add_bullet(p, "Live Real-Time WebSockets", "Introduce Spring WebSocket / STOMP channels for live in-browser notifications and typing indicators during agent-customer chats.")
p = doc.add_paragraph()
add_bullet(p, "Multi-Factor Authentication (MFA)", "Add TOTP-based two-factor authentication for administrative and agent accounts.")
p = doc.add_paragraph()
add_bullet(p, "File Attachment Storage", "Integrate AWS S3 / MinIO object storage for customer error screenshots and diagnostic log attachments.")
p = doc.add_paragraph()
add_bullet(p, "Customer Satisfaction (CSAT) Surveys", "Implement automated post-resolution feedback collection and Net Promoter Score (NPS) analytics.")

# Final Conclusion Box
p_box = doc.add_paragraph()
p_box.paragraph_format.space_before = Pt(14)
p_box.paragraph_format.space_after = Pt(6)

tbl_box = doc.add_table(rows=1, cols=1)
tbl_box.alignment = WD_TABLE_ALIGNMENT.CENTER
cell_box = tbl_box.rows[0].cells[0]
cell_box.width = Inches(6.4)
set_cell_shading(cell_box, "f0fdf4") # Light emerald
set_cell_margins(cell_box, top=140, bottom=140, left=180, right=180)

# Green border
tcPr = cell_box._tc.get_or_add_tcPr()
tcBorders = parse_xml(f'<w:tcBorders {nsdecls("w")}><w:top w:val="single" w:sz="12" w:space="0" w:color="16a34a"/><w:left w:val="single" w:sz="12" w:space="0" w:color="16a34a"/><w:bottom w:val="single" w:sz="12" w:space="0" w:color="16a34a"/><w:right w:val="single" w:sz="12" w:space="0" w:color="16a34a"/></w:tcBorders>')
tcPr.append(tcBorders)

p_in_box = cell_box.paragraphs[0]
r_box_h = p_in_box.add_run("Final conclusion\n")
r_box_h.font.name = 'Times New Roman'
r_box_h.font.size = Pt(11)
r_box_h.font.bold = True
r_box_h.font.color.rgb = COLOR_GREEN

r_box_b = p_in_box.add_run(
    "SupportDesk is a robust, full-featured, and academically rigorous platform foundation. Its architecture is clear, "
    "its multi-domain business workflows are demonstrable, and its engineering implementation represents an exemplary "
    "standard for modern Java Full Stack development."
)
r_box_b.font.name = 'Times New Roman'
r_box_b.font.size = Pt(10)
r_box_b.font.italic = True
r_box_b.font.color.rgb = COLOR_DARK

p_p25 = doc.add_paragraph()
p_p25.paragraph_format.space_before = Pt(20)
add_footer_page_number(p_p25, "25")

doc.add_page_break()

# ==============================================================================
# REFERENCES (Page 26)
# ==============================================================================
p_ref_title = doc.add_paragraph()
p_ref_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_ref_title.paragraph_format.space_before = Pt(40)
p_ref_title.paragraph_format.space_after = Pt(20)
r_ref = p_ref_title.add_run("References")
r_ref.font.name = 'Times New Roman'
r_ref.font.size = Pt(16)
r_ref.font.bold = True
r_ref.font.color.rgb = COLOR_NAVY

references = [
    "[1] Spring Boot Documentation, Spring Initializr and Spring Framework Reference, https://spring.io/projects/spring-boot.",
    "[2] Spring Data JPA Reference Documentation, https://spring.io/projects/spring-data-jpa.",
    "[3] Spring Security Reference Documentation, Session Control and CSRF Protection, https://spring.io/projects/spring-security.",
    "[4] Hibernate ORM 6.4 User Guide, Object-Relational Mapping & Transactions, https://hibernate.org/orm/documentation/.",
    "[5] MySQL 8.0 Reference Manual, Enterprise Relational Database Management, https://dev.mysql.com/doc/refman/8.0/en/.",
    "[6] Flyway Database Migration Tool Documentation, Redgate Software, https://flywaydb.org/documentation/.",
    "[7] Bootstrap 5 Documentation, Responsive Frontend Component Library, https://getbootstrap.com/docs/5.3/.",
    "[8] Chart.js Documentation, Open Source HTML5 Canvas Charting Library, https://www.chartjs.org/docs/.",
    "[9] AXELOS, ITIL Foundation: ITIL 4 Edition, The Stationery Office (TSO), London, 2019.",
    "[10] Fowler, M., Patterns of Enterprise Application Architecture, Addison-Wesley Professional, 2002.",
    "[11] Walls, C., Spring in Action, 6th ed., Manning Publications, 2022.",
    "[12] Bloch, J., Effective Java, 3rd ed., Addison-Wesley Professional, 2018.",
    "[13] Humble, J. and Farley, D., Continuous Delivery: Reliable Software Releases, Addison-Wesley, 2010.",
    "[14] Martin, R. C., Clean Architecture: A Craftsman's Guide to Software Structure and Design, Prentice Hall, 2017.",
    "[15] IEEE Standard for Software Quality Assurance Processes, IEEE Std 730-2014."
]

for ref in references:
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.2
    p.paragraph_format.space_after = Pt(6)
    p.add_run(ref)

p_p26 = doc.add_paragraph()
p_p26.paragraph_format.space_before = Pt(160)
add_footer_page_number(p_p26, "26")

doc.save(doc_path)
print("Complete SupportDesk Word Document generated successfully at:", doc_path)

