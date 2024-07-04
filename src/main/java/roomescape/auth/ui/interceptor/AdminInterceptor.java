package roomescape.auth.ui.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.application.CookieUtils;
import roomescape.auth.application.JwtTokenProvider;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.MemberRole;

import java.io.IOException;

public class AdminInterceptor implements HandlerInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;

    public AdminInterceptor(JwtTokenProvider jwtTokenProvider, CookieUtils cookieUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieUtils = cookieUtils;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        Cookie[] cookies = request.getCookies();
        try {
            String token = cookieUtils.getCookieByName(cookies, "token")
                    .orElseThrow(() -> UnauthorizedException.of("토큰이 없습니다."))
                    .getValue();
            if (!jwtTokenProvider.extractMemberRole(token).equals(MemberRole.ADMIN.name())) {
                throw UnauthorizedException.of("관리자 페이지 권한이 없습니다.");
            }
        }
        catch (UnauthorizedException exception) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write("Error: " + exception.getMessage());
            return false;
        }
        return true;
    }
}
