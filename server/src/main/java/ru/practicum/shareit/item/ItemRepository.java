package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;


public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByOwnerIdOrderByIdAsc(Long userId, Pageable pageable);

    @Query("select itm from Item itm " +
            "where itm.available = TRUE " +
            "and (lower(itm.name) like lower(concat('%', ?1,'%')) " +
            "or lower(itm.description) like lower(lower(concat('%', ?1,'%'))))")
    Page<Item> searchItemsByText(String text, Pageable pageable);
}
