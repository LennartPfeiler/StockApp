package mosbach.dhbw.de.stockwizzard.model.alexa;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

// This class represents the request part of the Alexa request-response mechanism.

@JsonTypeName(value = RequestRO.TYPENAME)
public class RequestRO {
    // A constant that defines the type name for this class.

    protected final static String TYPENAME = "RequestRO";

    // Various properties specific to the request.
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("type")
    private String type;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("intent")
    private IntentRO intent;
    @JsonProperty("locale")
    private String locale;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor.

    public RequestRO() {
        super();
    }

    // Constructor initializing various properties.

    public RequestRO(String type, String requestId, IntentRO intent, String locale, String timestamp) {
        super();
        this.type = type;
        this.requestId = requestId;
        this.intent = intent;
        this.locale = locale;
        this.timestamp = timestamp;
    }

    // Getters and Setters for all the properties.

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("intent")
    public IntentRO getIntent() {
        return intent;
    }

    @JsonProperty("intent")
    public void setIntent(IntentRO intent) {
        this.intent = intent;
    }

    @JsonProperty("locale")
    public String getLocale() {
        return locale;
    }

    @JsonProperty("locale")
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    @JsonAnyGetter
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

}
