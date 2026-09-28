package com.household.household.controller;


import com.household.household.dto.response.AcDataListResponse;
import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.service.AcDataImportService;
import com.household.household.service.AcDataService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ac")
@RequiredArgsConstructor
public class AcDataImportController {

    private final AcDataImportService acDataImportService;
    private final AcDataService acDataService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<ExcelImportResponse> importExcel(
            @RequestParam("file") MultipartFile file) {

        ExcelImportResponse response =
                acDataImportService.importExcel(file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<AcDataListResponse> getAllAcData() {
        AcDataListResponse response = acDataService.getAllAcData();
        return ResponseEntity.ok(response);
    }
}
