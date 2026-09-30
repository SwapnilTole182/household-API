package com.household.household.controller;

import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.service.UsedCarDataImportService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/used-car")
@RequiredArgsConstructor
public class UsedCarDataImportController {

    private final UsedCarDataImportService usedCarDataImportService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<ExcelImportResponse> importExcel(
            @RequestParam("file") MultipartFile file) {

        ExcelImportResponse response =
                usedCarDataImportService.importExcel(file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
