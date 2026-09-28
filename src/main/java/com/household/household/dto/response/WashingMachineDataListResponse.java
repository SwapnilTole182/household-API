package com.household.household.dto.response;

import com.household.household.entity.WashingMachineData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WashingMachineDataListResponse {
    private long totalCount;
    private List<WashingMachineData> data;
}
