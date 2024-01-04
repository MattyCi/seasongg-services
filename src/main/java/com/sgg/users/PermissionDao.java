package com.sgg.users;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PermissionDao {

	@Id
	@Column(name = "PERM_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int permId;

	@Column(name = "PERM_VALUE")
	private String permValue;

	@OneToMany(mappedBy = "permissionDao")
	private List<UserPermissionDao> userPermissionDaos;

}