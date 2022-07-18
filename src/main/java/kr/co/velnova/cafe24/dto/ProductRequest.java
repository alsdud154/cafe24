package kr.co.velnova.cafe24.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductRequest {
    private Long shopNo;
    private ProductRequestData request;
}
