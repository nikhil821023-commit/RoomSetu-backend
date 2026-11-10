package com.roomsetu.repository;

import com.roomsetu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);
    boolean existsByAdminTrue();

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.workEmail) = LOWER(:email) AND u.id <> :userId")
    boolean isWorkEmailTakenByAnother(@Param("email") String email, @Param("userId") Long userId);
}