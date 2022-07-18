package kr.co.velnova.cafe24.api;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import kr.co.velnova.cafe24.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Api(tags = "CAFE24")
@Slf4j
@RequestMapping("/cafe24")
@RestController
public class Cafe24Api {

    private final String mallId;

    @Value("${cafe24.clientId}")
    private String clientId;

    @Value("${cafe24.clientSecret}")
    private String clientSecret;

    @Value("${cafe24.redirectUri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String cafe24ApiUrl;

    private ObjectMapper objectMapper;

    {
        objectMapper = JsonMapper.builder() // or different mapper for other format
                .addModule(new JavaTimeModule())
                .build();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    }

    public Cafe24Api(@Value("${cafe24.mallId}") String mallId) {
        this.mallId = mallId;
        this.cafe24ApiUrl = "https://" + mallId + ".cafe24api.com";
    }

    /**
     * code 발급 url 예
     * https://{mallid}.cafe24api.com/api/v2/oauth/authorize?response_type=code&client_id={client_id}&state=state&redirect_uri={redirect_uri}&scope=mall.write_product
     * https://alsdud154.cafe24api.com/api/v2/oauth/authorize?response_type=code&client_id=5wg1bNmUFFBjWv1YSVYEzB&state=state&redirect_uri=https://cafe24.velnova.co.kr/cafe24/callback&scope=mall.write_product
     * <p>
     * 콜백 받은 데이터(code)를 사용하여 cafe24 access token을 발급받는다.
     *
     * @param callbackRequest cafe24 Authentication Code 요청 뒤 callback 데이터
     * @return access token
     */
    @Operation(summary = "콜백 받은 데이터(code)를 사용하여 cafe24 access token을 발급")
    @GetMapping("/callback")
    public AccessTokenInfo callback(@ModelAttribute CallbackRequest callbackRequest) {
        log.info("callbackRequest[{}]", callbackRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", callbackRequest.getCode());
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<AccessTokenInfo> result = restTemplate.exchange(getCafe24Url("/api/v2/oauth/token"), HttpMethod.POST, request, new ParameterizedTypeReference<AccessTokenInfo>() {
        });

        return result.getBody();
    }

    /**
     * 1. 상품 이미지 등록
     * 2. 상품 등록
     *
     * @param accessToken
     * @return 등록된 상품 정보
     * @throws JsonProcessingException
     */
    @Operation(summary = "상품 등록 테스트")
    @GetMapping("/product")
    public Map createProduct(@RequestParam String accessToken) throws JsonProcessingException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Cafe24-Api-Version", "2022-06-01");

        // 상품 이미지 등록(cafe24는 이미지만 업로드할 수 있으며 이미지 파일이 base64로 인코딩된 String을 보내야 한다.)
        ProductImageRequest productImageRequest = new ProductImageRequest(getByteArrayFromImageURL("https://image.dongascience.com/Photo/2020/03/5bddba7b6574b95d37b6079c199d7101.jpg"));

        String imageJsonStr = objectMapper.writeValueAsString(productImageRequest);

        HttpEntity<String> imageHttpEntity = new HttpEntity<>(imageJsonStr, headers);
        ResponseEntity<ProductImageResponse> imageResult = restTemplate.exchange(getCafe24Url("/api/v2/admin/products/images"), HttpMethod.POST, imageHttpEntity, new ParameterizedTypeReference<ProductImageResponse>() {
        });

        ProductImageResponse imageBody = imageResult.getBody();

        List<Image> images = imageBody.getImages();
        String imagePath = images.get(0).getPath().substring(28);

        log.info("imagePath = [{}]", imagePath);

        String prefix = "CCCV_";
        String nftId = "ASDFFDGEWRWFSDFSDF";

        // 상품 등록
        ProductRequest productRequest = ProductRequest.builder()
                .shopNo(1L)
                .request(ProductRequestData.builder()
                        .display('T')
                        .selling('T')
                        .productName("상품명")
                        .price(15000)
                        .supplyPrice(10000)
                        .description("설명")
                        .detailImage(imagePath)
                        .shippingFeeByProduct('T')
                        .shippingFeeType('T')
                        .addCategoryNo(Collections.singletonList(Category.builder()
                                .categoryNo(26L)
                                .build()))
                        .customProductCode(prefix + nftId)
                        .build())
                .build();

        String productJsonStr = objectMapper.writeValueAsString(productRequest);

        HttpEntity<String> productHttpEntity = new HttpEntity<>(productJsonStr, headers);
        ResponseEntity<Map> productResult = restTemplate.postForEntity(getCafe24Url("/api/v2/admin/products"), productHttpEntity, Map.class);

        return productResult.getBody();

    }

    /**
     * create cafe24 url
     *
     * @param path
     * @return cafe24 url
     */
    private String getCafe24Url(String path) {
        return cafe24ApiUrl + path;
    }

    /**
     * image url -> encoded base64 String
     *
     * @param url image url
     * @return encoded base64 String
     */
    private String getByteArrayFromImageURL(String url) {

        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();
            InputStream is = ucon.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return new Base64().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
