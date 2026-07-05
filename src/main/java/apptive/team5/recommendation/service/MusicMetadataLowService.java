package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import apptive.team5.recommendation.repository.MusicMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicMetadataLowService {

    private final MusicMetadataRepository musicMetadataRepository;

    public MusicMetadataEntity save(MusicMetadataEntity musicMetadata) {
        return musicMetadataRepository.save(musicMetadata);
    }

    @Transactional(readOnly = true)
    public Optional<MusicMetadataEntity> findBySourceTypeAndSourceTrackId(
            MusicMetadataSourceType sourceType,
            String sourceTrackId
    ) {
        return musicMetadataRepository.findBySourceTypeAndSourceTrackId(sourceType, sourceTrackId);
    }
}
