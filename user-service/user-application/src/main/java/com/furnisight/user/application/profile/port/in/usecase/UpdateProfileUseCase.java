package com.furnisight.user.application.profile.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.profile.dto.ProfileResult;
import com.furnisight.user.application.profile.dto.UpdateProfileCommand;

public interface UpdateProfileUseCase extends UseCase<UpdateProfileCommand, ProfileResult> {

}
