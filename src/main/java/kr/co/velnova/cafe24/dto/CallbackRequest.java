package kr.co.velnova.cafe24.dto;

import lombok.Data;

@Data
public class CallbackRequest {
    private String code;
    private String state;
}
