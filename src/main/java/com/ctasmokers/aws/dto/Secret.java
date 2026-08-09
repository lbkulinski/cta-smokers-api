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

        @Override
        public String toString() {
            return "CloudflareSecret{originVerify=REDACTED}";
        }
    }

    public record RollbarSecret(String accessToken) {
        public RollbarSecret {
            Objects.requireNonNull(accessToken);
        }

        @Override
        public String toString() {
            return "RollbarSecret{accessToken=REDACTED}";
        }
    }
}
