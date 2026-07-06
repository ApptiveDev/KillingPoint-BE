package apptive.team5.recommendation.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.Locale;

public enum MusicMetadataSourceType {
    ITUNES;

    @JsonCreator
    public static MusicMetadataSourceType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(sourceType -> sourceType.name().equals(normalized))
                .findFirst()
                .orElse(null);
    }
}
