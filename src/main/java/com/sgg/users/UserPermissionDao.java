package com.sgg.users;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserPermissionDao {

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