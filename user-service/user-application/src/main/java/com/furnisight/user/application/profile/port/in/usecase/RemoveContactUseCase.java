package com.furnisight.user.application.profile.port.in.usecase;

import com.furnisight.user.application.profile.dto.RemoveContactCommand;

public interface RemoveContactUseCase {
    Void execute(RemoveContactCommand command);
}
