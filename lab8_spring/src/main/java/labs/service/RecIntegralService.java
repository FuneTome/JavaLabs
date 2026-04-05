package labs.service;

import labs.model.RecIntegral;
import org.springframework.stereotype.Service;

@Service
public class RecIntegralService {
    public RecIntegral calculate(RecIntegral recIntegral) {
        recIntegral.result();
        return recIntegral;
    }
}
