package com.app.foodlane.restaurant.service;

import com.app.foodlane.restaurant.entity.MenuItem;
import com.app.foodlane.restaurant.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;

    public MenuItem getById(Long id){
        return menuItemRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND ,"MenuItem not found with ID: " + id)
                );
    }
    public MenuItem getByIdWithCustomizations(Long id) {
        return menuItemRepository.findByIdWithCustomizations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MenuItem not found with ID: " + id));
    }
}
