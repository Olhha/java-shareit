package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import javax.validation.constraints.Email;

@Data
@Value
@Builder
public class UserDto {
    Integer id;
    String name;
    @Email
    String email;
}
