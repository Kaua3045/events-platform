package com.kaua.events.platform.infrastructure.organizations.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.application.usecases.organizations.update.member.UpdateMemberOutput;

public record UpdateMemberResponse(
        @JsonProperty("updated_user_id") String updatedUserId
) {

    public static UpdateMemberResponse from(final UpdateMemberOutput aOutput) {
        return new UpdateMemberResponse(aOutput.userId());
    }
}
