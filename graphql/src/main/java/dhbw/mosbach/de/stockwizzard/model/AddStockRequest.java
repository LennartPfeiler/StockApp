
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
public class AddStockRequest {

    @JsonProperty("tokenemail")
    private TokenEmail tokenEmail;
    @JsonProperty("stock")
    private Stock stock;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public AddStockRequest() {
    }

    /**
     * 
     * @param tokenEmail
     * @param stock
     */
    public AddStockRequest(TokenEmail tokenEmail, Stock stock) {
        super();
        this.tokenEmail = tokenEmail;
        this.stock = stock;
    }

    @JsonProperty("tokenemail")
    public TokenEmail getTokenEmail() {
        return tokenEmail;
    }

    @JsonProperty("tokenemail")
    public void setTokenEmail(TokenEmail tokenEmail) {
        this.tokenEmail = tokenEmail;
    }

    @JsonProperty("stock")
    public Stock getStock() {
        return stock;
    }

    @JsonProperty("stock")
    public void setStock(Stock stock) {
        this.stock = stock;
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
