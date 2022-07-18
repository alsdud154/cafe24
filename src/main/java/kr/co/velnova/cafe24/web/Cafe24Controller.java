package kr.co.velnova.cafe24.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Cafe24Controller {
    @GetMapping("/")
    public String index() {
        return "index";
    }

}
