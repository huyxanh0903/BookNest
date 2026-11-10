package com.huypham.huymelon.spring3.repository;

import com.huypham.huymelon.spring3.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b JOIN FETCH b.category LEFT JOIN FETCH b.owner ORDER BY b.id DESC")
    List<Book> findAllWithCategoryAndOwnerOrderByIdDesc();

    @Query("SELECT b FROM Book b JOIN FETCH b.category LEFT JOIN FETCH b.owner WHERE b.owner.id = :ownerId ORDER BY b.id DESC")
    List<Book> findAllByOwner_IdWithCategoryAndOwnerOrderByIdDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Book b JOIN FETCH b.category LEFT JOIN FETCH b.owner WHERE b.id = :id")
    Optional<Book> findByIdWithCategoryAndOwner(@Param("id") Long id);

    @Query("SELECT b FROM Book b JOIN FETCH b.category LEFT JOIN FETCH b.owner WHERE b.id = :id AND b.owner.id = :ownerId")
    Optional<Book> findByIdAndOwner_IdWithCategoryAndOwner(@Param("id") Long id, @Param("ownerId") Long ownerId);

    List<Book> findAllByOwner_IdOrderByIdDesc(Long ownerId);
    Optional<Book> findByIdAndOwner_Id(Long id, Long ownerId);
    long countByOwner_Id(Long ownerId);
    void deleteByIdAndOwner_Id(Long id, Long ownerId);
}
