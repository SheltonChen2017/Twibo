package edu.miis.Service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyAwarePasswordEncoderTest {

    private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();

    @Test
    void encodesNewPasswordsWithBcrypt() {
        String encoded = encoder.encode("a long test password");

        assertThat(encoded).startsWith("$2");
        assertThat(encoder.matches("a long test password", encoded)).isTrue();
        assertThat(encoder.matches("wrong password", encoded)).isFalse();
        assertThat(encoder.upgradeEncoding(encoded)).isFalse();
    }

    @Test
    void acceptsLegacyPlaintextButMarksItForUpgrade() {
        assertThat(encoder.matches("legacy-password", "legacy-password")).isTrue();
        assertThat(encoder.matches("wrong", "legacy-password")).isFalse();
        assertThat(encoder.upgradeEncoding("legacy-password")).isTrue();
    }
}
