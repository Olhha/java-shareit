package ru.practicum.shareit.request;

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
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ItemRequestRepository itemRequestRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    ItemRequest itemRequest;
    ItemRequest itemRequest2;
    User user;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        userRepository.save(owner);

        Item item = Item.builder()
                .name("Book")
                .description("Interesting book")
                .available(true)
                .owner(owner)
                .build();

        user = new User();
        user.setName("Ivanov Ivan");
        user.setEmail("ivan.ivanov@email.com");
        userRepository.save(user);

        User user2 = new User();
        user2.setName("Ivanov2 Ivan2");
        user2.setEmail("ivan2.ivanov2@email.com");
        userRepository.save(user2);

        itemRepository.save(item);

        itemRequest = ItemRequest.builder()
                .items(List.of(item))
                .requester(user)
                .description("Item request description")
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(itemRequest);

        itemRequest2 = ItemRequest.builder()
                .items(List.of(item))
                .requester(user2)
                .description("Item request description2")
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(itemRequest2);
    }

    @AfterEach
    void tearDown() {
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findByRequesterId() {
        List<ItemRequest> requests = itemRequestRepository.findByRequesterId(
                user.getId(), Sort.by(Sort.Direction.DESC, "created"));
        assertThat(requests, hasSize(1));
        assertThat(requests.get(0), equalTo(itemRequest));
    }

    @Test
    void findAllByRequesterIdNot() {
        Pageable page = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(user.getId(), page).getContent();

        assertThat(requests, hasSize(1));
        assertThat(requests.get(0), equalTo(itemRequest2));
    }
}