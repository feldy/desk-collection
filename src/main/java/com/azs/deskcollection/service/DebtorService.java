package com.azs.deskcollection.service;

import com.azs.deskcollection.model.Debtor;
import com.azs.deskcollection.model.User;
import com.azs.deskcollection.repository.DebtorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class DebtorService {

    private final DebtorRepository debtorRepository;

    @Autowired
    public DebtorService(DebtorRepository debtorRepository) {
        this.debtorRepository = debtorRepository;
    }

    public void importDebtorsFromExcel(MultipartFile file, User user) throws IOException {
        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<Debtor> debtors = new ArrayList<>();
            DataFormatter dataFormatter = new DataFormatter();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                // Check if row is empty/first cell is empty to avoid processing empty rows
                if (currentRow.getCell(0) == null || currentRow.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                Debtor debtor = new Debtor();
                // Assumed Column Mapping:
                // 0: Name (Required)
                // 1: Phone (Required)
                // 2: Email
                // 3: Address
                // 4: Notes

                String name = getCellValue(currentRow, 0, dataFormatter);
                String phoneNumber = getCellValue(currentRow, 1, dataFormatter);

                if (name.isEmpty() || phoneNumber.isEmpty()) {
                    // Skip invalid records or throw exception?
                    // For now, let's skip
                    rowNumber++;
                    continue;
                }

                debtor.setName(name);
                debtor.setPhoneNumber(phoneNumber);
                debtor.setEmail(getCellValue(currentRow, 2, dataFormatter));
                debtor.setAddress(getCellValue(currentRow, 3, dataFormatter));
                debtor.setNotes(getCellValue(currentRow, 4, dataFormatter));
                debtor.setUser(user);

                debtors.add(debtor);
                rowNumber++;
            }

            if (!debtors.isEmpty()) {
                debtorRepository.saveAll(debtors);
            }
        }
    }

    public List<Debtor> getAllDebtors() {
        return debtorRepository.findAll();
    }

    public List<Debtor> getDebtorsByUser(User user) {
        return debtorRepository.findByUser(user);
    }

    private String getCellValue(Row row, int cellIndex, DataFormatter dataFormatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell);
    }

    public java.io.ByteArrayInputStream generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Debtors");
            Row headerRow = sheet.createRow(0);

            String[] columns = { "Name", "Phone Number", "Email", "Address", "Notes" };
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new java.io.ByteArrayInputStream(out.toByteArray());
        }
    }
}
