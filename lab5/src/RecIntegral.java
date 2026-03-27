import java.io.Serializable;

public class RecIntegral implements Serializable {
    private double lowerLimit, upperLimit, step, result;

    public RecIntegral(double lowerLimit, double upperLimit, double step) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.step = step;
    }

    public RecIntegral(Main.Table o){
        this.lowerLimit = o.getLowerLimit();
        this.upperLimit = o.getUpperLimit();
        this.step = o.getSteps();
        this.result = o.getResult();
    }

    public Main.Table getTable() {
        return new Main.Table(lowerLimit, upperLimit, step, result);
    }

    public double getLowerLimit() { return lowerLimit; }
    public double getUpperLimit() { return upperLimit; }
    public double getStep() { return step; }
    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RecIntegral that = (RecIntegral) o;
        return Double.compare(lowerLimit, that.lowerLimit) == 0 && Double.compare(upperLimit, that.upperLimit) == 0 &&
                Double.compare(step, that.step) == 0 && Double.compare(result, that.result) == 0;
    }

    public double f(double x){
        return 1/Math.log(x);
    }
}
