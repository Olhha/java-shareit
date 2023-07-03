package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class CommentResponseDto {
    long id;
    String text;
    String authorName;
    LocalDateTime created;
}
