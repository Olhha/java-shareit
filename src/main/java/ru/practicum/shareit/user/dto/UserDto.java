package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.exception.MarkerValidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
@Builder(toBuilder = true)
public class UserDto {
    Long id;
    @NotBlank(groups = MarkerValidation.OnCreate.class)
    String name;
    @Email(groups = {MarkerValidation.OnCreate.class, MarkerValidation.OnUpdate.class})
    @NotBlank(groups = MarkerValidation.OnCreate.class)
    String email;
}
