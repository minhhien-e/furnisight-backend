package com.furnisight.user.application.profile.port.in.usecase;

import java.util.UUID;

import com.furnisight.user.application.common.port.in.UseCase;
import com.furnisight.user.domain.entities.profile.UserProfile;

public interface GetProfileUseCase extends UseCase<UUID, UserProfile> {
}