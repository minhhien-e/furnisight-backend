package com.furnisight.user.domain.entities.identity;

import com.furnisight.user.domain.enums.identity.Permission;
import com.furnisight.user.domain.seedwork.AggregateRoot;
import com.furnisight.user.domain.valueobjects.identity.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends AggregateRoot {

    @Id
    private UUID id;

    private int position;

    private RoleName name;

    @Column(nullable = false)
    private Long permissions = 0L;

    public Role(RoleName name, int position) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.name = name;
    }

    public boolean hasPermission(Permission permission) {
        return Permission.hasPermission(this.permissions, permission);
    }

    public void grantPermission(Permission permission) {
        this.permissions = Permission.addPermission(this.permissions, permission);
    }

    public void revokePermission(Permission permission) {
        this.permissions = Permission.removePermission(this.permissions, permission);
    }

    public void update(RoleName name, int position) {
        this.name = name;
        this.position = position;
    }

    public List<Permission> getPermissions() {
        return Permission.getPermissions(this.permissions);
    }
}
