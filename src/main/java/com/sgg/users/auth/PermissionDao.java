package com.sgg.users.auth;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "permissions") // TODO: how can we ensure these are unique entries?
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class PermissionDao {

	@Id
	@Column(name = "PERM_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int permId;

	@Column(name = "RESOURCE_TYPE")
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;

	@Column(name = "PERMISSION_TYPE")
	@Enumerated(EnumType.STRING)
	private PermissionType permissionType;

	@Column(name = "RESOURCE_ID")
	private Long resourceId;

	@OneToMany(mappedBy = "permissionDao")
	private List<UserPermissionDao> userPermissionDaos;

}