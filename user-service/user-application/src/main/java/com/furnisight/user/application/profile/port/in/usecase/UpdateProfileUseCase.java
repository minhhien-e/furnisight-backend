package com.furnisight.user.application.profile.port.in.usecase;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.application.profile.dto.UpdateProfileCommand;
import com.furnisight.user.domain.entities.profile.UserProfile;

public interface UpdateProfileUseCase extends UseCase<UpdateProfileCommand, UserProfile> {

}
