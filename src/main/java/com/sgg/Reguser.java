package com.sgg;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="regusers")
@NamedQuery(name="Reguser.findAll", query="SELECT r FROM Reguser r")
public class Reguser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="USER_ID")
    private Long userId;

    private String username;

    @JsonIgnore
    private String password;

    @OneToMany(mappedBy="reguser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserPermission> userPermissions;

    public Reguser() {
    }

    public Reguser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<UserPermission> getUserPermissions() {
        return this.userPermissions;
    }

    public void setUserPermissions(List<UserPermission> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public UserPermission addUserPermission(UserPermission userPermission) {
        getUserPermissions().add(userPermission);
        userPermission.setReguser(this);

        return userPermission;
    }

    public UserPermission removeUserPermission(UserPermission userPermission) {
        getUserPermissions().remove(userPermission);
        userPermission.setReguser(null);

        return userPermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reguser reguser = (Reguser) o;

        if (!Objects.equals(userId, reguser.userId)) return false;
        if (!username.equals(reguser.username)) return false;
        if (!password.equals(reguser.password)) return false;
        return Objects.equals(userPermissions, reguser.userPermissions);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (userPermissions != null ? userPermissions.hashCode() : 0);
        return result;
    }
}
