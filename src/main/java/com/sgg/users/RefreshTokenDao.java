package com.sgg.users;

import io.micronaut.data.annotation.DateCreated;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserDao userDao;

    private String refreshToken;

    private Boolean revoked;

    @DateCreated
    private Instant dateCreated;

}
