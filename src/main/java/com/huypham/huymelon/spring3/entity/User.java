package com.huypham.huymelon.spring3.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Nationalized
    @Column(name = "Username", nullable = false, length = 50)
    private String username;

    @Nationalized
    @Column(name = "Email", nullable = false)
    private String email;

    @Nationalized
    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Nationalized
    @ColumnDefault("N'USER'")
    @Column(name = "Role", nullable = false, length = 20)
    private String role;

    @ColumnDefault("1")
    @Column(name = "Enabled", nullable = false)
    private Boolean enabled;

    @ColumnDefault("sysutcdatetime()")
    @Column(name = "CreatedAt", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @Nationalized
    @Column(name = "FavoriteGenres", length = 500)
    private String favoriteGenres;

    @Nationalized
    @Column(name = "ReadingTopics", length = 500)
    private String readingTopics;

    @Nationalized
    @Column(name = "PreferredLanguage", length = 80)
    private String preferredLanguage;

    @Nationalized
    @Column(name = "ReadingGoal", length = 300)
    private String readingGoal;


}
