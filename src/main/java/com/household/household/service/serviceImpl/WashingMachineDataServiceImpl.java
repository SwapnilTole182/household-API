package com.household.household.service.serviceImpl;

import com.household.household.dto.response.WashingMachineDataListResponse;
import com.household.household.entity.WashingMachineData;
import com.household.household.repository.WashingMachineDataRepository;
import com.household.household.service.WashingMachineDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WashingMachineDataServiceImpl implements WashingMachineDataService {

    private final WashingMachineDataRepository repository;

    @Override
    public WashingMachineDataListResponse getAllWashingMachineData() {
        List<WashingMachineData> allData = repository.findAll();
        return WashingMachineDataListResponse.builder()
                .totalCount(allData.size())
                .data(allData)
                .build();
    }
}
