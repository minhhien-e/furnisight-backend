package com.furnisight.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private UUID id; // user_id (each user has 1 cart)

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Double total;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
