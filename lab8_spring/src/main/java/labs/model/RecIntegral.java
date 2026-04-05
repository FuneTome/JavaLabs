package labs.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecIntegral implements Serializable {
    private static final long serialVersionUID = 1L;
    private double lowerLimit, upperLimit, step, result;

    public RecIntegral(double lowerLimit, double upperLimit, double step) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.step = step;
    }

    public void result() {
        double res = 0;
        double ai = 0;
        double ll = lowerLimit;
        for (ai = (ll + step); ai <= upperLimit; ai += step) {
            res += (((f(ll) + f(ai)) / 2) * step);
            ll += step;
        }
        res += (((f(ai) + f(upperLimit)) / 2) * ((upperLimit - lowerLimit) % step));
        this.result = res;
    }

    public double f(double x){
        return 1/Math.log(x);
    }
}
