package dhbw.mosbach.de.stockwizzard.model.alexa;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

// This class represents the Alexa Response Object (AlexaRO)
@JsonRootName(value = "AlexaRO")
public class AlexaRO {

    protected final static String TYPENAME = "AlexaRO";

    @JsonProperty("request")
    private RequestRO request;

    @JsonProperty("response")
    private ResponseRO response;

    @JsonProperty("version")
    private String version;

    @JsonProperty("session")
    private SessionRO session;

    @JsonProperty("context")
    private ContextRO context;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor
    public AlexaRO() {
        super();
    }

    // Constructor to initialize with a specific version
    public AlexaRO(String version) {
        super();
        this.version = version;
    }

    // Getters and Setters

    @JsonProperty("request")
    public RequestRO getRequest() {
        return request;
    }

    @JsonProperty("request")
    public void setRequest(RequestRO request) {
        this.request = request;
    }

    @JsonProperty("response")
    public ResponseRO getResponse() {
        return response;
    }

    @JsonProperty("response")
    public void setResponse(ResponseRO response) {
        this.response = response;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("session")
    public SessionRO getSession() {
        return session;
    }

    @JsonProperty("session")
    public void setSession(SessionRO session) {
        this.session = session;
    }

    @JsonProperty("context")
    public ContextRO getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(ContextRO context) {
        this.context = context;
    }

    // Getter and Setter for the additional properties
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
