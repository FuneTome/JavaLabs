import java.util.Objects;

public class RecIntegral {
    private final Main.Table table;

    public RecIntegral(Main.Table table) {
        this.table = table;
    }

    public Main.Table getTable() {
        return table;
    }

    public void result() {
        double lowerLimit = table.getLowerLimit();
        double upperLimit = table.getUpperLimit();
        double step = table.getSteps();
        double res = 0;
        double ai = 0;
        double ll = lowerLimit;
        for (ai = (ll + step); ai <= upperLimit; ai += step) {
            res += (((f(ll) + f(ai)) / 2) * step);
            ll += step;
        }
        res += (((f(ai) + f(upperLimit)) / 2) * ((upperLimit - lowerLimit) % step));
        table.resultProperty().set(res);
    }

    private double f(double x) {
        return 1 / Math.log(x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecIntegral that = (RecIntegral) o;
        return Objects.equals(table, that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(table);
    }
}