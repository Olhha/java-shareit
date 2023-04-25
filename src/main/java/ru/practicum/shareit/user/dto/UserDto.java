package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.exception.MarkerValidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
public class UserDto {
    Integer id;
    @NotBlank(groups = MarkerValidation.OnCreate.class)
    String name;
    @Email
    @NotNull(groups = MarkerValidation.OnCreate.class)
    @NotBlank(groups = MarkerValidation.OnCreate.class)
    String email;
}
