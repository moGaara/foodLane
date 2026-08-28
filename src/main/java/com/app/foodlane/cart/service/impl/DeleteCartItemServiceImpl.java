package com.app.foodlane.cart.service.impl;

import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.repository.CartItemRepository;
import com.app.foodlane.cart.service.IDeleteCartItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCartItemServiceImpl implements IDeleteCartItemService {
    private final CartItemRepository repo;
    @Override
    @Transactional
    public void deleteItem(Long customerId, Long itemId) {
        int deletedRows = repo.deleteByCartItemIdAndCustomerId(itemId, customerId, CartStatus.ACTIVE.name());
        if (deletedRows == 0) {
            throw new IllegalArgumentException("Cart item not found or unauthorized access.");
        }
    }
}
