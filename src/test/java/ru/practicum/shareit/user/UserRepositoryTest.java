package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
class UserRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    UserRepository userRepository;
    User user;

    @BeforeEach
    void setUp_createUser() {
        user = new User();
        user.setName("Ivanov Ivan");
        user.setEmail("ivan.ivanov@email.com");

        entityManager.getEntityManager().createNativeQuery(
                        "INSERT INTO users (email, name) VALUES (?,?)")
                .setParameter(1, user.getEmail())
                .setParameter(2, user.getName())
                .executeUpdate();

        long userId = entityManager.getEntityManager().createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", user.getEmail())
                .getSingleResult().getId();

        user.setId(userId);
    }

    @Test
    void saveUsers() {
        userRepository.save(user);

        User userFound = selectUserById(user.getId());

        assertThat(user, equalTo(userFound));
        assertThat(user.toString(), equalTo(userFound.toString()));
    }

    @Test
    void updateUser() {
        user.setEmail("newEmail@email.com");
        user.setName("New Name");
        userRepository.save(user);

        User userFound = selectUserById(user.getId());

        assertThat(userFound.getEmail(), equalTo("newEmail@email.com"));
        assertThat(userFound.getName(), equalTo("New Name"));
    }

    @Test
    void findAll() {
        User user2 = new User();
        user2.setName("Petrov Petr");
        user2.setEmail("petrov@email.com");
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertThat(users, hasSize(2));
        assertThat(users.get(0), equalTo(user));
        assertThat(users.get(1), equalTo(user2));

    }

    @Test
    void deleteById() {
        long id = user.getId();

        userRepository.deleteById(id);
        Optional<User> userFound = userRepository.findById(id);

        assertThat(userFound.isPresent(), equalTo(false));
    }


    @AfterEach
    void cleanUp() {
        entityManager.getEntityManager().createQuery("delete from User").executeUpdate();
    }

    private User selectUserById(long id) {
        return entityManager.getEntityManager().createQuery(
                        "SELECT u FROM User u WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}