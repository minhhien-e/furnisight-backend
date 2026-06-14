package com.furnisight.user.domain.entities.profile;

import com.furnisight.user.domain.seedwork.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@jakarta.persistence.Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "province_code", nullable = false, length = 20)
    private String provinceCode;

    @Column(name = "province_name", nullable = false, length = 100)
    private String provinceName;

    @Column(name = "ward_code", nullable = false, length = 20)
    private String wardCode;

    @Column(name = "ward_name", nullable = false, length = 100)
    private String wardName;

    @Column(name = "detail", nullable = false, length = 255)
    private String detail;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public UserAddress(UUID accountId, String fullName, String phone,
                       String provinceCode, String provinceName,
                       String wardCode, String wardName,
                       String detail, String type, boolean isDefault) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.fullName = fullName;
        this.phone = phone;
        this.provinceCode = provinceCode;
        this.provinceName = provinceName;
        this.wardCode = wardCode;
        this.wardName = wardName;
        this.detail = detail;
        this.type = type;
        this.isDefault = isDefault;
    }
}
