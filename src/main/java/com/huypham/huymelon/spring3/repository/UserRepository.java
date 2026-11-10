package com.huypham.huymelon.spring3.repository;

import com.huypham.huymelon.spring3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    java.util.Optional<User> findByUsernameIgnoreCase(String username);
}
