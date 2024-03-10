package com.sgg.users.auth;

import com.sgg.users.UserDao;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "user_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class UserPermissionDao {

	// TODO: you should not be able to persist duplicate rows!
	@Id
	@Column(name = "USER_PERM_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userPermId;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private UserDao userDao;

	@ManyToOne
	@JoinColumn(name = "PERM_ID")
	private PermissionDao permissionDao;

}