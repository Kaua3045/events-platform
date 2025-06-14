package com.kaua.events.platform.infrastructure.organizations.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberInput;

public record UpdateMemberRequest(
        @JsonProperty("authenticated_user_id") String authenticatedUserId,
        @JsonProperty("update_user_id") String updateUserId,
        @JsonProperty("role") String role
) {

    public UpdateMemberInput toInput() {
        return UpdateMemberInput.with(authenticatedUserId, updateUserId, role);
    }
}
