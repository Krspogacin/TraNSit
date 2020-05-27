package org.mad.transit.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableDto {
    private List<String> workday;
    private List<String> saturday;
    private List<String> sunday;
}
