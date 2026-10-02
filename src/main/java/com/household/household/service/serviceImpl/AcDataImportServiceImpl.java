package com.household.household.service.serviceImpl;

import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.entity.AcData;
import com.household.household.enums.AcType;
import com.household.household.enums.AcBrand;
import com.household.household.enums.AcInverterType;
import com.household.household.exception.ExcelImportException;
import com.household.household.repository.AcDataRepository;
import com.household.household.service.AcDataImportService;
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
public class AcDataImportServiceImpl implements AcDataImportService {

    private final AcDataRepository acDataRepository;
    private final ExcelNormalizationService excelNormalizationService;
    private final DuplicateDetectionService duplicateDetectionService;

    private static final String[] EXPECTED_HEADERS = {
            "year",
            "brand",
            "model name",
            "ac type",
            "capacity in ton",
            "inverter/non-inverter",
            "star rating",
            "launching price"
    };

    @Override
    @Transactional
    public ExcelImportResponse importExcel(MultipartFile file) {

        validateFile(file);

        List<ExcelImportResponse.RowInfo> errors = new ArrayList<>();
        List<ExcelImportResponse.RowInfo> duplicateRowsResponse = new ArrayList<>();
        List<AcData> validRecords = new ArrayList<>();

        int totalRows = 0;
        int duplicateCount = 0;

        Set<String> excelRowHashes = new HashSet<>();
        Set<String> allGeneratedHashes = new HashSet<>();
        Map<String, Integer> hashToRowNumberMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);


            //Execel Header validation
            validateHeaders(sheet);

            //Start from row 1 because row 0 is header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (isEmptyRow(row)) {
                    continue;
                }

                totalRows++;
                int excelRowNumber = i + 1;

                try {

                    AcData acData = convertRowToEntity(row);

                    validateData(acData);


                    //Normalize
                    acData = excelNormalizationService.normalize(acData);


                    //Generate Hash
                    String rowHash = duplicateDetectionService.generateRowHash(acData);
                    acData.setRowHash(rowHash);


                    //Intra-file Duplicate check
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
                    validRecords.add(acData);

                } catch (Exception e) {

                    errors.add(
                            ExcelImportResponse.RowInfo.builder()
                                    .excelRowNumber(excelRowNumber)
                                    .reason(e.getMessage())
                                    .build()
                    );
                }
            }


            //Database Duplicate Check
            Set<String> existingDbHashes = new HashSet<>();
            if (!allGeneratedHashes.isEmpty()) {
                existingDbHashes = acDataRepository.findRowHashByRowHashIn(allGeneratedHashes);
            }

            List<AcData> finalRecordsToSave = new ArrayList<>();
            for (AcData record : validRecords) {
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

            //Save valid unique records
            if (!finalRecordsToSave.isEmpty()) {
                acDataRepository.saveAll(finalRecordsToSave);
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

    //File validation
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

    //Header validation
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

    //Excel row -> Entity
    private AcData convertRowToEntity(Row row) {

        return AcData.builder()

                .year(
                        getIntegerValue(
                                row.getCell(0)
                        )
                )

                .brand(
                        AcBrand.fromString(getStringValue(
                                row.getCell(1)
                        ))
                )

                .modelName(
                        getStringValue(
                                row.getCell(2)
                        )
                )

                .acType(
                        parseAcType(
                                getStringValue(
                                        row.getCell(3)
                                )
                        )
                )

                .capacityInTon(
                        getBigDecimalValue(
                                row.getCell(4)
                        )
                )

                .inverterNonInverter(
                        parseInverterType(
                                getStringValue(
                                        row.getCell(5)
                                )
                        )
                )

                .starRating(
                        getIntegerValue(
                                row.getCell(6)
                        )
                )

                .launchingPrice(
                        getBigDecimalValue(
                                row.getCell(7)
                        )
                )

                .build();
    }

    //Data validation
    private void validateData(AcData data) {

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

        if (data.getModelName() == null ||
                data.getModelName().isBlank()) {

            throw new ExcelImportException(
                    "Model Name is required"
            );
        }

        if (data.getAcType() == null) {

            throw new ExcelImportException(
                    "AC Type is required"
            );
        }

        if (data.getCapacityInTon() == null) {

            throw new ExcelImportException(
                    "Capacity in Ton is required"
            );
        }

        if (data.getStarRating() != null &&
                (data.getStarRating() < 0 ||
                        data.getStarRating() > 5)) {

            throw new ExcelImportException(
                    "Star Rating must be between 1 and 5"
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



    //String value
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

    //Integer value
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

    private AcType parseAcType(String value) {
        if (value == null) return null;
        try {
            return AcType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ExcelImportException(e.getMessage());
        }
    }

    private AcInverterType parseInverterType(String value) {
        if (value == null) return null;
        try {
            return AcInverterType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ExcelImportException(e.getMessage());
        }
    }

    //Empty row check
    private boolean isEmptyRow(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < 8; i++) {

            Cell cell = row.getCell(i);

            String value = getStringValue(cell);

            if (value != null && !value.isBlank()) {
                return false;
            }
        }

        return true;
    }
}