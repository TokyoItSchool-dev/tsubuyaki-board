package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "app_users")
public class User {

    public static final String DEFAULT_AVATAR_COLOR = "#6b7280";

    @Id
    @SequenceGenerator(name = "app_users_seq_gen", sequenceName = "app_users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_users_seq_gen")
    private Long id;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "avatar_color", length = 7, nullable = false)
    private String avatarColor;

    protected User() {
        // JPA
    }

    public User(String name, String avatarColor) {
        this.name = name;
        this.avatarColor = normalizeAvatarColor(avatarColor);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void updateAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.isBlank()) {
            return;
        }
        this.avatarColor = avatarColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private String normalizeAvatarColor(String color) {
        if (color == null || color.isBlank()) {
            return DEFAULT_AVATAR_COLOR;
        }
        return color;
    }
}
