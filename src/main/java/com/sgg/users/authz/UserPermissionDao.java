package com.sgg.users.authz;

import com.sgg.users.UserDao;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "user_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "perm_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public
class UserPermissionDao {

	@Id
	@Column(name = "user_perm_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userPermId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDao userDao;

	@ManyToOne
	@JoinColumn(name = "perm_id")
	private PermissionDao permissionDao;
}