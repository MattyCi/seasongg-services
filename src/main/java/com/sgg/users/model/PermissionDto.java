package com.sgg.users.model;

import com.sgg.users.auth.PermissionType;
import com.sgg.users.auth.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDto {
	private int permId;
	private ResourceType resourceType;
	private PermissionType permissionType;
	private Long resourceId;
}