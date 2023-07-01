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
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.model.ItemRequest;
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
@Transactional
class ItemRequestRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    ItemRequestRepository itemRequestRepository;
    ItemRequest itemRequest;
    ItemRequest itemRequest2;
    User user;

    @BeforeEach
    void setUp() {
        user = createTestUserSavedToDB("Ivanov Ivan", "ivan.ivanov@email.com");
        User user2 = createTestUserSavedToDB("Ivanov2 Ivan2", "ivan2.ivanov2@email.com");
        itemRequest = createItemRequestSavedToDB(user, "Description");
        itemRequest2 = createItemRequestSavedToDB(user2, "Description2");
    }


    @AfterEach
    void tearDown() {
        entityManager.getEntityManager().createQuery("delete from ItemRequest").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from User").executeUpdate();
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

    private User createTestUserSavedToDB(String name, String email) {
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

    private ItemRequest createItemRequestSavedToDB(User user, String description) {
        ItemRequest ir = ItemRequest.builder()
                .requester(user)
                .description(description)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO requests (requester_id, description, created) VALUES (?,?,?)")
                .setParameter(1, user.getId())
                .setParameter(2, description)
                .setParameter(3, ir.getCreated())
                .executeUpdate();

        BigInteger id = (BigInteger) entityManager.getEntityManager()
                .createNativeQuery("SELECT max(id) FROM requests").getSingleResult();
        ir.setId(id.longValue());

        return ir;
    }
}