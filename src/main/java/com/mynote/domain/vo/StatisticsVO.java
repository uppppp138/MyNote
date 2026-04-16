package com.mynote.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO {
    private Integer totalNotes;
    private Integer totalTags;
    private Integer totalViews;
    private Integer lastWeekAdded;
    private Map<String, Integer> categoryDistribution;
}