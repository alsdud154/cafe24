package kr.co.velnova.cafe24.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExpirationDate {
    private String startDate;
    private String endDate;
}
