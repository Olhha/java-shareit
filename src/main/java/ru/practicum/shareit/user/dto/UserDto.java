package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@Builder
public class  UserDto {
    int id;
    private String name;
    @Email
    private String email;
}
