package com.chat.server.core.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * User entity for authentication and user management
 * Stores user credentials and profile information
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String userId;

    private String profilePicture;

    @Column(nullable = false)
    private Boolean isActive = true;
    
    private String displayName;

    private String aboutMe = "Hey there! I am using this chat.";

    // @Column(nullable = false)
    // private String presenceStatus = "OFFLINE";

    @Column(name = "is_email_verified", nullable = false, columnDefinition = "boolean default false")
    private boolean isEmailVerified = false;

    @Column(name = "presence_status", nullable = false, columnDefinition = "varchar(255) default 'OFFLINE'")
    private String presenceStatus = "OFFLINE";

    private Long lastSeenAt;

    // @Column(nullable = false)
    // private Boolean isEmailVerified = true;

    private Long emailVerifiedAt;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    private Long updatedAt;

    public User() {
        long current = System.currentTimeMillis();
        this.createdAt = current;
        this.updatedAt = current;
        this.lastSeenAt = current;
    }

    public User(String username, String email, String password, String userId) {
        long current = System.currentTimeMillis();
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.displayName = username;
        this.createdAt = current;
        this.updatedAt = current;
        this.lastSeenAt = current;
    }

    @PrePersist
    protected void onCreate() {
        long current = System.currentTimeMillis();
        this.createdAt = current;
        this.updatedAt = current;
        if (this.lastSeenAt == null) this.lastSeenAt = current;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getDisplayName() { 
        return displayName != null ? displayName : username; 
    }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }

    public String getPresenceStatus() { return presenceStatus; }
    public void setPresenceStatus(String presenceStatus) { this.presenceStatus = presenceStatus; }

    public Long getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Long lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Long emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
}
