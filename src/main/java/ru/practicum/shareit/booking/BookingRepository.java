package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end);

    Booking getFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
            Long idItem, Status status, LocalDateTime now);

    Booking getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long idItem, Status status, LocalDateTime now);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(
            Long userID, Long itemId, LocalDateTime end);

    List<Booking> findByStatusAndItemInOrderByStartDesc(Status status, List<Item> items);
}
