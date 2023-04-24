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
class UserPermission {

	@Id
	@Column(name = "USER_PERM_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userPermId;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User user;

	@ManyToOne
	@JoinColumn(name = "PERM_ID")
	private Permission permission;

}