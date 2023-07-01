package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    Page<Booking> findByItemOwnerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, Status status, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, Status status, Pageable pageable);

    Page<Booking> findByBookerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end, Pageable pageable);

    Booking getFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
            Long idItem, Status status, LocalDateTime now);

    Booking getFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long idItem, Status status, LocalDateTime now);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(
            Long userID, Long itemId, LocalDateTime end);

    List<Booking> findByStatusAndItemInOrderByStartDesc(
            Status status, List<Item> items);
}
