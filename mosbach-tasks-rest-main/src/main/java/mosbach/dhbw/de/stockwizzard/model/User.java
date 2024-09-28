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
public class User {

    @JsonProperty("userID")
    private Integer userID;
    @JsonProperty("firstname")
    private String firstname;
    @JsonProperty("lastname")
    private String lastname;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public User() {
    }

    /**
     * 
     * @param firstname
     * @param password
     * @param userID
     * @param email
     * @param lastname
     */
    public User(Integer userID, String firstname, String lastname, String email, String password) {
        super();
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    @JsonProperty("userID")
    public Integer getUserID() {
        return userID;
    }

    @JsonProperty("userID")
    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    @JsonProperty("firstname")
    public String getFirstName() {
        return firstname;
    }

    @JsonProperty("firstname")
    public void setFirstName(String firstname) {
        this.firstname = firstname;
    }

    @JsonProperty("lastname")
    public String getLastName() {
        return lastname;
    }

    @JsonProperty("lastname")
    public void setLastName(String lastname) {
        this.lastname = lastname;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
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
