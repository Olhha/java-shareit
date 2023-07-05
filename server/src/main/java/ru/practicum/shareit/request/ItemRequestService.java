package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto addRequest(ItemRequestDto requestDto);

    List<ItemRequestResponseDto> getRequestsForUser(Long userId);

    List<ItemRequestResponseDto> getAllRequests(
            Long userId, Integer from, Integer size);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}
