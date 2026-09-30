package com.household.household.service.serviceImpl;

import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.entity.UsedCarData;
import com.household.household.enums.CarCompany;
import com.household.household.enums.CarFuelType;
import com.household.household.exception.ExcelImportException;
import com.household.household.repository.UsedCarDataRepository;
import com.household.household.service.UsedCarDataImportService;
import com.household.household.service.ExcelNormalizationService;
import com.household.household.service.DuplicateDetectionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsedCarDataImportServiceImpl implements UsedCarDataImportService {

    private final UsedCarDataRepository usedCarDataRepository;
    private final ExcelNormalizationService excelNormalizationService;
    private final DuplicateDetectionService duplicateDetectionService;

    private static final String[] EXPECTED_HEADERS = {
            "company",
            "model name",
            "variant",
            "launch year",
            "fuel type",
            "launching price"
    };

    @Override
    @Transactional
    public ExcelImportResponse importExcel(MultipartFile file) {

        validateFile(file);

        List<ExcelImportResponse.RowInfo> errors = new ArrayList<>();
        List<ExcelImportResponse.RowInfo> duplicateRowsResponse = new ArrayList<>();
        List<UsedCarData> validRecords = new ArrayList<>();

        int totalRows = 0;
        int duplicateCount = 0;

        Set<String> excelRowHashes = new HashSet<>();
        Set<String> allGeneratedHashes = new HashSet<>();
        Map<String, Integer> hashToRowNumberMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            /*
             * Header validation
             */
            validateHeaders(sheet);

            /*
             * Start from row 1 because row 0 is header
             */
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                int excelRowNumber = i + 1;

                try {

                    UsedCarData usedCarData = convertRowToEntity(row);

                    validateData(usedCarData);

                    /*
                     * Normalize
                     */
                    usedCarData = excelNormalizationService.normalize(usedCarData);

                    /*
                     * Generate Hash
                     */
                    String rowHash = duplicateDetectionService.generateRowHash(usedCarData);
                    usedCarData.setRowHash(rowHash);

                    /*
                     * Intra-file Duplicate check
                     */
                    if (!excelRowHashes.add(rowHash)) {
                        duplicateCount++;
                        duplicateRowsResponse.add(
                                ExcelImportResponse.RowInfo.builder()
                                        .excelRowNumber(excelRowNumber)
                                        .reason("Duplicate row found within uploaded Excel file")
                                        .build()
                        );
                        continue;
                    }

                    allGeneratedHashes.add(rowHash);
                    hashToRowNumberMap.put(rowHash, excelRowNumber);
                    validRecords.add(usedCarData);

                } catch (Exception e) {

                    errors.add(
                            ExcelImportResponse.RowInfo.builder()
                                    .excelRowNumber(excelRowNumber)
                                    .reason(e.getMessage())
                                    .build()
                    );
                }
            }

            /*
             * Database Duplicate Check
             */
            Set<String> existingDbHashes = new HashSet<>();
            if (!allGeneratedHashes.isEmpty()) {
                existingDbHashes = usedCarDataRepository.findRowHashByRowHashIn(allGeneratedHashes);
            }

            List<UsedCarData> finalRecordsToSave = new ArrayList<>();
            for (UsedCarData record : validRecords) {
                if (existingDbHashes.contains(record.getRowHash())) {
                    duplicateCount++;
                    duplicateRowsResponse.add(
                            ExcelImportResponse.RowInfo.builder()
                                    .excelRowNumber(hashToRowNumberMap.get(record.getRowHash()))
                                    .reason("Duplicate row already exists in database")
                                    .build()
                    );
                } else {
                    finalRecordsToSave.add(record);
                }
            }

            /*
             * Save valid unique records
             */
            if (!finalRecordsToSave.isEmpty()) {
                usedCarDataRepository.saveAll(finalRecordsToSave);
            }

            return ExcelImportResponse.builder()
                    .success(true)
                    .message("Excel import completed successfully")
                    .totalRows(totalRows)
                    .insertedRows(finalRecordsToSave.size())
                    .duplicateRows(duplicateCount)
                    .failedRows(errors.size())
                    .duplicates(duplicateRowsResponse)
                    .errors(errors)
                    .build();

        } catch (ExcelImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelImportException(
                    "Failed to import Excel file: " + e.getMessage(),
                    e
            );
        }
    }

    /*
     * File validation
     */
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ExcelImportException(
                    "Excel file is required"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                (!fileName.toLowerCase().endsWith(".xlsx")
                        && !fileName.toLowerCase().endsWith(".xls"))) {

            throw new ExcelImportException(
                    "Only .xlsx and .xls Excel files are allowed"
            );
        }
    }

    /*
     * Header validation
     */
    private void validateHeaders(Sheet sheet) {

        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new ExcelImportException(
                    "Excel header row is missing"
            );
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {

            Cell cell = headerRow.getCell(i);

            String actualHeader = getStringValue(cell);

            String expectedHeader = EXPECTED_HEADERS[i];

            if (actualHeader == null ||
                    !actualHeader.trim()
                            .equalsIgnoreCase(expectedHeader)) {

                throw new ExcelImportException(
                        "Invalid header at column "
                                + (i + 1)
                                + ". Expected: "
                                + expectedHeader
                                + ", Found: "
                                + actualHeader
                );
            }
        }
    }

    /*
     * Excel row -> Entity
     */
    private UsedCarData convertRowToEntity(Row row) {

        return UsedCarData.builder()
                .company(
                        parseCarCompany(getStringValue(
                                row.getCell(0)
                        ))
                )
                .modelName(
                        getStringValue(
                                row.getCell(1)
                        )
                )
                .variant(
                        getStringValue(
                                row.getCell(2)
                        )
                )
                .launchYear(
                        getIntegerValue(
                                row.getCell(3)
                        )
                )
                .fuelType(
                        parseFuelType(getStringValue(
                                row.getCell(4)
                        ))
                )
                .launchingPrice(
                        getBigDecimalValue(
                                row.getCell(5)
                        )
                )
                .build();
    }

    /*
     * Data validation
     */
    private void validateData(UsedCarData data) {

        if (data.getCompany() == null) {
            throw new ExcelImportException(
                    "Company is required"
            );
        }

        if (data.getModelName() == null ||
                data.getModelName().isBlank()) {
            throw new ExcelImportException(
                    "Model Name is required"
            );
        }

        if (data.getVariant() == null ||
                data.getVariant().isBlank()) {
            throw new ExcelImportException(
                    "Variant is required"
            );
        }

        if (data.getLaunchYear() == null) {
            throw new ExcelImportException(
                    "Launch Year is required"
            );
        }

        if (data.getFuelType() == null) {
            throw new ExcelImportException(
                    "Fuel Type is required"
            );
        }

        if (data.getLaunchingPrice() != null &&
                data.getLaunchingPrice().compareTo(
                        BigDecimal.ZERO
                ) < 0) {
            throw new ExcelImportException(
                    "Launching Price cannot be negative"
            );
        }
    }

    /*
     * String value
     */
    private String getStringValue(Cell cell) {

        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    /*
     * Integer value
     */
    private Integer getIntegerValue(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        String value = getStringValue(cell);

        if (value == null) {
            return null;
        }

        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ExcelImportException(
                    "Invalid integer value: " + value
            );
        }
    }

    /*
     * BigDecimal value
     */
    private BigDecimal getBigDecimalValue(Cell cell) {

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(
                    cell.getNumericCellValue()
            );
        }

        String value = getStringValue(cell);

        if (value == null) {
            return null;
        }

        try {
            value = value.replace(",", "");
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ExcelImportException(
                    "Invalid decimal value: " + value
            );
        }
    }

    private CarCompany parseCarCompany(String value) {
        if (value == null) return null;
        try {
            return CarCompany.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ExcelImportException(e.getMessage());
        }
    }

    private CarFuelType parseFuelType(String value) {
        if (value == null) return null;
        try {
            return CarFuelType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ExcelImportException(e.getMessage());
        }
    }

    /*
     * Empty row check
     */
    private boolean isEmptyRow(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            String value = getStringValue(cell);

            if (value != null && !value.isBlank()) {
                return false;
            }
        }

        return true;
    }
}
