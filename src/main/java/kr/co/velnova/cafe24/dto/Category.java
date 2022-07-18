package kr.co.velnova.cafe24.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class Category {
        Long categoryNo;
//        Character recommend;
//        @JsonProperty("new")
//        Character _new;
}
