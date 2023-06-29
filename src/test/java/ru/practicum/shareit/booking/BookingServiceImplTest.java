package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplTest {
    @Mock
    ItemRepository itemRepositoryMock;
    @Mock
    BookingRepository bookingRepositoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @InjectMocks
    BookingServiceImpl bookingServiceMock;
    final long ownerUserId = 0L;
    final long bookerUserId = 1L;
    final long otherUserId = 2L;
    final long itemId = 0L;
    final long bookingId = 0L;
    final long otherBookingId = 1L;
    User owner;
    User booker;
    User otherUser;
    Item item;
    Booking booking;
    Page<Booking> bookingsPage;
    BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(ownerUserId)
                .email("user@user.com")
                .name("Ivan Ivanov")
                .build();

        booker = User.builder()
                .id(bookerUserId)
                .email("booker@user.com")
                .name("Ivan Booker")
                .build();

        otherUser = new User();

        item = Item.builder()
                .id(itemId)
                .name("Book")
                .description("Interesting book")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .status(Status.WAITING)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(10))
                .item(item)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .status(Status.WAITING)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(10))
                .item(item)
                .build();

        Booking booking3 = Booking.builder()
                .id(3L)
                .status(Status.WAITING)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(10))
                .item(item)
                .build();

        List<Booking> bookings = List.of(booking, booking2, booking3);
        bookingsPage = new PageImpl<>(bookings);

        bookingRequestDto =
                BookingRequestDto.builder()
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .bookerId(bookerUserId)
                        .itemId(itemId)
                        .build();

        Mockito.lenient().when(bookingRepositoryMock.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.lenient().when(bookingRepositoryMock.findById(otherBookingId)).thenReturn(Optional.empty());
    }

    @Test
    void addBooking_EndEqualsStart_test() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequestDto =
                BookingRequestDto.builder()
                        .start(start)
                        .end(start)
                        .bookerId(ownerUserId)
                        .itemId(itemId)
                        .build();

        assertThrows(ValidationException.class,
                () -> bookingServiceMock.addBooking(bookingRequestDto));
    }

    @Test
    void addBooking_EndBeforeStart_test() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingRequestDto bookingRequestDto =
                BookingRequestDto.builder()
                        .start(start)
                        .end(end)
                        .bookerId(ownerUserId)
                        .itemId(itemId)
                        .build();

        assertThrows(ValidationException.class,
                () -> bookingServiceMock.addBooking(bookingRequestDto));
    }

    @Test
    void addBooking_itemNotFound_test() {
        BookingRequestDto bookingRequestDto =
                BookingRequestDto.builder()
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .bookerId(ownerUserId)
                        .itemId(itemId)
                        .build();

        Mockito.when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingServiceMock.addBooking(bookingRequestDto));
    }

    @Test
    void addBooking_itemUnavailable_test() {
        BookingRequestDto bookingRequestDto =
                BookingRequestDto.builder()
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .bookerId(ownerUserId)
                        .itemId(itemId)
                        .build();

        item.setAvailable(false);

        Mockito.when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingServiceMock.addBooking(bookingRequestDto));
    }

    @Test
    void addBooking_bookerIsOwner_test() {
        BookingRequestDto bookingRequestDto =
                BookingRequestDto.builder()
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .bookerId(ownerUserId)
                        .itemId(itemId)
                        .build();

        Mockito.when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingServiceMock.addBooking(bookingRequestDto));
    }

    @Test
    void addBooking_test() {


        Mockito.when(itemRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepositoryMock.save(any(Booking.class)))
                .thenReturn(booking);
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        BookingResponseDto bookingResponseDto =
                bookingServiceMock.addBooking(bookingRequestDto);

        assertThat(bookingResponseDto, allOf(
                hasProperty("id", equalTo(booking.getId())),
                hasProperty("start", equalTo(booking.getStart())),
                hasProperty("end", equalTo(booking.getEnd())),
                hasProperty("status", equalTo(booking.getStatus().toString()))));
        assertThat(booking.getItem(), allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName()))
        ));
        assertThat(booking.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void approveBooking_bookingNotFound_test() {
        assertThrows(NotFoundException.class,
                () -> bookingServiceMock.approveBooking(
                        ownerUserId, otherBookingId, true));
    }

    @Test
    void approveBooking_byNotOwner_test() {
        assertThrows(NotFoundException.class,
                () -> bookingServiceMock.approveBooking(
                        bookerUserId, bookingId, true));
    }

    @Test
    void approveBooking_alreadyApproved_test() {
        booking.setStatus(Status.APPROVED);
        assertThrows(ValidationException.class,
                () -> bookingServiceMock.approveBooking(
                        ownerUserId, bookingId, true));
    }

    @Test
    void approveBooking_setApproved_test() {
        BookingResponseDto bookingResponseDto =
                bookingServiceMock.approveBooking(
                        ownerUserId, bookingId, true);

        assertThat(bookingResponseDto, allOf(
                hasProperty("id", equalTo(booking.getId())),
                hasProperty("start", equalTo(booking.getStart())),
                hasProperty("end", equalTo(booking.getEnd())),
                hasProperty("status", equalTo(booking.getStatus().toString()))
        ));
    }

    @Test
    void approveBooking_setRejected_test() {
        BookingResponseDto bookingResponseDto =
                bookingServiceMock.approveBooking(
                        ownerUserId, bookingId, false);

        assertThat(bookingResponseDto, allOf(
                hasProperty("id", equalTo(booking.getId())),
                hasProperty("start", equalTo(booking.getStart())),
                hasProperty("end", equalTo(booking.getEnd())),
                hasProperty("status", equalTo(Status.REJECTED.toString()))
        ));
    }

    @Test
    void getBookingByIdByOwnerOrBooker_byOwner_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        BookingResponseDto bookingResponseDto =
                bookingServiceMock.getBookingByIdByOwnerOrBooker(ownerUserId, bookingId);

        assertThat(bookingResponseDto.getId(), equalTo(booking.getId()));
    }

    @Test
    void getBookingByIdByOwnerOrBooker_byBooker_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        BookingResponseDto bookingResponseDto =
                bookingServiceMock.getBookingByIdByOwnerOrBooker(bookerUserId, bookingId);

        assertThat(bookingResponseDto.getId(), equalTo(booking.getId()));
    }

    @Test
    void getBookingByIdByOwnerOrBooker_byOtherUser_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(otherUser));

        assertThrows(NotFoundException.class,
                () -> bookingServiceMock.getBookingByIdByOwnerOrBooker(otherUserId, bookingId));
    }

    @Test
    void getAllBookingsForOwner_ALL_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdOrderByStartDesc(
                anyLong(), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "ALL", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_CURRENT_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "CURRENT", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_PAST_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "PAST", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_FUTURE_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "FUTURE", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_WAITING_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "WAITING", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_REJECTED_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(bookingRepositoryMock.findByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "REJECTED", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForOwner_unknownState_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> bookingServiceMock
                .getAllBookingsForOwner(
                        ownerUserId, "UNKNOWN", 1, 1));
    }

    @Test
    void getAllBookingsForUser() {
    }

    @Test
    void getAllBookingsForUser_ALL_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdOrderByStartDesc(
                anyLong(), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "ALL", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_CURRENT_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "CURRENT", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_PAST_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdAndEndBeforeOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "PAST", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdAndEndBeforeOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_FUTURE_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdAndStartIsAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "FUTURE", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdAndStartIsAfterOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_WAITING_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "WAITING", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_REJECTED_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));
        Mockito.when(bookingRepositoryMock.findByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class))).thenReturn((bookingsPage));

        List<BookingResponseDto> bookingsDto = bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "REJECTED", 1, 1);
        assertThat(bookingsDto, hasSize(3));

        verify(bookingRepositoryMock).findByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsForUser_unknownState_test() {
        Mockito.when(userRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingServiceMock
                .getAllBookingsForUser(
                        bookerUserId, "UNKNOWN", 1, 1));
    }
}