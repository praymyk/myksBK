package com.myks.myksbk.global.exception;

import lombok.Getter;

/**
 * 인증(Authentication) 실패 시 예외
 * <p>
 * 주로 다음과 같은 상황에서 발생:
 * 1. 로그인 실패 (아이디/비밀번호 불일치)
 * 2. 유효하지 않은 토큰 (Access Token 위변조, 만료 등)
 * 3. 리프레시 토큰 만료
 * 4. 인증 헤더(Authorization) 누락
 * <p>
 * HTTP 상태 코드 401 (Unauthorized)와 매핑됩니다.
 * 프론트엔드 인터셉터가 이 예외를 감지하면 토큰 갱신(Refresh)을 시도합니다.
 */
@Getter
public class UnauthorizedException extends RuntimeException {

    /**
     * 예외 코드 (기본값: "UNAUTHORIZED")
     * <p>
     * 예: "INVALID_TOKEN", "EXPIRED_TOKEN", "LOGIN_FAILED" 등으로 세분화 가능
     */
    private final String code;

    /**
     * 기본 코드로 예외 생성
     * @param message 에러 메시지
     */
    public UnauthorizedException(String message) {
        super(message);
        this.code = "UNAUTHORIZED";
    }

    /**
     * 커스텀 코드로 예외 생성 (상황별 세분화 필요 시 사용)
     * @param code 커스텀 에러 코드 (예: "EXPIRED_TOKEN")
     * @param message 에러 메시지
     */
    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }
}