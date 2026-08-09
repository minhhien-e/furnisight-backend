package com.furnisight.admin.infrastructure;

import com.furnisight.admin.account.infrastructure.grpc.AdminUserGrpcClient;
import com.furnisight.admin.catalog.AdminCatalogServiceGrpc;
import com.furnisight.admin.catalog.GetAdminProductsRequest;
import com.furnisight.admin.catalog.ProductPageResponse;
import com.furnisight.admin.catalog.infrastructure.grpc.AdminCatalogGrpcClient;
import com.furnisight.admin.order.AdminOrderServiceGrpc;
import com.furnisight.admin.order.GetAdminOrdersRequest;
import com.furnisight.admin.order.OrderPageResponse;
import com.furnisight.admin.order.infrastructure.grpc.AdminOrderGrpcClient;
import com.furnisight.admin.user.AccountPageResponse;
import com.furnisight.admin.user.AdminUserServiceGrpc;
import com.furnisight.admin.user.GetAccountsRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GrpcClientForwardingTest {

    @Test
    void catalogClientForwardsFiltersWithoutChangingContract() {
        var stub = mock(AdminCatalogServiceGrpc.AdminCatalogServiceBlockingStub.class);
        when(stub.getAdminProducts(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ProductPageResponse.getDefaultInstance());
        AdminCatalogGrpcClient client = new AdminCatalogGrpcClient();
        ReflectionTestUtils.setField(client, "adminCatalogServiceStub", stub);

        client.getProducts(2, 30, "chair", "ACTIVE", "living-room");

        ArgumentCaptor<GetAdminProductsRequest> request = ArgumentCaptor.forClass(GetAdminProductsRequest.class);
        verify(stub).getAdminProducts(request.capture());
        assertThat(request.getValue().getPage()).isEqualTo(2);
        assertThat(request.getValue().getSize()).isEqualTo(30);
        assertThat(request.getValue().getQuery()).isEqualTo("chair");
        assertThat(request.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(request.getValue().getCategory()).isEqualTo("living-room");
    }

    @Test
    void orderClientForwardsPaginationAndFilters() {
        var stub = mock(AdminOrderServiceGrpc.AdminOrderServiceBlockingStub.class);
        when(stub.getAdminOrders(org.mockito.ArgumentMatchers.any()))
                .thenReturn(OrderPageResponse.getDefaultInstance());
        AdminOrderGrpcClient client = new AdminOrderGrpcClient();
        ReflectionTestUtils.setField(client, "adminOrderServiceStub", stub);

        client.getOrders(3, 15, "PAID", "ORD-1");

        ArgumentCaptor<GetAdminOrdersRequest> request = ArgumentCaptor.forClass(GetAdminOrdersRequest.class);
        verify(stub).getAdminOrders(request.capture());
        assertThat(request.getValue().getPage()).isEqualTo(3);
        assertThat(request.getValue().getSize()).isEqualTo(15);
        assertThat(request.getValue().getStatus()).isEqualTo("PAID");
        assertThat(request.getValue().getQuery()).isEqualTo("ORD-1");
    }

    @Test
    void userClientForwardsPaginationAndFilters() {
        var stub = mock(AdminUserServiceGrpc.AdminUserServiceBlockingStub.class);
        when(stub.getAccounts(org.mockito.ArgumentMatchers.any()))
                .thenReturn(AccountPageResponse.getDefaultInstance());
        AdminUserGrpcClient client = new AdminUserGrpcClient();
        ReflectionTestUtils.setField(client, "adminUserServiceStub", stub);

        client.getAccounts(4, 25, "minh", "ACTIVE", "CUSTOMER");

        ArgumentCaptor<GetAccountsRequest> request = ArgumentCaptor.forClass(GetAccountsRequest.class);
        verify(stub).getAccounts(request.capture());
        assertThat(request.getValue().getPage()).isEqualTo(4);
        assertThat(request.getValue().getSize()).isEqualTo(25);
        assertThat(request.getValue().getQuery()).isEqualTo("minh");
        assertThat(request.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(request.getValue().getScope()).isEqualTo(com.furnisight.admin.user.AccountScope.ACCOUNT_SCOPE_CUSTOMER);
    }
}
