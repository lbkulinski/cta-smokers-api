package com.ctasmokers.aws.dto;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record Secret(
    CloudflareSecret cloudflare
) {
    public Secret {
        Objects.requireNonNull(cloudflare);
    }

    public record CloudflareSecret(String originVerify) {
        public CloudflareSecret {
            Objects.requireNonNull(originVerify);
        }
    }
}
