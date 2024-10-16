package mosbach.dhbw.de.stockwizzard.model;

public class PortfolioStockValue {
    private Double currentValue;
    private Double boughtValue;

    public PortfolioStockValue(Double currentValue, Double boughtValue) {
        this.currentValue = currentValue;
        this.boughtValue = boughtValue;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public Double getBoughtValue() {
        return boughtValue;
    }
}
