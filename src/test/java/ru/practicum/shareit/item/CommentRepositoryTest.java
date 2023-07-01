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
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
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
class CommentRepositoryTest {
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    BookingRepository bookingRepository;
    Comment comment;
    Item item;
    Item item2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Ivanov Ivan");
        user.setEmail("ivan.ivanov@email.com");
        userRepository.save(user);

        item = Item.builder()
                .name("Book")
                .description("Interesting book")
                .available(true)
                .owner(user)
                .build();
        itemRepository.save(item);

        item2 = Item.builder()
                .name("Book2")
                .description("Interesting book2")
                .available(true)
                .owner(user)
                .build();
        itemRepository.save(item2);

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .build();
        bookingRepository.save(booking);

        comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setText("text");
        commentRepository.save(comment);

        Comment comment2 = new Comment();
        comment2.setItem(item2);
        comment2.setAuthor(user);
        comment2.setText("text");
        commentRepository.save(comment2);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findCommentsByItemId() {
        List<Comment> comments = commentRepository.findCommentsByItemId(item.getId());

        assertThat(comments, hasSize(1));
        assertThat(comments.get(0), equalTo(comment));
    }

    @Test
    void findByItemIn() {
        List<Comment> comments = commentRepository.findByItemIn(List.of(item, item2),
                Sort.by(Sort.Direction.DESC, "created"));

        assertThat(comments, hasSize(2));
    }
}