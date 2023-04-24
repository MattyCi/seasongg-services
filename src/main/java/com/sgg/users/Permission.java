package com.sgg.users;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Permission {

	@Id
	@Column(name = "PERM_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int permId;

	@Column(name = "PERM_VALUE")
	private String permValue;

	@OneToMany(mappedBy = "permission")
	private List<UserPermission> userPermissions;

}