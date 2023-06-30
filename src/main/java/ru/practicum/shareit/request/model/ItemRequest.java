package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requests", schema = "public")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String description;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;
    private LocalDateTime created;

    @OneToMany(targetEntity = Item.class,
            mappedBy = "request",
            fetch = FetchType.LAZY)
    @ToString.Exclude
    List<Item> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRequest that = (ItemRequest) o;
        return id == that.id && Objects.equals(description, that.description)
                && Objects.equals(requester, that.requester) && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
