package com.app.foodlane.restaurant.utils;

import com.app.foodlane.cart.dto.request.CustomizationSelectionDto;
import com.app.foodlane.restaurant.entity.CustomizationGroup;
import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.restaurant.entity.MenuItem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomizationValidator {

    public void validate(MenuItem menuItem, List<CustomizationSelectionDto> selections) {
        List<CustomizationSelectionDto> safeSelections =
                selections == null ? List.of() : selections;

        // 1. Collect the groups and options available for this menu item.
        Set<CustomizationGroup> availableGroups = menuItem.getCustomizationGroups();

        // Map for quick option lookup: optionId -> CustomizationOption.
        Map<Long, CustomizationOption> availableOptionsMap = availableGroups.stream()
                .flatMap(group -> group.getCustomizationOptions().stream())
                .collect(Collectors.toMap(CustomizationOption::getCustomizationOptionId, opt -> opt));

        // Map selections by group.
        Map<CustomizationGroup, List<CustomizationSelectionDto>> selectionsByGroup = new HashMap<>();

        for (CustomizationSelectionDto selection : safeSelections) {
            // check if incoming selectedCustomization is existed in availableOptionsMap
            CustomizationOption option = availableOptionsMap.get(selection.customizationOptionId());

            // Check 1: Ensure the option belongs to this menu item.
            if (option == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Invalid customization option ID: " + selection.customizationOptionId() + " for this menu item");
            }

            // Check 2: Ensure sufficient option inventory is available.
            if (option.getInventoryQuantity() < selection.selected()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Customization option '" + option.getName() + "' is out of stock");
            }
            // selected group with its selected customization
            selectionsByGroup.computeIfAbsent(option.getCustomizationGroup(), k -> new ArrayList<>()).add(selection);
        }

        // 2. Validate the rules for each group individually.
        for (CustomizationGroup group : availableGroups) {
            List<CustomizationSelectionDto> groupSelections = selectionsByGroup.getOrDefault(group, List.of());

            if (Boolean.TRUE.equals(group.getRequired())) {
                // --- Required Group Logic (Single Preference) ---
                // Only one option may be selected.
                if (groupSelections.size() != 1) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Group '" + group.getName() + "' is required. Exactly 1 option must be selected.");
                }

                // The quantity is always 1 because this is a preference, not an add-on quantity.
                CustomizationSelectionDto selectedOption = groupSelections.get(0);
                if (selectedOption.selected() != 1) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Quantity for required customization '" + group.getName() + "' must be 1.");
                }

            } else {
                // --- Optional Group Logic (Extra Add-ons) ---
                // The number of selected options must not exceed maxSelect.
                if (groupSelections.size() > group.getMaxSelect()) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "You can select up to " + group.getMaxSelect() + " options for '" + group.getName() + "'");
                }

                // The quantity of each selected option must be between 1 and 6, per the database constraint.
                for (CustomizationSelectionDto selection : groupSelections) {
                    if (selection.selected() < 1 || selection.selected() > 6) {
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Option selected must be between 1 and 6 for optional group '" + group.getName() + "'");
                    }
                }
            }
        }
    }
}
