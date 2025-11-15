package com.miniproject.cafe.Controller;


import com.miniproject.cafe.Service.MemberService;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // ⭐ 이메일 중복 체크 API
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {

        boolean idDuplicate = memberService.isIdDuplicate(email);
        boolean emailDuplicate = memberService.isEmailDuplicate(email);

        if (idDuplicate || emailDuplicate) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }

        return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
    }

    // ⭐ 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody MemberVO vo) {

        if (vo.getId() == null) vo.setId(vo.getEmail());

        String result = memberService.registerMember(vo);

        return switch (result) {
            case "SUCCESS" -> ResponseEntity.ok(Map.of("message", "가입 성공!"));
            case "PASSWORD_MISMATCH" ->
                    ResponseEntity.badRequest().body(Map.of("field", "passwordCheck"));
            case "ID_DUPLICATE" ->
                    ResponseEntity.status(409).body(Map.of("field", "id"));
            case "EMAIL_DUPLICATE" ->
                    ResponseEntity.status(409).body(Map.of("field", "email"));
            default ->
                    ResponseEntity.internalServerError().body(Map.of("message", "오류 발생"));
        };
    }
}