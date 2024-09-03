package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.common.dto.CommonErrorDto;
import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRT;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/member/create")
    public ResponseEntity<CommonResDto> memberCreate(@Valid @RequestBody MemberCreateReqDto dto) {
        MemberResDto memberResDto = memberService.memberCreate(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "회원이 성공적으로 등록되었습니다.", memberResDto), HttpStatus.CREATED);
    }

    /**
     * admin만 회원 전체 목록 조회 가능
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<CommonResDto> memberList(@PageableDefault(sort = "createdTime",direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MemberResDto> memberResDtos = memberService.memberList(pageable);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"회원 목록이 성공적으로 조회되었습니다.",memberResDtos),HttpStatus.OK);
    }

    /**
     * 본인은 본인 회원 정보만 조회 가능
     */
    @GetMapping("/member/myinfo")
    public ResponseEntity<CommonResDto> myInfo() {
        MemberResDto memberResDto = memberService.myInfo();
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK,"회원 정보가 성공적으로 조회되었습니다.",memberResDto),HttpStatus.OK);

    }
    @PostMapping("/doLogin")
    public ResponseEntity<CommonResDto> doLogin(@RequestBody MemberLoginDto dto) {
//        이메일, 비밀번호 검증
        Member member = memberService.login(dto);
//        일치할 경우 accessToken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(),member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(),member.getRole().toString());

//        redis에 Email에 rt를 key:value 로 하여 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "로그인이 성공적으로 되었습니다.", loginInfo), HttpStatus.CREATED);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {
        log.info("refresh token");
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try {
//        코드를 통해 토큰 검증
            claims = Jwts.parser().setSigningKey(secretKeyRT).parseClaimsJws(rt).getBody();
        }catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST,"invalid refresh token"),HttpStatus.BAD_REQUEST);
        }
        String email = claims.getSubject();
        String role = claims.get("role").toString();

//        redis를 조회하여 rt 추가 검증
        Object storesRt = redisTemplate.opsForValue().get(email);
        if (storesRt == null || !storesRt.toString().equals(rt)) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST,"invalid refresh token"),HttpStatus.BAD_REQUEST);
        }
        String newAccessToken = jwtTokenProvider.createToken(email,role);
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAccessToken);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.CREATED, "토큰이 성공적으로 재발급되었습니다.", info), HttpStatus.CREATED);
    }
    @PatchMapping("/member/reset-password")
    public ResponseEntity<CommonResDto> resetPassword(@RequestBody MemberPasswordResetDto dto) {
        memberService.resetPassword(dto);
        return new ResponseEntity<>(new CommonResDto(HttpStatus.OK, "비밀번호가 성공적으로 변경되었습니다.",null), HttpStatus.OK);
    }

}
