package com.furnisight.user.application.common.port.in;

import java.util.Map;

public interface JWKProvider {
    Map<String,Object> getJWK();
}
