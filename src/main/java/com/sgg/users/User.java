package com.sgg.users;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String username;

    private String password;

    @Column(name = "registration_time", updatable = false)
    private Timestamp registrationTime;

    private String salt;

    @OneToMany(mappedBy = "user")
    private List<UserPermission> userPermissions;

}
