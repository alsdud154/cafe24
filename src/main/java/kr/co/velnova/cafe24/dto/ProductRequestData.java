package kr.co.velnova.cafe24.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ProductRequestData {
    private Character display;
    private Character selling;
    private String productName;
    private Integer price;
    private Integer supplyPrice;
    private String description;
    private String detailImage;
    private Character shippingFeeByProduct;
    private Character shippingFeeType;
    private List<Category> addCategoryNo;
    private String customProductCode;
}
