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
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
class ItemRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ItemRepository itemRepository;
    Item item;
    User user;
    final Pageable page = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        user = createUser("Ivanov Ivan", "ivan.ivanov@email.com");
        item = createItem("something", "Item description", true);
    }

    @Test
    void saveItem() {
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
    void updateItem() {
        item.setName("New Name");
        item.setAvailable(false);
        itemRepository.save(item);

        Optional<Item> itemFound = itemRepository.findById(item.getId());

        assertThat(itemFound.get().getName(), equalTo("New Name"));
        assertThat(itemFound.get().getAvailable(), equalTo(false));
    }

    @AfterEach
    void tearDown() {
        entityManager.getEntityManager().createQuery("delete from Item").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from User").executeUpdate();
    }

    private Item createItem(String name, String description, boolean available) {
        Item newItem = Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(user)
                .build();

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO items (owner_id, name, description, is_available) " +
                                "VALUES (?,?,?,?)")
                .setParameter(1, user.getId())
                .setParameter(2, newItem.getName())
                .setParameter(3, newItem.getDescription())
                .setParameter(4, newItem.getAvailable())
                .executeUpdate();

        BigInteger id = (BigInteger) entityManager.getEntityManager()
                .createNativeQuery("SELECT max(id) FROM items").getSingleResult();
        newItem.setId(id.longValue());
        return newItem;
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
}