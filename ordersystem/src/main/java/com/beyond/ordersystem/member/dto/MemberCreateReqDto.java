package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.member.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateReqDto {
    private String name;
    @NotEmpty(message = "email은 필수값입니다.")
    private String email;
    @NotEmpty(message = "password는 필수값입니다.")
    @Size(min = 8, message = "password의 최소 길이는 8입니다.")
    private String password;
    private Address address;
    private Role role = Role.USER;

    public Member toEntity(String password) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(password)
                .address(this.address)
                .role(this.role)
                .orderList(new ArrayList<>())
                .delYn("N")
                .build();
    }
}
