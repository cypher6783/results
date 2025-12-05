package com.university.resultsystem.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.university.resultsystem.config.UniversityConfig;
import com.university.resultsystem.dto.CourseResultDto;
import com.university.resultsystem.dto.DetailedResultDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    private final UniversityConfig universityConfig;

    public PdfService(UniversityConfig universityConfig) {
        this.universityConfig = universityConfig;
    }

    public byte[] generateResultSlip(DetailedResultDto result) throws DocumentException {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        // Add header
        addHeader(document, result);
        document.add(Chunk.NEWLINE);

        // Add student info
        addStudentInfo(document, result);
        document.add(Chunk.NEWLINE);

        // Add course table
        addCourseTable(document, result.getCourses());
        document.add(Chunk.NEWLINE);

        // Add performance summary
        addPerformanceSummary(document, result);
        document.add(Chunk.NEWLINE);

        // Add key/legend
        addLegend(document);

        // Add footer (Signature & Page number)
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    private void addHeader(Document document, DetailedResultDto result) throws DocumentException {
        // Logo Placeholder (Top Left) - Using a table to position it if we had one,
        // but for now just center the text as per typical letterheads if logo isn't
        // absolute.
        // The image shows the logo on the left and text centered.
        // We can use a 3-column table: Logo | Text | Empty/Right

        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[] { 1.5f, 7f, 1.5f });
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Column 1: Logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        // TODO: Load actual logo image here
        // Image logo = Image.getInstance("path/to/logo.png");
        // logoCell.addElement(logo);
        logoCell.addElement(new Phrase("LOGO", FontFactory.getFont(FontFactory.TIMES, 8)));
        headerTable.addCell(logoCell);

        // Column 2: University Details
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font uniFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14);
        Font subFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);
        Font deptFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);

        Paragraph uniName = new Paragraph(universityConfig.getName().toUpperCase(), uniFont);
        uniName.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(uniName);

        Paragraph pmb = new Paragraph(universityConfig.getPmb(), subFont);
        pmb.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(pmb);

        Paragraph college = new Paragraph(universityConfig.getCollege().toUpperCase(), subFont);
        college.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(college);

        Paragraph dept = new Paragraph(universityConfig.getDepartment().toUpperCase(), deptFont);
        dept.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(dept);

        headerTable.addCell(textCell);

        // Column 3: Empty
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(emptyCell);

        document.add(headerTable);

        // Statement of Result Line
        Font statementFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);
        statementFont.setStyle(Font.UNDERLINE);

        Paragraph statement = new Paragraph(
                "STATEMENT OF EXAMINATION RESULT FOR " +
                        (result.getSemester() == 1 ? "FIRST" : "SECOND") +
                        " SEMESTER " + result.getSessionName(),
                statementFont);
        statement.setAlignment(Element.ALIGN_CENTER);
        statement.setSpacingBefore(10);
        document.add(statement);
    }

    private void addStudentInfo(Document document, DetailedResultDto result) throws DocumentException {
        Font labelFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 9);
        Font contentFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 9); // Bold for filled info

        // Course and Level lines
        Paragraph courseLine = new Paragraph("COURSE: " + result.getCourse(), labelFont);
        document.add(courseLine);

        Paragraph levelLine = new Paragraph("LEVEL: " + result.getLevel(), labelFont);
        levelLine.setSpacingAfter(5);
        document.add(levelLine);

        // Boxed Table for Reg No and Name
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 2.5f, 2.5f, 1.5f, 4.5f }); // Adjusted widths

        // Row 1
        addBoxedCell(infoTable, "REGISTRATION NUMBER:", labelFont, true);
        addBoxedCell(infoTable, result.getMatricNo(), contentFont, false);
        addBoxedCell(infoTable, "FULL NAME:", labelFont, true);
        addBoxedCell(infoTable, result.getFullName().toUpperCase(), contentFont, false);

        document.add(infoTable);
    }

    private void addCourseTable(Document document, List<CourseResultDto> courses) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 8);
        Font dataFont = FontFactory.getFont(FontFactory.TIMES, 8);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        // Code, Title, Unit, Score, Point Earn, Remark
        table.setWidths(new float[] { 1.5f, 6f, 1f, 1f, 1.5f, 1.5f });

        // Headers
        addTableHeader(table, "Code", headerFont);
        addTableHeader(table, "Title", headerFont);
        addTableHeader(table, "Unit", headerFont);
        addTableHeader(table, "Score", headerFont);
        addTableHeader(table, "Point Earn", headerFont);
        addTableHeader(table, "Remark", headerFont);

        // Data rows
        for (CourseResultDto course : courses) {
            addTableCell(table, course.getCode(), dataFont);
            addTableCell(table, course.getTitle(), dataFont);
            addTableCell(table, String.valueOf(course.getUnit()), dataFont);
            addTableCell(table, String.format("%.0f%s", course.getScore(), course.getGrade()), dataFont); // e.g. 76A
            addTableCell(table, String.format("%.2f", course.getPointEarned()), dataFont);
            addTableCell(table, course.getRemark(), dataFont);
        }

        // Fill empty rows if needed to make it look full?
        // The image shows a fixed height table, but dynamic is fine for now.

        document.add(table);
    }

    private void addPerformanceSummary(Document document, DetailedResultDto result) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 8);
        Font dataFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 8); // Values seem bold in image

        // "PERFORMANCE" Header
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase("PERFORMANCE", headerFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(BaseColor.LIGHT_GRAY); // Or a very light gray
        titleCell.setPadding(3);
        mainTable.addCell(titleCell);
        document.add(mainTable);

        // The 8-column data table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.5f, 1f, 1.5f, 1f, 1.5f, 1f, 1.5f, 1f });

        // Row 1: Current
        addPerfLabel(table, "TCC", headerFont);
        addPerfValue(table, String.valueOf(result.getTcc()), dataFont);
        addPerfLabel(table, "TCE", headerFont);
        addPerfValue(table, String.valueOf(result.getTce()), dataFont);
        addPerfLabel(table, "TPE", headerFont);
        addPerfValue(table, String.format("%.0f", result.getTpe()), dataFont);
        addPerfLabel(table, "GPA", headerFont);
        addPerfValue(table, String.format("%.2f", result.getGpa()), dataFont);

        // Row 2: Previous
        addPerfLabel(table, "Previous TCC", headerFont);
        addPerfValue(table, result.getPreviousTcc() != null ? String.valueOf(result.getPreviousTcc()) : "-", dataFont);
        addPerfLabel(table, "Previous TCE", headerFont);
        addPerfValue(table, result.getPreviousTce() != null ? String.valueOf(result.getPreviousTce()) : "-", dataFont);
        addPerfLabel(table, "Previous TPE", headerFont);
        addPerfValue(table, result.getPreviousTpe() != null ? String.format("%.0f", result.getPreviousTpe()) : "-",
                dataFont);
        addPerfLabel(table, "Previous CGPA", headerFont);
        addPerfValue(table, result.getPreviousGpa() != null ? String.format("%.2f", result.getPreviousGpa()) : "-",
                dataFont);

        // Row 3: Cumulative
        addPerfLabel(table, "CCC", headerFont);
        addPerfValue(table, String.valueOf(result.getCcc()), dataFont);
        addPerfLabel(table, "CCE", headerFont);
        addPerfValue(table, String.valueOf(result.getCce()), dataFont);
        addPerfLabel(table, "CPE", headerFont);
        addPerfValue(table, String.format("%.0f", result.getCpe()), dataFont);
        addPerfLabel(table, "CGPA", headerFont);
        addPerfValue(table, String.format("%.2f", result.getCgpa()), dataFont);

        document.add(table);
    }

    private void addLegend(Document document) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 8);
        Font dataFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 7); // Italicized in image? Looks like it.

        // "KEY" Header
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingBefore(5);

        PdfPCell titleCell = new PdfPCell(new Phrase("KEY", headerFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        titleCell.setPadding(2);
        mainTable.addCell(titleCell);
        document.add(mainTable);

        // 4-column Legend Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        // Row 1
        addLegendCell(table, "TCC = Total Credit Carried", dataFont);
        addLegendCell(table, "TCE = Total Credit Earned", dataFont);
        addLegendCell(table, "TPE = Total Points Earned", dataFont);
        addLegendCell(table, "GPA = Grade Points Average", dataFont);

        // Row 2
        addLegendCell(table, "CCC = Cummulative Credit Carried", dataFont);
        addLegendCell(table, "CCE = Cummulative Credit Earned", dataFont); // Note: Image says "Cummulative" (sic)
        addLegendCell(table, "CPE = Cummulative Points Earned", dataFont);
        addLegendCell(table, "CGPA = Cummulative Grade Points Averag", dataFont); // Image cut off? "Average"

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // Signature Stamp Placeholder
        // Image shows a stamp on the right.
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);

        PdfPCell leftCell = new PdfPCell(new Phrase(""));
        leftCell.setBorder(Rectangle.NO_BORDER);
        footerTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        // Placeholder for stamp
        // Image stamp = Image.getInstance(...);
        // rightCell.addElement(stamp);
        Paragraph stampPlaceholder = new Paragraph("(Signature/Stamp)", FontFactory.getFont(FontFactory.TIMES, 8));
        stampPlaceholder.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(stampPlaceholder);

        footerTable.addCell(rightCell);
        document.add(footerTable);

        // Bottom Text
        Paragraph bottomText = new Paragraph("B. SC. COMPUTER SCIENCE",
                FontFactory.getFont(FontFactory.TIMES_ITALIC, 9));
        bottomText.setAlignment(Element.ALIGN_CENTER);
        bottomText.setSpacingBefore(20);
        document.add(bottomText);

        // Page Number (Manual for now, or use PageEventHelper for true footer)
        Paragraph pageNum = new Paragraph("Page 1/1", FontFactory.getFont(FontFactory.TIMES, 8));
        pageNum.setAlignment(Element.ALIGN_RIGHT);
        document.add(pageNum);
    }

    // --- Helpers ---

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT); // Image shows left align for most? Or center?
        // Actually headers look left aligned or centered. Let's stick to standard.
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addBoxedCell(PdfPTable table, String text, Font font, boolean isLabel) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        if (isLabel) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        table.addCell(cell);
    }

    private void addPerfLabel(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(240, 240, 240)); // Very light gray
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addPerfValue(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private void addLegendCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOX); // Boxed in image? Yes, the whole key table is grid.
        table.addCell(cell);
    }

    public byte[] generateBatchResultsPdf(List<DetailedResultDto> results) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();

        for (int i = 0; i < results.size(); i++) {
            if (i > 0) {
                document.newPage();
            }
            DetailedResultDto result = results.get(i);

            addHeader(document, result);
            document.add(Chunk.NEWLINE);
            addStudentInfo(document, result);
            document.add(Chunk.NEWLINE);
            addCourseTable(document, result.getCourses());
            document.add(Chunk.NEWLINE);
            addPerformanceSummary(document, result);
            document.add(Chunk.NEWLINE);
            addLegend(document);
            addFooter(document);
        }

        document.close();
        return out.toByteArray();
    }
}
