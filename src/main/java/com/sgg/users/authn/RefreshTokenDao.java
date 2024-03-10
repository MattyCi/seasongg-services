package com.sgg.users.authn;

import com.sgg.users.UserDao;
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
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserDao userDao;

    @Column(name = "REFRESH_TOKEN", nullable = false)
    private String refreshToken;

    @Column(name = "REVOKED", nullable = false)
    private Boolean revoked;

    @DateCreated
    @Column(name = "DATE_CREATED", nullable = false)
    private Instant dateCreated;
}
