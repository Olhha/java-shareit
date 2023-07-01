package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    BookingRepository bookingRepository;

    Booking bookingPastApproved;
    Booking bookingFutureApproved;
    Booking bookingFutureWaiting;
    Booking bookingPastRejected;
    Booking bookingCurrent;
    User booker;
    User owner;
    Item item;
    final Pageable page = PageRequest.of(0, 10);
    final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    @BeforeEach
    void setUp() {
        booker = createUser("Ivan Booker", "booker@user.com");
        owner = createUser("Ivan Owner", "owner@user.com");

        item = createItem();

        bookingPastApproved = createBooking(Status.APPROVED, now.minusDays(5), now.minusDays(4));
        bookingFutureApproved = createBooking(Status.APPROVED, now.plusDays(5), now.plusDays(6));
        bookingFutureWaiting = createBooking(Status.WAITING, now.plusDays(5), now.plusDays(6));
        bookingPastRejected = createBooking(Status.REJECTED, now.minusDays(5), now.minusDays(4));
        bookingCurrent = createBooking(Status.APPROVED, now.minusDays(3), now.plusDays(3));
    }


    @AfterEach
    void tearDown() {
        entityManager.getEntityManager().createQuery("delete from Booking").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from Item").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from User").executeUpdate();
    }

    @Test
    void findByBookerIdOrderByStartDesc() {
        List<Booking> bookings = bookingRepository
                .findByBookerIdOrderByStartDesc(booker.getId(), page).getContent();
        assertThat(bookings, hasSize(5));
        assertThat(bookings.get(0), equalTo(bookingFutureApproved));
        assertThat(bookings.get(1), equalTo(bookingFutureWaiting));
        assertThat(bookings.get(2), equalTo(bookingCurrent));
        assertThat(bookings.get(3), equalTo(bookingPastApproved));
        assertThat(bookings.get(4), equalTo(bookingPastRejected));
    }

    @Test
    void findByItemOwnerIdOrderByStartDesc() {
        List<Booking> bookings = bookingRepository
                .findByItemOwnerIdOrderByStartDesc(owner.getId(), page).getContent();
        assertThat(bookings, hasSize(5));
        assertThat(bookings.get(0), equalTo(bookingFutureApproved));
        assertThat(bookings.get(1), equalTo(bookingFutureWaiting));
        assertThat(bookings.get(2), equalTo(bookingCurrent));
        assertThat(bookings.get(3), equalTo(bookingPastApproved));
        assertThat(bookings.get(4), equalTo(bookingPastRejected));
    }

    @Test
    void findByBookerIdAndStatusOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                booker.getId(), Status.WAITING, page).getContent();
        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0).getStatus(), equalTo(Status.WAITING));
    }

    @Test
    void findByItemOwnerIdAndStatusOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                owner.getId(), Status.REJECTED, page).getContent();
        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0).getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    void findByBookerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                booker.getId(), now, now, page).getContent();
        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0), equalTo(bookingCurrent));
    }

    @Test
    void findByItemOwnerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
                owner.getId(), now, now, page).getContent();
        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0), equalTo(bookingCurrent));
    }

    @Test
    void findByBookerIdAndStartIsAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(
                booker.getId(), now, page).getContent();
        assertThat(bookings, hasSize(2));
        assertThat(bookings.get(0), equalTo(bookingFutureApproved));
        assertThat(bookings.get(1), equalTo(bookingFutureWaiting));
    }

    @Test
    void findByItemOwnerIdAndStartIsAfterOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                owner.getId(), now, page).getContent();
        assertThat(bookings, hasSize(2));
        assertThat(bookings.get(0), equalTo(bookingFutureApproved));
        assertThat(bookings.get(1), equalTo(bookingFutureWaiting));
    }

    @Test
    void findByBookerIdAndEndBeforeOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                booker.getId(), now, page).getContent();
        assertThat(bookings, hasSize(2));
        assertThat(bookings.get(0), equalTo(bookingPastApproved));
        assertThat(bookings.get(1), equalTo(bookingPastRejected));
    }

    @Test
    void findByItemOwnerIdAndEndBeforeOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                owner.getId(), now, page).getContent();
        assertThat(bookings, hasSize(2));
        assertThat(bookings.get(0), equalTo(bookingPastApproved));
        assertThat(bookings.get(1), equalTo(bookingPastRejected));
    }

    @Test
    void getFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc() {
        Booking lastBooking = bookingRepository.getFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                item.getId(), Status.APPROVED, now);
        assertThat(lastBooking, equalTo(bookingCurrent));
    }

    @Test
    void getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc() {
        Booking nextBooking = bookingRepository.getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                item.getId(), Status.APPROVED, now);
        assertThat(nextBooking, equalTo(bookingFutureApproved));
    }

    @Test
    void findByBookerIdAndItemIdAndEndBefore() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                booker.getId(), item.getId(), now);
        assertThat(bookings, hasSize(2));
        assertThat(bookings.get(0), equalTo(bookingPastApproved));
        assertThat(bookings.get(1), equalTo(bookingPastRejected));
    }

    @Test
    void findByStatusAndItemInOrderByStartDesc() {
        List<Booking> bookings = bookingRepository.findByStatusAndItemInOrderByStartDesc(
                Status.APPROVED, List.of(item));
        assertThat(bookings, hasSize(3));
        assertThat(bookings.get(0), equalTo(bookingFutureApproved));
        assertThat(bookings.get(1), equalTo(bookingCurrent));
        assertThat(bookings.get(2), equalTo(bookingPastApproved));
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO users (email, name) VALUES (?,?)")
                .setParameter(1, email)
                .setParameter(2, name)
                .executeUpdate();

        long userId = entityManager.getEntityManager().createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getSingleResult().getId();
        user.setId(userId);

        return user;
    }

    private Booking createBooking(Status status, LocalDateTime start, LocalDateTime end) {
        Booking newBooking = Booking.builder()
                .status(status)
                .start(start)
                .end(end)
                .booker(booker)
                .item(item)
                .build();

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO bookings (item_id, booker_id, status, start_date, end_date) " +
                                "VALUES (?,?,?,?,?)")
                .setParameter(1, item.getId())
                .setParameter(2, booker.getId())
                .setParameter(3, status.toString())
                .setParameter(4, start)
                .setParameter(5, end)
                .executeUpdate();

        BigInteger id = (BigInteger) entityManager.getEntityManager()
                .createNativeQuery("SELECT max(id) FROM bookings").getSingleResult();
        newBooking.setId(id.longValue());
        return newBooking;
    }

    private Item createItem() {
        Item newItem = Item.builder()
                .name("Book")
                .description("Interesting book")
                .available(true)
                .owner(owner)
                .build();

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO items (owner_id, name, description, is_available) " +
                                "VALUES (?,?,?,?)")
                .setParameter(1, owner.getId())
                .setParameter(2, newItem.getName())
                .setParameter(3, newItem.getDescription())
                .setParameter(4, newItem.getAvailable())
                .executeUpdate();

        BigInteger id = (BigInteger) entityManager.getEntityManager()
                .createNativeQuery("SELECT max(id) FROM items").getSingleResult();
        newItem.setId(id.longValue());
        return newItem;
    }
}