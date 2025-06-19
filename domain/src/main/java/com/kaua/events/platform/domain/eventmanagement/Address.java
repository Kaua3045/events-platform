package com.kaua.events.platform.domain.eventmanagement;

import com.kaua.events.platform.domain.ValueObject;

import java.util.Optional;

public class Address implements ValueObject {

    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private Address(
            final String street,
            final String number,
            final String complement,
            final String neighborhood,
            final String city,
            final String state,
            final String postalCode,
            final String country
    ) {
        setStreet(street);
        setNumber(number);
        setComplement(complement);
        setNeighborhood(neighborhood);
        setCity(city);
        setState(state);
        setPostalCode(postalCode);
        setCountry(country);
    }

    public static Address newAddress(
            final String street,
            final String number,
            final String complement,
            final String neighborhood,
            final String city,
            final String state,
            final String postalCode,
            final String country
    ) {
        return new Address(
                street,
                number,
                complement,
                neighborhood,
                city,
                state,
                postalCode,
                country
        );
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public Optional<String> getComplement() {
        return Optional.ofNullable(complement);
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    private void setStreet(final String street) {
        this.assertArgumentNotEmpty(street, "street", "should not be empty");
        this.street = street;
    }

    private void setNumber(final String number) {
        this.assertArgumentNotEmpty(number, "number", "should not be empty");
        this.number = number;
    }

    private void setComplement(final String complement) {
        this.assertArgumentMaxLength(complement, 100, "complement", "can only have a maximum of 100 characters");
        this.complement = complement;
    }

    private void setNeighborhood(final String neighborhood) {
        this.assertArgumentNotEmpty(neighborhood, "neighborhood", "should not be empty");
        this.assertArgumentMaxLength(neighborhood, 100, "neighborhood", "can only have a maximum of 100 characters");
        this.neighborhood = neighborhood;
    }

    private void setCity(final String city) {
        this.assertArgumentNotEmpty(city, "city", "should not be empty");
        this.assertArgumentMaxLength(city, 100, "city", "can only have a maximum of 100 characters");
        this.city = city;
    }

    private void setState(final String state) {
        this.assertArgumentNotEmpty(state, "state", "should not be empty");
        this.assertArgumentMaxLength(state, 60, "state", "can only have a maximum of 60 characters");
        this.state = state;
    }

    private void setPostalCode(final String postalCode) {
        this.assertArgumentNotEmpty(postalCode, "postalCode", "should not be empty");
        this.assertArgumentMaxLength(postalCode, 20, "postalCode", "can only have a maximum of 20 characters");
        this.postalCode = postalCode;
    }

    private void setCountry(final String country) {
        this.assertArgumentNotEmpty(country, "country", "should not be empty");
        this.assertArgumentMaxLength(country, 10, "country", "can only have a maximum of 10 characters");
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address(" +
                "street='" + street + '\'' +
                ", number='" + number + '\'' +
                ", complement='" + complement + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ')';
    }
}
