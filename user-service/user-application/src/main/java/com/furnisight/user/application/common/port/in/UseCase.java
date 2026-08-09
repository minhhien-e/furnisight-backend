package com.furnisight.user.application.common.port.in;

public interface UseCase<IInput, IOutput> {
    IOutput execute(IInput input);
}
