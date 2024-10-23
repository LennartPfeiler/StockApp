package com.mosbach.demo.model.alexa;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

// This class represents the intent in the Alexa response object.
@JsonTypeName(value = IntentRO.TYPENAME)
public class IntentRO
{
    // A constant that defines the type name for this class.
    protected final static String TYPENAME = "IntentRO";
    
    // Represents the name of the intent.
    @JsonProperty("name")
    private String name;


    // Additional properties that might not be mapped to the predefined fields but can be captured dynamically.
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Default constructor.
    public IntentRO()
    {
        super();
    }

    // Constructor that initializes the name.
    public IntentRO(String name)
    {
        super();
        this.name = name;
    }

    // Getter and Setter for the name and additional properties.
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

        // A Map to hold slot names and their values
        @JsonProperty("slots")
        private Map<String, SlotsRO> slots;
    
        // Getter and setter for slots
        @JsonProperty("slots")
        public Map<String, SlotsRO> getSlots() {
            return slots;
        }
    
        @JsonProperty("slots")
        public void setSlots(Map<String, SlotsRO> slots) {
            this.slots = slots;
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
