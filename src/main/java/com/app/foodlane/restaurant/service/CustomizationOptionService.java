package com.app.foodlane.restaurant.service;

import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.restaurant.repository.CustomizationOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CustomizationOptionService {
    private final CustomizationOptionRepository customizationOptionRepository;

    public CustomizationOption getById(Long id){
        return customizationOptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND , "customizationOption not found"
                ));
    }
}
