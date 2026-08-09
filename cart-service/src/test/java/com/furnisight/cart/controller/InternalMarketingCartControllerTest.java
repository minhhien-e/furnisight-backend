package com.furnisight.cart.controller;

import com.furnisight.cart.model.Cart;
import com.furnisight.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalMarketingCartControllerTest {
    @Test
    void returnsDistinctAbandonedCartUserIds() {
        CartRepository repository = mock(CartRepository.class);
        UUID userId = UUID.randomUUID();
        when(repository.findAbandonedCartIds(any(LocalDateTime.class))).thenReturn(List.of(
                Cart.builder().id(userId).build(), Cart.builder().id(userId).build()));
        var controller = new InternalMarketingCartController(repository);
        ReflectionTestUtils.setField(controller, "abandonedAfterHours", 24L);

        assertThat(controller.getAbandonedCartUserIds()).containsExactly(userId.toString());
        verify(repository).findAbandonedCartIds(any(LocalDateTime.class));
    }
}
