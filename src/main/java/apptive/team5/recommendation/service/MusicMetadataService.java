package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import apptive.team5.recommendation.repository.MusicMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicMetadataService {

    private final MusicMetadataRepository musicMetadataRepository;

    public MusicMetadataEntity findOrCreate(
            MusicMetadataSourceType sourceType,
            String sourceTrackId,
            String sourceArtistId,
            String primaryGenreName
    ) {
        if (sourceType == null || sourceTrackId == null || sourceTrackId.isBlank()) {
            return null;
        }

        return musicMetadataRepository.findBySourceTypeAndSourceTrackId(sourceType, sourceTrackId)
                .orElseGet(() -> musicMetadataRepository.save(
                        new MusicMetadataEntity(
                                sourceType,
                                sourceTrackId,
                                sourceArtistId,
                                primaryGenreName
                        )
                ));
    }
}
