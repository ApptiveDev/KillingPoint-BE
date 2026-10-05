package apptive.team5.diary.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.domain.CommentMentionEntity;
import apptive.team5.comment.domain.CommentReportEntity;
import apptive.team5.comment.domain.MentionTargetType;
import apptive.team5.comment.repository.CommentLikeRepository;
import apptive.team5.comment.repository.CommentMentionRepository;
import apptive.team5.comment.repository.CommentReportRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.domain.DiaryMemoEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.domain.model.DiaryStoreInfo;
import apptive.team5.diary.repository.DiaryLikeRepository;
import apptive.team5.diary.repository.DiaryMemoRepository;
import apptive.team5.diary.repository.DiaryOrderRepository;
import apptive.team5.diary.repository.DiaryReportRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.diary.service.DiaryOrderLowService;
import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.recommendation.repository.MusicMetadataRepository;
import apptive.team5.recommendation.repository.UserArtistPreferenceRepository;
import apptive.team5.recommendation.repository.UserGenrePreferenceRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiaryDeletionIntegrationTest {

    private static final String GENRE = "K-Pop";
    private static final String ARTIST_ID = "artist-1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EntityManager em;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private DiaryLikeRepository diaryLikeRepository;
    @Autowired
    private DiaryStoreRepository diaryStoreRepository;
    @Autowired
    private DiaryMemoRepository diaryMemoRepository;
    @Autowired
    private DiaryReportRepository diaryReportRepository;
    @Autowired
    private DiaryOrderRepository diaryOrderRepository;
    @Autowired
    private DiaryOrderLowService diaryOrderLowService;
    @Autowired
    private MusicMetadataRepository musicMetadataRepository;
    @Autowired
    private UserGenrePreferenceRepository userGenrePreferenceRepository;
    @Autowired
    private UserArtistPreferenceRepository userArtistPreferenceRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private CommentMentionRepository commentMentionRepository;
    @Autowired
    private CommentReportRepository commentReportRepository;

    @Test
    @DisplayName("모든 연관 데이터가 달린 다이어리도 삭제되고 자식 행이 남지 않는다")
    void deleteFullyLoadedDiary() throws Exception {
        UserEntity owner = userRepository.save(TestUtil.makeUserEntity());
        UserEntity other = userRepository.save(TestUtil.makeDifferentUserEntity(owner));

        MusicMetadataEntity metadata = musicMetadataRepository.save(
                new MusicMetadataEntity(MusicMetadataSourceType.ITUNES, "track-1", ARTIST_ID, GENRE));
        DiaryEntity diary = TestUtil.makeDiaryEntity(owner);
        diary.assignMusicMetadata(metadata);
        diary = diaryRepository.save(diary);
        Long diaryId = diary.getId();

        userGenrePreferenceRepository.save(new UserGenrePreferenceEntity(owner, GENRE, 10));
        userArtistPreferenceRepository.save(new UserArtistPreferenceEntity(owner, ARTIST_ID, 10));
        diaryOrderLowService.saveOrder(owner, List.of(diaryId));

        diaryLikeRepository.save(new DiaryLikeEntity(other, diary));
        diaryStoreRepository.save(new DiaryStoreEntity(other, DiaryStoreInfo.from(diary, other)));
        diaryMemoRepository.save(new DiaryMemoEntity("memo", diary));
        diaryReportRepository.save(new DiaryReportEntity("신고", diary.getContent(), diary, other));

        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, other, "댓글"));
        CommentEntity reply = commentRepository.save(TestUtil.makeReplyEntity(diary, owner, parent, "답글"));
        commentLikeRepository.save(new CommentLikeEntity(owner, parent));
        commentMentionRepository.save(new CommentMentionEntity(parent, MentionTargetType.USER, owner.getId(), 1));
        commentReportRepository.save(new CommentReportEntity("신고", parent.getContent(), parent, owner));

        em.flush();
        em.clear();

        TestSecurityContextHolderInjection.inject(owner.getId(), owner.getRoleType());
        mockMvc.perform(delete("/api/diaries/{diaryId}", diaryId)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isNoContent());

        em.flush();
        em.clear();

        assertSoftly(softly -> {
            softly.assertThat(diaryRepository.existsById(diaryId)).isFalse();
            softly.assertThat(count("DiaryLikeEntity", "diary.id", diaryId)).isZero();
            softly.assertThat(count("DiaryMemoEntity", "diary.id", diaryId)).isZero();
            softly.assertThat(count("DiaryReportEntity", "diary.id", diaryId)).isZero();
            softly.assertThat(count("CommentEntity", "diary.id", diaryId)).isZero();
            softly.assertThat(count("CommentLikeEntity", "comment.id", parent.getId())).isZero();
            softly.assertThat(count("CommentMentionEntity", "comment.id", parent.getId())).isZero();
            softly.assertThat(count("CommentReportEntity", "comment.id", parent.getId())).isZero();
            softly.assertThat(diaryOrderRepository.findByUserId(owner.getId()).orElseThrow().getOrderList())
                    .doesNotContain(diaryId);
            softly.assertThat(diaryStoreRepository.existsByUserAndDiaryId(other, diaryId)).isTrue();
            softly.assertThat(userGenrePreferenceRepository.findByUser_IdAndGenreNameRaw(owner.getId(), GENRE)
                    .map(UserGenrePreferenceEntity::getScore).orElse(0)).isLessThan(10);
        });
    }

    private long count(String entity, String path, Long id) {
        return em.createQuery("select count(e) from " + entity + " e where e." + path + " = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
