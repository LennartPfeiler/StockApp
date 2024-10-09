
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
public class TransactionContent {

    @JsonProperty("transactionType")
    private Integer transactionType;
    @JsonProperty("stockAmount")
    private Double stockAmount;
    @JsonProperty("date")
    private String date;
    @JsonProperty("pricePerStock")
    private Double pricePerStock;
    @JsonProperty("totalPrice")
    private Double totalPrice;
    @JsonProperty("email")
    private String email;
    @JsonProperty("symbol")
    private String symbol;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public TransactionContent() {
    }

    /**
     * 
     * @param transactionType
     * @param stockAmount
     * @param date
     * @param symbol
     * @param pricePerStock
     * @param totalPrice
     * @param email
     */
    public TransactionContent(Integer transactionType, Double stockAmount, String date, Double pricePerStock, Double totalPrice, String email, String symbol) {
        super();
        this.transactionType = transactionType;
        this.stockAmount = stockAmount;
        this.date = date;
        this.pricePerStock = pricePerStock;
        this.totalPrice = totalPrice;
        this.email = email;
        this.symbol = symbol;
    }

    @JsonProperty("transactionType")
    public Integer getTransactionType() {
        return transactionType;
    }

    @JsonProperty("transactionType")
    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    @JsonProperty("stockAmount")
    public Double getStockAmount() {
        return stockAmount;
    }

    @JsonProperty("stockAmount")
    public void setStockAmount(Double stockAmount) {
        this.stockAmount = stockAmount;
    }

    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    @JsonProperty("pricePerStock")
    public Double getPricePerStock() {
        return pricePerStock;
    }

    @JsonProperty("pricePerStock")
    public void setPricePerStock(Double pricePerStock) {
        this.pricePerStock = pricePerStock;
    }

    @JsonProperty("totalPrice")
    public Double getTotalPrice() {
        return totalPrice;
    }

    @JsonProperty("totalPrice")
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("symbol")
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty("symbol")
    public void setSymbol(String symbol) {
        this.symbol = symbol;
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
