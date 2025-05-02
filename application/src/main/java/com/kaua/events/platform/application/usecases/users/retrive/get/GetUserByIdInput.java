package com.kaua.events.platform.application.usecases.users.retrive.get;

public record GetUserByIdInput(String id) {

    public static GetUserByIdInput with(final String id) {
        return new GetUserByIdInput(id);
    }
}
