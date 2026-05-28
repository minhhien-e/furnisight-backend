package com.furnisight.user.application.address.port.in.command;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetDefaultAddressCommand {
    private UUID accountId;
    private UUID addressId;
}
