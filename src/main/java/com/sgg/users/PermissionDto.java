package com.sgg.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PermissionDto {
	private int permId;

	// TODO: it may make more sense to have three fields representing the the "*:*:*" permission notation for the DTO
	private String permValue;
}