package com.huypham.huymelon.spring3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Nationalized
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Nationalized
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "publishYear")
    private Integer publishYear;

    @Column(name = "coverImageUrl", length = 1000)
    private String coverImageUrl;

    @Column(name = "sourceUrl", length = 1000)
    private String sourceUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ColumnDefault("1")
    @Column(name = "available", nullable = false)
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoryId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @Transient
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

}
