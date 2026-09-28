package com.household.household.service;

import com.household.household.dto.ExcelImportResponse;
import com.household.household.entity.AcData;
import com.household.household.enums.AcType;
import com.household.household.enums.InverterType;
import com.household.household.exception.ExcelImportException;
import com.household.household.repository.AcDataRepository;
import com.household.household.service.serviceImpl.AcDataImportServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AcDataImportServiceTest {

    private AcDataRepository acDataRepository;
    private ExcelNormalizationService normalizationService;
    private DuplicateDetectionService duplicateDetectionService;
    private AcDataImportServiceImpl importService;

    @BeforeEach
    void setUp() {
        acDataRepository = mock(AcDataRepository.class);
        normalizationService = new ExcelNormalizationService();
        duplicateDetectionService = new DuplicateDetectionService();
        importService = new AcDataImportServiceImpl(acDataRepository, normalizationService, duplicateDetectionService);
    }

    private MockMultipartFile createExcelFile(String[][] data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("AC Data");
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"year", "brand", "model name", "ac type", "capacity in ton", "inverter/non-inverter", "star rating", "launching price"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // Data
            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < data[r].length; c++) {
                    if (data[r][c] != null) {
                        try {
                            double num = Double.parseDouble(data[r][c]);
                            row.createCell(c).setCellValue(num);
                        } catch (NumberFormatException e) {
                            row.createCell(c).setCellValue(data[r][c]);
                        }
                    }
                }
            }
            
            workbook.write(out);
            return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    @Test
    void testExactDuplicateWithinExcel_isRejected() throws Exception {
        String[][] data = {
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "45000"},
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "45000"}
        };
        MockMultipartFile file = createExcelFile(data);
        when(acDataRepository.findRowHashByRowHashIn(any())).thenReturn(Collections.emptySet());

        ExcelImportResponse response = importService.importExcel(file);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getTotalRows());
        assertEquals(1, response.getInsertedRows());
        assertEquals(1, response.getDuplicateRows());
        
        verify(acDataRepository, times(1)).saveAll(any());
    }

    @Test
    void testPartialMatch_NotDuplicate() throws Exception {
        String[][] data = {
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "45000"},
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "46000"} // Price diff
        };
        MockMultipartFile file = createExcelFile(data);
        when(acDataRepository.findRowHashByRowHashIn(any())).thenReturn(Collections.emptySet());

        ExcelImportResponse response = importService.importExcel(file);

        assertEquals(2, response.getInsertedRows());
        assertEquals(0, response.getDuplicateRows());
    }

    @Test
    void testDuplicateAgainstDatabase() throws Exception {
        String[][] data = {
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "45000"}
        };
        MockMultipartFile file = createExcelFile(data);
        
        AcData temp = AcData.builder()
                .year(2024).brand("LG").modelName("LG123").acType(AcType.SPLIT)
                .capacityInTon(new BigDecimal("1.5")).inverterNonInverter(InverterType.INVERTER)
                .starRating(5).launchingPrice(new BigDecimal("45000")).build();
        temp = normalizationService.normalize(temp);
        String hash = duplicateDetectionService.generateRowHash(temp);

        when(acDataRepository.findRowHashByRowHashIn(any())).thenReturn(Set.of(hash));

        ExcelImportResponse response = importService.importExcel(file);

        assertEquals(0, response.getInsertedRows());
        assertEquals(1, response.getDuplicateRows());
        assertEquals(1, response.getDuplicates().size());
        assertEquals("Duplicate row already exists in database", response.getDuplicates().get(0).getReason());
    }

    @Test
    void testNumericEquivalence() throws Exception {
        // 1.5 vs 1.50
        String[][] data = {
                {"2024", "LG", "LG123", "Split", "1.5", "Inverter", "5", "45000"},
                {"2024", "LG", "LG123", "Split", "1.50", "Inverter", "5", "45000.0"}
        };
        MockMultipartFile file = createExcelFile(data);
        when(acDataRepository.findRowHashByRowHashIn(any())).thenReturn(Collections.emptySet());

        ExcelImportResponse response = importService.importExcel(file);

        assertEquals(1, response.getInsertedRows());
        assertEquals(1, response.getDuplicateRows());
    }

    @Test
    void testWhitespaceEquivalence() throws Exception {
        // " LG " vs "LG"
        String[][] data = {
                {"2024", " LG ", "LG123", "Split", "1.5", "Inverter", "5", "45000"},
                {"2024", "LG", " LG123 ", "Split", "1.5", "Inverter", "5", "45000"}
        };
        MockMultipartFile file = createExcelFile(data);
        when(acDataRepository.findRowHashByRowHashIn(any())).thenReturn(Collections.emptySet());

        ExcelImportResponse response = importService.importExcel(file);

        assertEquals(1, response.getInsertedRows());
        assertEquals(1, response.getDuplicateRows());
    }
}
