package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@Builder
public class ItemDto {
    private int id;
    @NotBlank
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final Boolean available;
    private Integer requestID;
}
