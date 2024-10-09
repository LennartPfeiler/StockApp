
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
public class TokenTransactionContent {

    @JsonProperty("token")
    private String token;
    @JsonProperty("transactionContent")
    private TransactionContent transactionContent;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public TokenTransactionContent() {
    }

    /**
     * 
     * @param transactionContent
     * @param token
     */
    public TokenTransactionContent(String token, TransactionContent transactionContent) {
        super();
        this.token = token;
        this.transactionContent = transactionContent;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("transactionContent")
    public TransactionContent getTransactionContent() {
        return transactionContent;
    }

    @JsonProperty("transactionContent")
    public void setTransactionContent(TransactionContent transactionContent) {
        this.transactionContent = transactionContent;
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
