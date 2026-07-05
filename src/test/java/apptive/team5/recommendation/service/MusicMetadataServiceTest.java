package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MusicMetadataServiceTest {

    @InjectMocks
    private MusicMetadataService musicMetadataService;

    @Mock
    private MusicMetadataLowService musicMetadataLowService;

    @Test
    @DisplayName("이미 존재하는 메타데이터가 있으면 재사용한다")
    void findOrCreate_returnsExistingMetadata() {
        MusicMetadataEntity existing = new MusicMetadataEntity(
                MusicMetadataSourceType.ITUNES,
                "track-1",
                "artist-1",
                "K-Pop"
        );

        given(musicMetadataLowService.findBySourceTypeAndSourceTrackId(MusicMetadataSourceType.ITUNES, "track-1"))
                .willReturn(Optional.of(existing));

        MusicMetadataEntity result = musicMetadataService.findOrCreate(
                MusicMetadataSourceType.ITUNES,
                "track-1",
                "artist-1",
                "K-Pop"
        );

        assertThat(result).isEqualTo(existing);
        verify(musicMetadataLowService, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("메타데이터가 없으면 새로 생성해서 저장한다")
    void findOrCreate_savesNewMetadata() {
        given(musicMetadataLowService.findBySourceTypeAndSourceTrackId(MusicMetadataSourceType.ITUNES, "track-1"))
                .willReturn(Optional.empty());

        musicMetadataService.findOrCreate(
                MusicMetadataSourceType.ITUNES,
                "track-1",
                "artist-1",
                "K-Pop"
        );

        ArgumentCaptor<MusicMetadataEntity> captor = ArgumentCaptor.forClass(MusicMetadataEntity.class);
        verify(musicMetadataLowService).save(captor.capture());
        assertThat(captor.getValue().getSourceTrackId()).isEqualTo("track-1");
        assertThat(captor.getValue().getSourceArtistId()).isEqualTo("artist-1");
        assertThat(captor.getValue().getPrimaryGenreNameRaw()).isEqualTo("K-Pop");
    }

    @Test
    @DisplayName("소스 타입이나 트랙 아이디가 없으면 메타데이터를 만들지 않는다")
    void findOrCreate_withoutRequiredKey_returnsNull() {
        MusicMetadataEntity result = musicMetadataService.findOrCreate(
                MusicMetadataSourceType.ITUNES,
                " ",
                "artist-1",
                "K-Pop"
        );

        assertThat(result).isNull();
        verify(musicMetadataLowService, never()).findBySourceTypeAndSourceTrackId(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(musicMetadataLowService, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
