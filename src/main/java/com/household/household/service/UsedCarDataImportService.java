package com.household.household.service;

import com.household.household.dto.response.ExcelImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UsedCarDataImportService {
    ExcelImportResponse importExcel(MultipartFile file);
}
