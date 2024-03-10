package com.sgg.users.authz;

import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "permissions",
		uniqueConstraints = @UniqueConstraint(columnNames = {"RESOURCE_TYPE", "PERMISSION_TYPE", "RESOURCE_ID"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class PermissionDao {

	@Id
	@Column(name = "PERM_ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int permId;

	@Column(name = "RESOURCE_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private ResourceType resourceType;

	@Column(name = "PERMISSION_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private PermissionType permissionType;

	@Column(name = "RESOURCE_ID", nullable = false)
	private Long resourceId;

	@OneToMany(mappedBy = "permissionDao")
	private List<UserPermissionDao> userPermissionDaos;

}