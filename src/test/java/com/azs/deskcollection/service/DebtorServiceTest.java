package com.azs.deskcollection.service;

import com.azs.deskcollection.model.Debtor;
import com.azs.deskcollection.model.User;
import com.azs.deskcollection.repository.DebtorRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtorServiceTest {

    @Mock
    private DebtorRepository debtorRepository;

    @InjectMocks
    private DebtorService debtorService;

    @Test
    void importDebtorsFromExcel_shouldSaveDebtors() throws IOException {
        // Create a valid Excel file in memory
        Workbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Debtors");
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Phone");

        var row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("John Doe");
        row1.createCell(1).setCellValue("1234567890");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile(
                "file", "debtors.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());

        User user = new User();
        debtorService.importDebtorsFromExcel(file, user);

        verify(debtorRepository).saveAll(anyList());
    }

    @Test
    void getDebtorsByUser_shouldReturnDebtors() {
        User user = new User();
        debtorService.getDebtorsByUser(user);
        verify(debtorRepository).findByUser(user);
    }
}
