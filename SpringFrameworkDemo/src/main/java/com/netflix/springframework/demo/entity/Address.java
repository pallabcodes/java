package com.netflix.springframework.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Address - Embeddable Entity for @Embedded and @MapsId
 * 
 * This embeddable entity demonstrates Netflix production-grade JPA embedding implementation:
 * 1. @Embeddable annotation for value objects
 * 2. @Embedded usage in parent entities
 * 3. @AttributeOverride for column mapping customization
 * 4. Comprehensive validation on embedded fields
 * 5. Immutable value object pattern
 * 
 * For C/C++ engineers:
 * - @Embeddable is like value objects in C++
 * - @Embedded is like composition in C++
 * - @AttributeOverride is like custom field mapping
 * - Value objects are like structs in C++
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Embeddable
public class Address {
    
    @NotBlank(message = "Street address is required")
    @Size(max = 200, message = "Street address must not exceed 200 characters")
    @Column(name = "street_address", length = 200)
    private String streetAddress;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "City must contain only letters and spaces")
    @Column(name = "city", length = 100)
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "State must contain only letters and spaces")
    @Column(name = "state", length = 100)
    private String state;
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Pattern(regexp = "^[0-9\\-]+$", message = "Postal code must contain only numbers and hyphens")
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Country must contain only letters and spaces")
    @Column(name = "country", length = 100)
    private String country;
    
    /**
     * Default constructor
     */
    public Address() {
    }
    
    /**
     * Constructor with all fields
     * 
     * @param streetAddress Street address
     * @param city City
     * @param state State
     * @param postalCode Postal code
     * @param country Country
     */
    public Address(String streetAddress, String city, String state, String postalCode, String country) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    // Getters and Setters
    
    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    /**
     * Get formatted address
     * 
     * @return formatted address string
     */
    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s", 
                           streetAddress, city, state, postalCode, country);
    }
    
    /**
     * Check if address is complete
     * 
     * @return true if all required fields are present
     */
    public boolean isComplete() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               postalCode != null && !postalCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }
    
    /**
     * Get address summary
     * 
     * @return address summary
     */
    public String getSummary() {
        return String.format("%s, %s", city, state);
    }
    
    /**
     * equals method for value object comparison
     * 
     * @param obj Object to compare
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Address address = (Address) obj;
        return Objects.equals(streetAddress, address.streetAddress) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }
    
    /**
     * hashCode method for value object hashing
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(streetAddress, city, state, postalCode, country);
    }
    
    /**
     * toString method for debugging
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "Address{" +
                "streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
