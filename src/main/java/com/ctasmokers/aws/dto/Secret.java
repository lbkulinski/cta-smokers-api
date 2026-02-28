package com.ctasmokers.aws.dto;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record Secret(
    CloudflareSecret cloudflare,

    RollbarSecret rollbar
) {
    public Secret {
        Objects.requireNonNull(cloudflare);
        Objects.requireNonNull(rollbar);
    }

    public record CloudflareSecret(String originVerify) {
        public CloudflareSecret {
            Objects.requireNonNull(originVerify);
        }
    }

    public record RollbarSecret(String accessToken) {
        public RollbarSecret {
            Objects.requireNonNull(accessToken);
        }
    }
}
