package com.household.household.dto.response;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportResponse {

    private boolean success;

    private String message;

    private int totalRows;

    private int insertedRows;

    private int duplicateRows;

    private int failedRows;

    private List<RowInfo> duplicates;

    private List<RowInfo> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowInfo {

        private int excelRowNumber;

        private String reason;
    }
}
