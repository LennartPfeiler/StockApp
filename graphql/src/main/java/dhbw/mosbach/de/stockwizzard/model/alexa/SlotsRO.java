package dhbw.mosbach.de.stockwizzard.model.alexa;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.HashMap;
import java.util.Map;

@JsonTypeName(value = SlotsRO.TYPENAME)
public class SlotsRO {
    protected final static String TYPENAME = "SlotRO";

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    // Additional properties that might not be mapped to the predefined fields
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Getters and Setters

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
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
