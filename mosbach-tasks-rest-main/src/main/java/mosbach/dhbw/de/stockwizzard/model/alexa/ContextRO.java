package com.mosbach.demo.model.alexa;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.HashMap;
import java.util.Map;

// This class represents the context in the Alexa response object.
@JsonTypeName(value = ContextRO.TYPENAME)
public class ContextRO
{
    // A constant that defines the type name for this class.
    protected final static String TYPENAME = "ContextRO";

    // Additional properties that might not be mapped to the predefined fields but can be captured dynamically.
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor.
    public ContextRO()
    {
        super();
    }

    // Getter and Setter for the additional properties.
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
