package com.household.household.service;

import com.household.household.dto.response.ExcelImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface WashingMachineDataImportService {

    ExcelImportResponse importExcel(MultipartFile file);
}
