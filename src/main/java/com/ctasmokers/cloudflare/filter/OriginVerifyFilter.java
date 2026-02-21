package com.ctasmokers.cloudflare.filter;

import com.ctasmokers.aws.client.AwsSecretsClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;

@Component
public final class OriginVerifyFilter extends OncePerRequestFilter {
    private static final String API_PATH_PREFIX = "/api";
    private static final String ORIGIN_VERIFY_HEADER = "X-Origin-Verify";

    private final byte[] expectedOriginVerify;

    @Autowired
    public OriginVerifyFilter(AwsSecretsClient awsSecretsClient) {
        this.expectedOriginVerify = awsSecretsClient.getAppSecret()
                                                    .cloudflare()
                                                    .originVerify()
                                                    .getBytes();
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        if (!requestUri.startsWith(API_PATH_PREFIX)) {
            filterChain.doFilter(request, response);

            return;
        }

        String originVerify = request.getHeader(ORIGIN_VERIFY_HEADER);

        if (originVerify == null) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        byte[] originVerifyBytes = originVerify.getBytes();

        if (!MessageDigest.isEqual(originVerifyBytes, this.expectedOriginVerify)) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        filterChain.doFilter(request, response);
    }
}
