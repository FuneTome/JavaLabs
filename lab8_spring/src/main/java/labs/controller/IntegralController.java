package labs.controller;

import labs.model.RecIntegral;
import labs.service.RecIntegralService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculate")
public class IntegralController {
    RecIntegralService recIntegralService;

    public IntegralController(RecIntegralService recIntegralService) {
        this.recIntegralService = recIntegralService;
    }

    @PostMapping
    public RecIntegral calculate(@RequestBody RecIntegral recIntegral) {
        return recIntegralService.calculate(recIntegral);
    }
}
