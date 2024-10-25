package dhbw.mosbach.de.stockwizzard.model.alexa;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

// This class represents the output speech in the Alexa response object.
@JsonTypeName(value = OutputSpeechRO.TYPENAME)
public class OutputSpeechRO {
    // A constant that defines the type name for this class.
    // Note: The type name seems to be mistakenly set to 'IntentRO'. It should
    // probably be 'OutputSpeechRO'.
    protected final static String TYPENAME = "IntentRO";

    // Represents the type of the speech output.
    @JsonProperty("type")
    private String type;

    // Represents the text of the speech output.
    @JsonProperty("text")
    private String text;

    // Additional properties that might not be mapped to the predefined fields but
    // can be captured dynamically.
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor.
    public OutputSpeechRO() {
        super();
    }

    // Constructor that initializes the type and text.
    public OutputSpeechRO(String type, String text) {
        super();
        this.type = type;
        this.text = text;
    }

    // Getter and Setter for type, text, and additional properties.
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
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
