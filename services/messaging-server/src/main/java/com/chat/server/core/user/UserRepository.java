package com.chat.server.core.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chat.server.api.user.dto.UserSearchResponse;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 * Provides database operations for users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT new com.chat.server.api.user.dto.UserSearchResponse(u.userId, u.username, u.profilePicture) " +
           "FROM User u " +
           "WHERE u.isActive = true AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    List<UserSearchResponse> searchUsersByKeyword(@Param("query") String query);
}
