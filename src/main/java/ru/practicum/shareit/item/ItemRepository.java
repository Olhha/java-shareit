package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long userId);

    @Query("select itm from Item itm " +
            "where itm.available = TRUE " +
            "and (lower(itm.name) like lower(concat('%', ?1,'%')) " +
            "or lower(itm.description) like lower(lower(concat('%', ?1,'%'))))")
    List<Item> searchItemsByText(String text);
}
