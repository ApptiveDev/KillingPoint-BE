package apptive.team5.admin.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;

import java.time.Duration;
import java.time.LocalDateTime;

public record AdminUgcItem(
        Long id,
        String displayId,
        String musicTitle,
        String artist,
        String albumImageUrl,
        String videoUrl,
        String username,
        Long userId,
        String segment,
        String registeredLabel,
        String content,
        String scopeLabel,
        long reportCount,
        String statusLabel,
        String statusClass,
        String filterState
) {

    public static AdminUgcItem from(DiaryEntity diary, long reportCount, LocalDateTime now) {
        String displayId = "KP-" + diary.getId();
        String username = diary.getUser().getUsername();
        String segment = diary.getStart() + "-" + diary.getEnd();
        String statusLabel = reportCount > 0 ? "신고 " + reportCount : "공개";
        String statusClass = reportCount > 0 ? "reported" : "";

        return new AdminUgcItem(
                diary.getId(),
                displayId,
                diary.getMusicTitle(),
                diary.getArtist(),
                diary.getAlbumImageUrl(),
                toYoutubeEmbedUrl(diary.getVideoUrl()),
                username,
                diary.getUser().getId(),
                segment,
                formatRegisteredLabel(diary.getCreateDateTime(), now),
                diary.getContent(),
                scopeLabel(diary.getScope()),
                reportCount,
                statusLabel,
                statusClass,
                filterState(reportCount)
        );
    }

    private static String filterState(long reportCount) {
        StringBuilder builder = new StringBuilder("all");
        if (reportCount > 0) {
            builder.append(" reported");
        }
        return builder.toString();
    }

    private static String toYoutubeEmbedUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            return "about:blank";
        }

        String trimmedUrl = videoUrl.trim();
        String videoId = extractYoutubeVideoId(trimmedUrl);
        if (videoId == null || videoId.isBlank()) {
            return "about:blank";
        }

        return "https://www.youtube-nocookie.com/embed/" + videoId;
    }

    private static String extractYoutubeVideoId(String videoUrl) {
        String marker;
        if (videoUrl.contains("/embed/")) {
            marker = "/embed/";
            return readVideoId(videoUrl.substring(videoUrl.indexOf(marker) + marker.length()));
        }
        if (videoUrl.contains("watch?v=")) {
            marker = "watch?v=";
            return readVideoId(videoUrl.substring(videoUrl.indexOf(marker) + marker.length()));
        }
        if (videoUrl.contains("youtu.be/")) {
            marker = "youtu.be/";
            return readVideoId(videoUrl.substring(videoUrl.indexOf(marker) + marker.length()));
        }
        if (videoUrl.contains("/shorts/")) {
            marker = "/shorts/";
            return readVideoId(videoUrl.substring(videoUrl.indexOf(marker) + marker.length()));
        }
        if (!videoUrl.contains("/") && videoUrl.length() == 11) {
            return videoUrl;
        }

        return null;
    }

    private static String readVideoId(String value) {
        int end = value.length();
        for (String delimiter : new String[]{"?", "&", "/"}) {
            int index = value.indexOf(delimiter);
            if (index >= 0) {
                end = Math.min(end, index);
            }
        }
        return value.substring(0, end);
    }

    private static String scopeLabel(DiaryScope scope) {
        return switch (scope) {
            case PUBLIC -> "전체공개";
            case KILLING_PART -> "킬링파트만 공개";
            case PRIVATE -> "비공개";
        };
    }

    private static String formatRegisteredLabel(LocalDateTime createdAt, LocalDateTime now) {
        long minutes = Math.max(0, Duration.between(createdAt, now).toMinutes());
        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        return (hours / 24) + "일 전";
    }
}
