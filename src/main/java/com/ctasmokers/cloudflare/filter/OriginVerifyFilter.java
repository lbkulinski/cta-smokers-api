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
import java.util.Objects;

@Component
public final class OriginVerifyFilter extends OncePerRequestFilter {
    private static final String API_PATH_PREFIX = "/api";
    private static final String ORIGIN_VERIFY_HEADER = "X-Origin-Verify";

    private final AwsSecretsClient awsSecretsClient;

    @Autowired
    public OriginVerifyFilter(AwsSecretsClient awsSecretsClient) {
        this.awsSecretsClient = awsSecretsClient;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith(API_PATH_PREFIX)) {
            filterChain.doFilter(request, response);

            return;
        }

        String originVerify = request.getHeader(ORIGIN_VERIFY_HEADER);
        String expectedOriginVerify = this.awsSecretsClient.getAppSecret()
                                                           .cloudflare()
                                                           .originVerify();

        if (!Objects.equals(originVerify, expectedOriginVerify)) {
            response.sendError(HttpStatus.FORBIDDEN.value());

            return;
        }

        filterChain.doFilter(request, response);
    }
}
