package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.BaseEntity;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member",fetch = FetchType.LAZY)
    private List<Ordering> orderList;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    @Column(nullable = false, columnDefinition = "char(1) default 'N'")
    private String delYn = "N";

    public void resetPassword(String encode) {
        this.password = encode;
    }

    public MemberResDto fromEntity() {
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .orderCount(this.orderList.size())
                .build();
    }
}
