package mosbach.dhbw.de.stockwizzard.model.alexa;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.HashMap;
import java.util.Map;

// This class represents the session in the Alexa request-response mechanism.

@JsonTypeName(value = SessionRO.TYPENAME)
public class SessionRO {
    // A constant that defines the type name for this class.

    protected final static String TYPENAME = "SessionRO";

    // Additional properties associated with the session.

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor.

    public SessionRO() {
        super();
    }

    // Getters and Setters for the additional properties.

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
