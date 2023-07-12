package com.sgg.users;

import io.micronaut.data.annotation.DateCreated;
import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username", name = "UQ_username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class UserDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String username;

    private String password;

    @DateCreated
    @Column(name = "registration_time", updatable = false)
    private OffsetDateTime registrationTime;

    @OneToMany(mappedBy = "userDao", cascade = CascadeType.ALL)
    private List<UserPermissionDao> userPermissionEntities;

    @OneToMany(mappedBy = "userDao", cascade = CascadeType.ALL)
    private List<RefreshTokenDao> refreshTokenDaos;

}
