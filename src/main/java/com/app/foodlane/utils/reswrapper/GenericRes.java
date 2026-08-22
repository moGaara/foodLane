package com.app.foodlane.utils.reswrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonPropertyOrder({ "header", "body" })
public class GenericRes <T>{
    private ResHeader header = new ResHeader();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T body;
}
