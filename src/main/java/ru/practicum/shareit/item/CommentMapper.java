package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static CommentResponseDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
    }

    public static Comment toComment(CommentRequestDto commentRequestDto) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .build();
    }

    public static List<CommentResponseDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
