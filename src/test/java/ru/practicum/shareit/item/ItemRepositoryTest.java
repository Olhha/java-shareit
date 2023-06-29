package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;

    Item item;
    User user;

    final Pageable page = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Ivanov Ivan");
        user.setEmail("ivan.ivanov@email.com");
        userRepository.save(user);

        ItemRequest request = new ItemRequest();
        request.setDescription("Item request description");
        request.setRequester(user);
        itemRequestRepository.save(request);

        item = new Item();
        item.setName("something");
        item.setDescription("Item description");
        item.setOwner(user);
        item.setAvailable(true);
        item.setRequest(request);
        itemRepository.save(item);

    }

    @Test
    void saveItem_test() {
        Optional<Item> itemFound = itemRepository.findById(item.getId());

        assertThat(itemFound.get(), equalTo(item));
        assertThat(itemFound.get().toString(), equalTo(item.toString()));
    }

    @Test
    void findByOwnerId() {
        List<Item> items = itemRepository.findByOwnerId(user.getId(), page).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0), equalTo(item));
    }

    @Test
    void searchItemsByText() {
        List<Item> items = itemRepository.searchItemsByText("thing", page).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0), equalTo(item));

        List<Item> items2 = itemRepository.searchItemsByText("any", page).getContent();
        assertThat(items2.size(), equalTo(0));
    }

    @Test
    void updateItem_test() {
        item.setName("New Name");
        item.setAvailable(false);

        Optional<Item> itemFound = itemRepository.findById(item.getId());

        assertThat(itemFound.get().getName(), equalTo("New Name"));
        assertThat(itemFound.get().getAvailable(), equalTo(false));
    }



    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }
}