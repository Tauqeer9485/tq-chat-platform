package com.chat.server.core.user;

import com.chat.server.api.user.dto.UserSearchResponse;
import com.chat.server.security.JwtTokenProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User Service
 * Handles user registration, authentication, and profile management
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register new user
     * @param username username
     * @param email email address
     * @param password plain password (will be hashed)
     * @return User object if successful
     * @throws IllegalArgumentException if user already exists
     */
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, email, hashedPassword, userId);
        return userRepository.save(user);
    }

    /**
     * Authenticate user with username and password
     * @param username username
     * @param password plain password (will be compared with hash)
     * @return User object if authentication successful
     * @throws IllegalArgumentException if credentials invalid
     */
    public User authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password for user: " + username);
        }

        return user;
    }

    /**
     * Search public user profiles by matching keywords against username or display name.
     * Evaluates business rules and query filtering before hitting the database layer.
     */
    public List<UserSearchResponse> searchPublicUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        // This is where enterprise rules live. For example, in the future you could add:
        // - Filtering out banned users
        // - Filtering out users who set their profiles to "private"
        
        return userRepository.searchUsersByKeyword(query.trim());
    }

    /**
     * Find user by userId (UUID)
     * @param userId user UUID
     * @return User object
     */
    public Optional<User> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * Find user by username
     * @param username username
     * @return User object
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     * @param email email address
     * @return User object
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Generate JWT token for authenticated user
     * @param user User object
     * @return JWT token
     */
    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getUserId(), user.getUsername());
    }

    /**
     * Verify JWT token and extract user
     * @param token JWT token
     * @return User object if valid
     */
    public Optional<User> verifyToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return Optional.empty();
        }

        String userId = jwtTokenProvider.getUserIdFromToken(token);
        return findByUserId(userId);
    }
}
