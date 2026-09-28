package com.household.household.service.serviceImpl;

import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.entity.WashingMachineData;
import com.household.household.enums.WashingMachineBrand;
import com.household.household.enums.WashingType;
import com.household.household.enums.WashingMachineLoadingType;
import com.household.household.exception.ExcelImportException;
import com.household.household.repository.WashingMachineDataRepository;
import com.household.household.service.WashingMachineDataImportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WashingMachineDataImportServiceImpl implements WashingMachineDataImportService {

    private final WashingMachineDataRepository washingMachineDataRepository;

    private static final String[] EXPECTED_HEADERS = {
            "year",
            "brand",
            "model number",
            "washing type",
            "capacity (kg)",
            "loading type",
            "launching price"
    };

    @Override
    @Transactional
    public ExcelImportResponse importExcel(MultipartFile file) {

        validateFile(file);

        List<ExcelImportResponse.RowInfo> errors = new ArrayList<>();
        List<ExcelImportResponse.RowInfo> duplicateRowsResponse = new ArrayList<>();
        List<WashingMachineData> validRecords = new ArrayList<>();

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

                    WashingMachineData wmData = convertRowToEntity(row);

                    validateData(wmData);

                    /*
                     * Normalize
                     */
                    wmData = normalize(wmData);

                    /*
                     * Generate Hash from raw row
                     */
                    String rowHash = generateRowHash(row);
                    wmData.setRowHash(rowHash);

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
                    validRecords.add(wmData);

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
                existingDbHashes = washingMachineDataRepository.findRowHashByRowHashIn(allGeneratedHashes);
            }

            List<WashingMachineData> finalRecordsToSave = new ArrayList<>();
            for (WashingMachineData record : validRecords) {
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
                washingMachineDataRepository.saveAll(finalRecordsToSave);
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
    private WashingMachineData convertRowToEntity(Row row) {

        return WashingMachineData.builder()

                .year(
                        getIntegerValue(
                                row.getCell(0)
                        )
                )

                .brand(
                        WashingMachineBrand.fromString(getStringValue(
                                row.getCell(1)
                        ))
                )

                .modelNumber(
                        getStringValue(
                                row.getCell(2)
                        )
                )

                .washingType(
                        parseWashingType(
                                getStringValue(
                                        row.getCell(3)
                                )
                        )
                )

                .capacityKg(
                        getBigDecimalValue(
                                row.getCell(4)
                        )
                )

                .loadingType(
                        parseLoadingType(
                                getStringValue(
                                        row.getCell(5)
                                )
                        )
                )

                .launchingPrice(
                        getBigDecimalValue(
                                row.getCell(6)
                        )
                )

                .build();
    }

    /*
     * Data validation
     */
    private void validateData(WashingMachineData data) {

        if (data.getYear() == null) {
            throw new ExcelImportException(
                    "Year is required"
            );
        }

        if (data.getBrand() == null) {

            throw new ExcelImportException(
                    "Brand is required"
            );
        }

        if (data.getModelNumber() == null ||
                data.getModelNumber().isBlank()) {

            throw new ExcelImportException(
                    "Model Number is required"
            );
        }

        if (data.getWashingType() == null) {

            throw new ExcelImportException(
                    "Washing Type is required"
            );
        }

        if (data.getCapacityKg() == null) {

            throw new ExcelImportException(
                    "Capacity (Kg) is required"
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
     * Normalize
     */
    private WashingMachineData normalize(WashingMachineData data) {
        if (data == null) {
            return null;
        }

        data.setModelNumber(normalizeString(data.getModelNumber()));
        data.setCapacityKg(normalizeNumeric(data.getCapacityKg()));
        data.setLaunchingPrice(normalizeNumeric(data.getLaunchingPrice()));

        return data;
    }

    private String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal normalizeNumeric(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.stripTrailingZeros();
    }

    /*
     * Generate Row Hash from raw Excel row
     */
    private String generateRowHash(Row row) {
        if (row == null) {
            return null;
        }

        StringBuilder signature = new StringBuilder();
        int maxCol = Math.max(7, row.getLastCellNum());
        DataFormatter formatter = new DataFormatter();
        
        for (int i = 0; i < maxCol; i++) {
            Cell cell = row.getCell(i);
            String rawValue = cell != null ? formatter.formatCellValue(cell) : "";
            signature.append(rawValue).append("|");
        }

        return generateSHA256(signature.toString());
    }

    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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

    private WashingType parseWashingType(String value) {
        if (value == null) return null;
        try {
            return WashingType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ExcelImportException(e.getMessage());
        }
    }

    private WashingMachineLoadingType parseLoadingType(String value) {
        if (value == null) return null;
        try {
            return WashingMachineLoadingType.fromString(value);
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

        for (int i = 0; i < 7; i++) {

            Cell cell = row.getCell(i);

            String value = getStringValue(cell);

            if (value != null && !value.isBlank()) {
                return false;
            }
        }

        return true;
    }
}
