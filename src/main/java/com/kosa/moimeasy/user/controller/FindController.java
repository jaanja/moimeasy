package com.kosa.moimeasy.user.controller;


import com.kosa.moimeasy.user.dto.*;
import com.kosa.moimeasy.user.entity.User;
import com.kosa.moimeasy.user.exception.InvalidPasswordResetException;
import com.kosa.moimeasy.user.exception.PasswordMismatchException;
import com.kosa.moimeasy.user.exception.UserNotFoundException;
import com.kosa.moimeasy.user.service.FindService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Validated
public class FindController {

    @Autowired
    private FindService findService;

    /**
     * 이메일 찾기 API
     */
    @PostMapping("/find/email")
    public ResponseEntity<EmailFindResponse> findEmail(@Valid @RequestBody EmailFindRequest request) {
        try {
            String email = findService.findEmailByNicknameAndPhone(request.getNickname(), request.getPhone());
            return ResponseEntity.ok(new EmailFindResponse(email));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    /**
     * 회원 조회
     */
    @PostMapping("/find/user")
    public ResponseEntity<?> findUser(@Valid @RequestBody FindUserRequest request) {
        try {
            // 회원 정보 일치 여부 확인
            User user = findService.findUserByNicknameAndPhoneAndEmail(
                    request.getNickname(),
                    request.getPhone(),
                    request.getEmail()
            );

            // 성공 시 응답 반환
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "회원 정보가 확인되었습니다."
            ));

        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "exists", false,
                            "message", ex.getMessage()
                    ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "exists", false,
                            "message", "서버 오류가 발생했습니다."
                    ));
        }
    }

    /**
     * 비밀번호 초기화
     */
    @PostMapping("/reset/password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            PasswordResetResponse response = findService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (InvalidPasswordResetException ex) {
            throw ex;
        } catch (PasswordMismatchException ex) {
            throw ex;
        } catch (UserNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ex;
        }
    }

}
