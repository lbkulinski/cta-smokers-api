package com.ctasmokers.cloudflare.filter;

import com.ctasmokers.aws.client.AwsSecretsClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

@Component
public final class OriginVerifyFilter extends OncePerRequestFilter {
    private static final String HEALTH_CHECK_PATH = "/actuator/health";
    private static final String CF_CONNECTING_IP_HEADER = "CF-Connecting-IP";
    private static final String ORIGIN_VERIFY_HEADER = "X-Origin-Verify";

    private final byte[] expectedOriginVerify;

    @Autowired
    public OriginVerifyFilter(AwsSecretsClient awsSecretsClient) {
        this.expectedOriginVerify = awsSecretsClient.getAppSecret()
                                                    .cloudflare()
                                                    .originVerify()
                                                    .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        if (Objects.equals(requestPath, HEALTH_CHECK_PATH)) {
            filterChain.doFilter(request, response);

            return;
        }

        String cfConnectingIp = request.getHeader(CF_CONNECTING_IP_HEADER);

        if ((cfConnectingIp == null) || cfConnectingIp.isBlank()) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        if (!InetAddressValidator.getInstance()
                                 .isValid(cfConnectingIp)) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        String originVerify = request.getHeader(ORIGIN_VERIFY_HEADER);

        if (originVerify == null) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        byte[] originVerifyBytes = originVerify.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(originVerifyBytes, this.expectedOriginVerify)) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        filterChain.doFilter(request, response);
    }
}
