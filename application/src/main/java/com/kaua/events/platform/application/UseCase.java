package com.kaua.events.platform.application;

public abstract class UseCase<I, O> {

    public abstract O execute(I input);
}
