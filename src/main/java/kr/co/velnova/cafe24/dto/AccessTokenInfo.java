package kr.co.velnova.cafe24.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class AccessTokenInfo {
    private String accessToken;
    private LocalDateTime expiresAt;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiresAt;
    private String clientId;
    private String mallId;
    private String userId;
    private List<String> scopes;
    private LocalDateTime issuedAt;
    private Long shopNo;
}
