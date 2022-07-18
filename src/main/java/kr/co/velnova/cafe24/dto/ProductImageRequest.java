package kr.co.velnova.cafe24.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class ProductImageRequest  {

    List<Image> requests;

    public ProductImageRequest(String base64Image) {
        this.requests = Collections.singletonList(new Image(base64Image));
    }

    private class Image {
        private String image;

        public Image(String image) {
            this.image = image;
        }
    }
}
