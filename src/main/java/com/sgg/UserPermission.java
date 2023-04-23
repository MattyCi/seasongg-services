package com.sgg;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="user_permissions")
@NamedQuery(name="UserPermission.findAll", query="SELECT u FROM UserPermission u")
public class UserPermission implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="USER_PERM_ID")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int userPermId;

	//bi-directional many-to-one association to Reguser
	@ManyToOne
	@JoinColumn(name="USER_ID")
	private Reguser reguser;

	//bi-directional many-to-one association to Permission
	@ManyToOne
	@JoinColumn(name="PERM_ID")
	private Permission permission;

	public UserPermission() {
	}

	public int getUserPermId() {
		return this.userPermId;
	}

	public void setUserPermId(int userPermId) {
		this.userPermId = userPermId;
	}

	public Reguser getReguser() {
		return this.reguser;
	}

	public void setReguser(Reguser reguser) {
		this.reguser = reguser;
	}

	public Permission getPermission() {
		return this.permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

}