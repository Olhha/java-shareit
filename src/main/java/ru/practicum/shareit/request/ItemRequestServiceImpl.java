package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemRequestResponseDto addRequest(ItemRequestDto requestDto) {
        User user = getUser(requestDto.getRequesterId());

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());

        return ItemRequestMapper.toItemRequestResponseDto(
                itemRequestRepository.save(itemRequest));
    }


    @Override
    public List<ItemRequestResponseDto> getRequestsForUser(Long userId) {
        getUser(userId);
        return ItemRequestMapper.toItemRequestDtoList(
                itemRequestRepository.findByRequesterId(userId,
                        Sort.by(Sort.Direction.DESC, "created")));
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(
            Long userId, Integer from, Integer size) {
        getUser(userId);

        List<ItemRequest> itemRequestsPaged = itemRequestRepository.findAllByRequesterIdNot(
                        userId, PageRequest.of(from / size, size,
                                Sort.by(Sort.Direction.DESC, "created")))
                .getContent();

        return ItemRequestMapper.toItemRequestDtoList(itemRequestsPaged);

    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        getUser(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "There's no itemRequest with id = " + requestId));

        return ItemRequestMapper.toItemRequestResponseDto(itemRequest);
    }

    private User getUser(Long userID) {
        return userRepository.findById(userID).orElseThrow(
                () -> new NotFoundException(String.format(
                        "User with id = %d doesn't exist", userID)));
    }
}
