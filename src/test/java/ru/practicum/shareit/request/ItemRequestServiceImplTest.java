package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestServiceImplTest {
    @Mock
    ItemRequestRepository itemRequestRepositoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @InjectMocks
    ItemRequestServiceImpl itemRequestServiceMock;
    final long requesterId = 0L;
    final long requestId = 0L;
    ItemRequest itemRequest;
    List<ItemRequest> itemRequests;

    @BeforeEach
    void setUp() {
        User requester = new User();
        requester.setId(requesterId);

        Item item = new Item();
        List<Item> items = List.of(item);

        itemRequest = ItemRequest.builder()
                .id(requestId)
                .requester(requester)
                .created(LocalDateTime.now())
                .description("I need something new")
                .items(items)
                .build();

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(1L);
        itemRequest2.setRequester(requester);
        itemRequest2.setItems(items);

        itemRequests = List.of(itemRequest, itemRequest2);

        Mockito.lenient().when(itemRequestRepositoryMock.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        Mockito.lenient().when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(requester));
    }

    @Test
    void addRequest_userNotFound_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .requesterId(requesterId)
                .description("Long long description")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemRequestServiceMock.addRequest(requestDto));
    }

    @Test
    void addRequest_test() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .requesterId(requesterId)
                .description("Long long description")
                .build();

        ItemRequestResponseDto itemRequestResponseDto =
                itemRequestServiceMock.addRequest(requestDto);

        assertThat(itemRequestResponseDto, allOf(
                hasProperty("id", equalTo(itemRequest.getId())),
                hasProperty("description", equalTo(itemRequest.getDescription())),
                hasProperty("requesterId", equalTo(itemRequest.getRequester().getId()))
        ));
    }

    @Test
    void getRequestsForUser() {
        Mockito.when(itemRequestRepositoryMock.findByRequesterId(requesterId,
                        Sort.by(Sort.Direction.DESC, "created")))
                .thenReturn(itemRequests);

        List<ItemRequestResponseDto> responseDtos
                = itemRequestServiceMock.getRequestsForUser(requesterId);

        assertThat(responseDtos, hasSize(2));
    }

    @Test
    void getAllRequests() {
        Mockito.when(itemRequestRepositoryMock.findAllByRequesterIdNot(
                anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(itemRequests));

        List<ItemRequestResponseDto> responseDtos =
                itemRequestServiceMock.getAllRequests(requesterId, 1, 10);

        assertThat(responseDtos.size(), equalTo(2));
    }

    @Test
    void getRequestById_test() {
        Mockito.when(itemRequestRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));

        ItemRequestResponseDto itemRequestResponseDto = itemRequestServiceMock.getRequestById(requesterId, requestId);

        assertThat(itemRequestResponseDto, allOf(
                hasProperty("id", equalTo(itemRequest.getId())),
                hasProperty("description", equalTo(itemRequest.getDescription())),
                hasProperty("requesterId", equalTo(itemRequest.getRequester().getId()))
        ));

        assertThat(itemRequestResponseDto.getItems(), hasSize(1));
    }

    @Test
    void getRequestById_itemRequestNotFound_test() {
        Mockito.when(itemRequestRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestServiceMock.getRequestById(requesterId, requestId));
    }
}