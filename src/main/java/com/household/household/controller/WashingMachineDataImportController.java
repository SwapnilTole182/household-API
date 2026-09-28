package com.household.household.controller;

import com.household.household.dto.response.ExcelImportResponse;
import com.household.household.dto.response.WashingMachineDataListResponse;
import com.household.household.service.WashingMachineDataImportService;
import com.household.household.service.WashingMachineDataService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/washing-machine")
@RequiredArgsConstructor
public class WashingMachineDataImportController {

    private final WashingMachineDataImportService washingMachineDataImportService;
    private final WashingMachineDataService washingMachineDataService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<ExcelImportResponse> importExcel(
            @RequestParam("file") MultipartFile file) {

        ExcelImportResponse response =
                washingMachineDataImportService.importExcel(file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<WashingMachineDataListResponse> getAllWashingMachineData() {
        WashingMachineDataListResponse response = washingMachineDataService.getAllWashingMachineData();
        return ResponseEntity.ok(response);
    }
}
