package apptive.team5.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionComparatorTest {

    @Test
    @DisplayName("동일 버전 비교")
    void compareSameVersion() {
        assertThat(VersionComparator.compare("1.0.0", "1.0.0")).isEqualTo(0);
    }

    @Test
    @DisplayName("major 버전 비교")
    void compareMajor() {
        assertThat(VersionComparator.compare("1.0.0", "2.0.0")).isNegative();
        assertThat(VersionComparator.compare("2.0.0", "1.0.0")).isPositive();
    }

    @Test
    @DisplayName("minor 버전 비교")
    void compareMinor() {
        assertThat(VersionComparator.compare("1.0.0", "1.1.0")).isNegative();
        assertThat(VersionComparator.compare("1.2.0", "1.1.0")).isPositive();
    }

    @Test
    @DisplayName("patch 버전 비교")
    void comparePatch() {
        assertThat(VersionComparator.compare("1.0.0", "1.0.1")).isNegative();
        assertThat(VersionComparator.compare("1.0.2", "1.0.1")).isPositive();
    }

    @Test
    @DisplayName("자릿수가 다른 버전 비교")
    void compareDifferentLength() {
        assertThat(VersionComparator.compare("1.0", "1.0.0")).isEqualTo(0);
        assertThat(VersionComparator.compare("1.0", "1.0.1")).isNegative();
    }

    @Test
    @DisplayName("isLowerThan 검증")
    void isLowerThan() {
        assertThat(VersionComparator.isLowerThan("0.9.0", "1.0.0")).isTrue();
        assertThat(VersionComparator.isLowerThan("1.0.0", "1.0.0")).isFalse();
        assertThat(VersionComparator.isLowerThan("1.1.0", "1.0.0")).isFalse();
    }
}
