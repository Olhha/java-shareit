package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.UpdateForbiddenException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithLastAndNextBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplUnitTest {
    @Mock
    ItemRepository itemRepositoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @Mock
    BookingRepository bookingRepositoryMock;
    @Mock
    CommentRepository commentRepositoryMock;
    @Mock
    ItemRequestRepository itemRequestRepositoryMock;

    @InjectMocks
    ItemServiceImpl itemServiceMock;
    Item item;
    User user;
    Comment comment;
    long userId;
    long itemId;
    ItemDto itemDto;
    ItemDto itemUpdate;
    CommentRequestDto commentDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemId = 0L;
        long itemIdUpdate = 1L;

        itemDto = ItemDto.builder().name("Book").description("Interesting book").available(true).build();
        user = User.builder().id(userId).email("user@user.com").name("Ivan Ivanov").build();
        item = Item.builder().id(itemId).name("Book").description("Interesting book").available(true).owner(user).build();
        Item item1 = Item.builder().id(itemId + 1).name("Book1").description("Interesting book1").available(true).owner(user).build();
        Item item2 = Item.builder().id(itemId + 2).name("Book2").description("Interesting book2").available(true).owner(user).build();

        List<Item> items = List.of(item, item1, item2);
        itemUpdate = ItemDto.builder().id(itemIdUpdate).name("NewName").description("NewDescription").available(false).build();

        LocalDateTime createdTime = LocalDateTime.now();
        commentDto = new CommentRequestDto();
        commentDto.setText("I'm happy.");


        comment = Comment.builder()
                .id(0L)
                .item(item)
                .author(user)
                .text(commentDto.getText())
                .created(createdTime)
                .build();
        Comment comment2 = new Comment();

        Mockito.lenient().when(itemRepositoryMock.save(Mockito.any())).thenReturn(item);
        Mockito.lenient().when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.lenient().when(itemRepositoryMock.findById(anyLong())).thenReturn(Optional.of(item));
        Mockito.lenient()
                .when(itemRepositoryMock.findByOwnerId(anyLong(), Mockito.any()))
                .thenReturn(new PageImpl<>(items));
        Mockito.lenient().when(
                        bookingRepositoryMock.findByStatusAndItemInOrderByStartDesc(Mockito.any(), Mockito.anyList()))
                .thenReturn(List.of());
        Mockito.lenient().when(commentRepositoryMock.findByItemIn(Mockito.anyList(), Mockito.any()))
                .thenReturn(List.of());
        Mockito.lenient().when(itemRepositoryMock.searchItemsByText(Mockito.anyString(), Mockito.any()))
                .thenReturn(new PageImpl<>(items));
        Mockito.lenient().when(commentRepositoryMock.save(Mockito.any()))
                .thenReturn(comment);
    }

    @Test
    void addItem_DtoToModel_test() {
        itemServiceMock.addItem(itemDto, userId);

        verify(userRepositoryMock).findById(userId);
        verify(itemRepositoryMock).save(item);
    }

    @Test
    void addItem_RequestIdNotNull_test() {
        Mockito.when(itemRequestRepositoryMock.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new ItemRequest()));
        ItemDto itemDtoWithRequest = itemDto.toBuilder().requestId(1L).build();

        itemServiceMock.addItem(itemDtoWithRequest, userId);
    }

    @Test
    void updateItem_AllFieldsUpdate_test() {
        ItemDto updatedItem = itemServiceMock.updateItem(itemUpdate, userId);

        assertThat(updatedItem, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemUpdate.getName())),
                hasProperty("description", equalTo(itemUpdate.getDescription())),
                hasProperty("available", equalTo(itemUpdate.getAvailable()))));
    }

    @Test
    void updateItem_byNotOwner_test() {
        Long anotherUserId = 2L;

        assertThrows(UpdateForbiddenException.class,
                () -> itemServiceMock.updateItem(itemUpdate, anotherUserId));
    }

    @Test
    void updateItem_nullUserId_test() {
        assertThrows(ValidationException.class,
                () -> itemServiceMock.updateItem(itemUpdate, null));
    }

    @Test
    void getItemByID_test() {
        ItemWithLastAndNextBookingsAndCommentsDto itemDtoReceived
                = itemServiceMock.getItemByID(itemId, userId);

        assertThat(itemDtoReceived, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto.getName())),
                hasProperty("description", equalTo(itemDto.getDescription())),
                hasProperty("available", equalTo(itemDto.getAvailable()))));
    }

    @Test
    void getItemsForUser_test() {
        List<ItemWithLastAndNextBookingsAndCommentsDto> itemsDto
                = itemServiceMock.getItemsForUser(userId, 1, 10);

        assertThat(itemsDto, hasSize(3));
    }

    @Test
    void searchItemsByText_test() {
        List<ItemDto> itemsDto
                = itemServiceMock.searchItemsByText("Book", 1, 10);
        assertThat(itemsDto, hasSize(3));
    }

    @Test
    void addCommentToItem_test() {
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .booker(user)
                .status(Status.APPROVED)
                .build();
        Mockito.when(bookingRepositoryMock.findByBookerIdAndItemIdAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(booking));

        CommentResponseDto commentAddedDto = itemServiceMock.addCommentToItem(itemId, userId, commentDto);

        verify(bookingRepositoryMock).findByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), Mockito.any());
        assertThat(commentAddedDto, allOf(
                hasProperty("id", equalTo(comment.getId())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("created", equalTo(comment.getCreated())),
                hasProperty("authorName", equalTo(comment.getAuthor().getName()))
        ));
    }

    @Test
    void addCommentToItem_nullComment_test() {
        Mockito.when(commentRepositoryMock.save(Mockito.any()))
                .thenReturn(null);
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .booker(user)
                .status(Status.APPROVED)
                .build();
        Mockito.when(bookingRepositoryMock.findByBookerIdAndItemIdAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(booking));

        CommentResponseDto commentAddedDto = itemServiceMock.addCommentToItem(itemId, userId, commentDto);

        assertThat(commentAddedDto, equalTo(null));

    }

    @Test
    void addEmptyCommentToItem_test() {
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .booker(user)
                .status(Status.APPROVED)
                .build();
        Mockito.when(bookingRepositoryMock.findByBookerIdAndItemIdAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of(booking));

        CommentResponseDto commentAddedDto = itemServiceMock.addCommentToItem(itemId, userId, commentDto);

        verify(bookingRepositoryMock).findByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), Mockito.any());
        assertThat(commentAddedDto, allOf(
                hasProperty("id", equalTo(comment.getId())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("created", equalTo(comment.getCreated())),
                hasProperty("authorName", equalTo(comment.getAuthor().getName()))
        ));
    }

    @Test
    void addCommentToItem_authorCantComment_test() {
        Mockito.when(bookingRepositoryMock.findByBookerIdAndItemIdAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(List.of());

        assertThrows(ValidationException.class,
                () -> itemServiceMock.addCommentToItem(itemId, userId, commentDto));
    }
}