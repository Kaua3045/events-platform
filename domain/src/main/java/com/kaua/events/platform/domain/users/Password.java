package com.kaua.events.platform.domain.users;

import com.kaua.events.platform.domain.ValueObject;

import java.util.regex.Pattern;

public class Password implements ValueObject {

    private static final String PASSWORD = "password";

    private static final String PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).*$")
            .pattern();

    private String value;

    private Password(final String aValue) {
        this.value = this.assertArgumentNotEmpty(aValue, PASSWORD, "should not be empty");
    }

    public static Password create(String aPassword, PasswordEncryption encryption) {
        final var aPasswordObject = new Password(aPassword);
        aPasswordObject.assertArgumentNotEmpty(aPassword, PASSWORD, "should not be empty");
        aPasswordObject.assertArgumentMinLength(aPassword, 8, PASSWORD, "should have at least 8 characters");
        aPasswordObject.assertArgumentMaxLength(aPassword, 255, PASSWORD, "should have at most 255 characters");
        aPasswordObject.assertArgumentPattern(aPassword, PASSWORD_PATTERN, PASSWORD, "should have at least one lowercase letter, one uppercase letter, one digit and one special character");
        aPasswordObject.setValue(encryption.encrypt(aPassword));
        return aPasswordObject;
    }

    public static Password of(final String aValue) {
        return new Password(aValue);
    }

    public boolean matches(final String aRawPassword, final PasswordEncryption encryption) {
        return encryption.matches(aRawPassword, this.value);
    }

    public String value() {
        return value;
    }

    private void setValue(final String value) {
        this.value = this.assertArgumentNotEmpty(value, PASSWORD, "should not be empty");;
    }
}
