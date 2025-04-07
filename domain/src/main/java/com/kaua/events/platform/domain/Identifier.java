package com.kaua.events.platform.domain;

public interface Identifier<T> extends ValueObject {

    T value();
}
