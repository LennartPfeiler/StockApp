
package mosbach.dhbw.de.stockwizzard.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    @JsonProperty("transactionid")
    private Integer transactionID;
    @JsonProperty("transactiontype")
    private Integer transactionType;
    @JsonProperty("stockamount")
    private Double stockAmount;
    @JsonProperty("date")
    private Timestamp date;
    @JsonProperty("priceperstock")
    private Double pricePerStock;
    @JsonProperty("totalprice")
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
    public Transaction() {
    }

    /**
     * 
     * @param transactionType
     * @param stockAmount
     * @param date
     * @param symbol
     * @param pricePerStock
     * @param totalPrice
     * @param transactionID
     * @param email
     */
    public Transaction(Integer transactionID, Integer transactionType, Double stockAmount, Timestamp date, Double pricePerStock, Double totalPrice, String email, String symbol) {
        super();
        this.transactionID = transactionID;
        this.transactionType = transactionType;
        this.stockAmount = stockAmount;
        this.date = date;
        this.pricePerStock = pricePerStock;
        this.totalPrice = totalPrice;
        this.email = email;
        this.symbol = symbol;
    }

    @JsonProperty("transactionid")
    public Integer getTransactionID() {
        return transactionID;
    }

    @JsonProperty("transactionid")
    public void setTransactionID(Integer transactionID) {
        this.transactionID = transactionID;
    }

    @JsonProperty("transactiontype")
    public Integer getTransactionType() {
        return transactionType;
    }

    @JsonProperty("transactiontype")
    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    @JsonProperty("stockamount")
    public Double getStockAmount() {
        return stockAmount;
    }

    @JsonProperty("stockamount")
    public void setStockAmount(Double stockAmount) {
        this.stockAmount = stockAmount;
    }

    @JsonProperty("date")
    public Timestamp getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(Timestamp date) {
        this.date = date;
    }

    @JsonProperty("priceperstock")
    public Double getPricePerStock() {
        return pricePerStock;
    }

    @JsonProperty("priceperstock")
    public void setPricePerStock(Double pricePerStock) {
        this.pricePerStock = pricePerStock;
    }

    @JsonProperty("totalprice")
    public Double getTotalPrice() {
        return totalPrice;
    }

    @JsonProperty("totalprice")
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
