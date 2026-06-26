package com.furniro.MessageService.service.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.furniro.MessageService.dto.res.CustomerProfile;
import com.furnisight.admin.user.AccountDetailResponse;
import com.furnisight.admin.user.AccountDto;
import com.furnisight.admin.user.AccountPageResponse;
import com.furnisight.admin.user.AccountScope;
import com.furnisight.admin.user.AdminUserServiceGrpc;
import com.furnisight.admin.user.GetAccountByIdRequest;
import com.furnisight.admin.user.GetAccountsRequest;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class UserProfileGrpcClient {
    private static final int CUSTOMER_OFFSET = 100_000;
    private static final int CUSTOMER_SPAN = 1_400_000_000;
    private static final int PAGE_SIZE = 500;

    @GrpcClient("user-service")
    private AdminUserServiceGrpc.AdminUserServiceBlockingStub userStub;

    public Map<Integer, CustomerProfile> resolveBuyerProfiles(Collection<Integer> buyerIds) {
        Set<Integer> requestedBuyerIds = new LinkedHashSet<>();
        for (Integer buyerId : buyerIds) {
            if (buyerId != null && buyerId > 0) {
                requestedBuyerIds.add(buyerId);
            }
        }
        if (requestedBuyerIds.isEmpty()) {
            return Map.of();
        }

        return findCustomerProfiles(requestedBuyerIds);
    }

    public CustomerProfile resolveBuyerProfile(Integer buyerId) {
        return resolveBuyerProfiles(buyerId == null ? Set.of() : Set.of(buyerId)).get(buyerId);
    }

    private Map<Integer, CustomerProfile> findCustomerProfiles(Set<Integer> buyerIds) {
        Map<Integer, CustomerProfile> matches = new HashMap<>();

        for (AccountScope scope : new AccountScope[] {
                AccountScope.ACCOUNT_SCOPE_UNSPECIFIED,
                AccountScope.ACCOUNT_SCOPE_CUSTOMER,
                AccountScope.ACCOUNT_SCOPE_ADMIN
        }) {
            scanAccountsByScope(scope, buyerIds, matches);
            if (matches.size() == buyerIds.size()) {
                break;
            }
        }

        return matches;
    }

    private void scanAccountsByScope(
            AccountScope scope,
            Set<Integer> buyerIds,
            Map<Integer, CustomerProfile> matches
    ) {
        int page = 1;
        int totalPages = 1;

        do {
            AccountPageResponse response = userStub.getAccounts(GetAccountsRequest.newBuilder()
                    .setPage(page)
                    .setSize(PAGE_SIZE)
                    .setScope(scope)
                    .build());

            totalPages = Math.max(response.getTotalPages(), 1);
            for (AccountDto account : response.getAccountsList()) {
                AccountDetailResponse detail = userStub.getAccountById(
                        GetAccountByIdRequest.newBuilder()
                                .setId(account.getId())
                                .build());
                CustomerProfile profile = toCustomerProfile(account, detail);
                for (Integer numericId : profileNumericIds(account, detail)) {
                    if (buyerIds.contains(numericId)) {
                        matches.putIfAbsent(numericId, CustomerProfile.builder()
                                .buyerId(numericId)
                                .accountId(profile.getAccountId())
                                .buyerName(profile.getBuyerName())
                                .buyerEmail(profile.getBuyerEmail())
                                .buyerAvatarUrl(profile.getBuyerAvatarUrl())
                                .build());
                    }
                }
            }
            page++;
        } while (page <= totalPages && matches.size() < buyerIds.size());
    }

    private CustomerProfile toCustomerProfile(AccountDto account, AccountDetailResponse detail) {
        return CustomerProfile.builder()
                .accountId(account.getId())
                .buyerName(firstNonBlank(detail.getName(), account.getName(), detail.getUsername(), account.getUsername()))
                .buyerEmail(firstNonBlank(detail.getEmail(), account.getEmail()))
                .buyerAvatarUrl(blankToNull(detail.getAvatarUrl()))
                .build();
    }

    private Set<Integer> profileNumericIds(AccountDto account, AccountDetailResponse detail) {
        Set<Integer> numericIds = new LinkedHashSet<>();
        addNumericId(numericIds, account.getId());
        addNumericId(numericIds, detail.getPhone());
        addNumericId(numericIds, account.getEmail());
        addNumericId(numericIds, account.getName());
        return numericIds;
    }

    private void addNumericId(Set<Integer> numericIds, String value) {
        Integer direct = parseNumericId(value);
        if (direct != null) {
            numericIds.add(direct);
            return;
        }
        Integer hashed = hashStableNumericId(value, CUSTOMER_OFFSET, CUSTOMER_SPAN);
        if (hashed != null) {
            numericIds.add(hashed);
        }
    }

    private Integer parseNumericId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed > 0 && parsed <= Integer.MAX_VALUE) {
                return (int) parsed;
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private Integer hashStableNumericId(String value, int offset, int span) {
        String text = value == null ? "" : value.trim();
        if (text.isBlank()) {
            return null;
        }

        int hash = 0;
        for (int i = 0; i < text.length(); i++) {
            hash = ((hash << 5) - hash) + text.charAt(i);
        }

        long positiveHash = Math.abs((long) hash);
        return (int) (positiveHash % span) + offset;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
