
package dhbw.mosbach.de.stockwizzard.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortfolioStock {

    @JsonProperty("portfolioid")
    private Integer portfolioID;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("stockamount")
    private Double stockAmount;
    @JsonProperty("boughtvalue")
    private Double boughtValue;
    @JsonProperty("currentvalue")
    private Double currentValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public PortfolioStock() {
    }

    /**
     * 
     * @param portfolioID
     * @param symbol
     * @param stockAmount
     * @param boughtValue
     * @param currentValue
     */
    public PortfolioStock(Integer portfolioID, String symbol, Double stockAmount, Double boughtValue, Double currentValue) {
        super();
        this.portfolioID = portfolioID;
        this.symbol = symbol;
        this.stockAmount = stockAmount;
        this.boughtValue = boughtValue;
        this.currentValue = currentValue;
    }

    @JsonProperty("portfolioid")
    public Integer getPortfolioID() {
        return portfolioID;
    }

    @JsonProperty("portfolioid")
    public void setPortfolioID(Integer portfolioID) {
        this.portfolioID = portfolioID;
    }

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty("symbol")
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("stockamount")
    public Double getStockAmount() {
        return stockAmount;
    }

    @JsonProperty("stockamount")
    public void setStockAmount(Double stockAmount) {
        this.stockAmount = stockAmount;
    }

    @JsonProperty("boughtvalue")
    public Double getBoughtValue() {
        return boughtValue;
    }

    @JsonProperty("boughtvalue")
    public void setBoughtValue(Double boughtValue) {
        this.boughtValue = boughtValue;
    }

    @JsonProperty("currentvalue")
    public Double getCurrentValue() {
        return currentValue;
    }

    @JsonProperty("currentvalue")
    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
