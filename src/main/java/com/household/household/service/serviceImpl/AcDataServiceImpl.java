package com.household.household.service.serviceImpl;

import com.household.household.dto.response.AcDataListResponse;
import com.household.household.entity.AcData;
import com.household.household.repository.AcDataRepository;
import com.household.household.service.AcDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcDataServiceImpl implements AcDataService {

    private final AcDataRepository acDataRepository;

    @Override
    public AcDataListResponse getAllAcData() {
        List<AcData> allData = acDataRepository.findAll();
        return AcDataListResponse.builder()
                .totalCount(allData.size())
                .data(allData)
                .build();
    }
}
