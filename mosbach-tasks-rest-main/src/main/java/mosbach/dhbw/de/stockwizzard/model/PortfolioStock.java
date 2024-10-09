
package mosbach.dhbw.de.stockwizzard.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Portfolio {

    @JsonProperty("portfolioID")
    private Integer portfolioID;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("stockAmount")
    private Double stockAmount;
    @JsonProperty("boughtValue")
    private Double boughtValue;
    @JsonProperty("currentValue")
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

    @JsonProperty("portfolioID")
    public Integer getPortfolioID() {
        return portfolioID;
    }

    @JsonProperty("portfolioID")
    public void setPortfolioID(Integer portfolioID) {
        this.portfolioID = portfolioID;
    }

    @JsonProperty("symbol")
    public Double getSymbol() {
        return symbol;
    }

    @JsonProperty("symbol")
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("stockAmount")
    public Double getStockAmount() {
        return stockAmount;
    }

    @JsonProperty("stockAmount")
    public void setStockAmount(Double stockAmount) {
        this.stockAmount = stockAmount;
    }

    @JsonProperty("boughtValue")
    public Double getBoughtValue() {
        return boughtValue;
    }

    @JsonProperty("boughtValue")
    public void setBoughtValue() {
        this.boughtValue = boughtValue;
    }

    @JsonProperty("currentValue")
    public Double getCurrentValue() {
        return currentValue;
    }

    @JsonProperty("currentValue")
    public void setCurrentValue() {
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
