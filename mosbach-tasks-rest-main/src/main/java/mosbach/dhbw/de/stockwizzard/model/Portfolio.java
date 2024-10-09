
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
    @JsonProperty("value")
    private Double value;
    @JsonProperty("email")
    private String email;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Portfolio() {
    }

    /**
     * 
     * @param portfolioID
     * @param value
     * @param email
     */
    public Portfolio(Integer portfolioID, Double value, String email) {
        super();
        this.portfolioID = portfolioID;
        this.value = value;
        this.email = email;
    }

    @JsonProperty("portfolioID")
    public Integer getPortfolioID() {
        return portfolioID;
    }

    @JsonProperty("portfolioID")
    public void setPortfolioID(Integer portfolioID) {
        this.portfolioID = portfolioID;
    }

    @JsonProperty("value")
    public Double getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Double value) {
        this.value = value;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
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
