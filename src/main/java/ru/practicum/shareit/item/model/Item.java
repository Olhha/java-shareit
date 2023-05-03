package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;


@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "items", schema = "public")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private String description;

    @Column(name = "is_available", nullable = false)
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Transient
    private ItemRequest request;
}
