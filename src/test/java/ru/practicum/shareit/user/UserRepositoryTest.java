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
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
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

        userRepository.save(user);
    }

    @Test
    void saveUsers_test() {
        Optional<User> userFound = userRepository.findById(user.getId());

        assertThat(userFound.get(), equalTo(user));
        assertThat(userFound.get().toString(), equalTo(user.toString()));
    }

    @Test
    void updateUser_test() {
        user.setEmail("newEmail@email.com");
        user.setName("New Name");

        Optional<User> userUpdated = userRepository.findById(user.getId());

        assertThat(userUpdated.get().getEmail(), equalTo("newEmail@email.com"));
        assertThat(userUpdated.get().getName(), equalTo("New Name"));
    }

    @Test
    void findAll_test() {
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
    void deleteUsers() {
        userRepository.deleteAll();
    }
}