package apptive.team5.user.service;

import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.repository.AlarmRepository;
import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.domain.CommentMentionEntity;
import apptive.team5.comment.domain.CommentStatus;
import apptive.team5.comment.domain.MentionTargetType;
import apptive.team5.comment.repository.CommentLikeRepository;
import apptive.team5.comment.repository.CommentMentionRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.domain.model.DiaryStoreInfo;
import apptive.team5.diary.repository.DiaryLikeRepository;
import apptive.team5.diary.repository.DiaryReportRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.diary.service.DiaryOrderLowService;
import apptive.team5.fcm.entity.DeviceToken;
import apptive.team5.fcm.repository.DeviceTokenRepository;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserExploreExposureEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.recommendation.repository.UserArtistPreferenceRepository;
import apptive.team5.recommendation.repository.UserExploreExposureRepository;
import apptive.team5.recommendation.repository.UserGenrePreferenceRepository;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.survey.domain.SurveyEntity;
import apptive.team5.survey.repository.SurveyRepository;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserBlockRepository;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Transactional
class UserWithdrawalIntegrationTest {

    @Autowired
    private UserService userService;
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
    private DiaryReportRepository diaryReportRepository;
    @Autowired
    private DiaryOrderLowService diaryOrderLowService;
    @Autowired
    private SubscribeRepository subscribeRepository;
    @Autowired
    private UserBlockRepository userBlockRepository;
    @Autowired
    private AlarmRepository alarmRepository;
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private UserGenrePreferenceRepository userGenrePreferenceRepository;
    @Autowired
    private UserArtistPreferenceRepository userArtistPreferenceRepository;
    @Autowired
    private UserExploreExposureRepository userExploreExposureRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private CommentMentionRepository commentMentionRepository;

    @Test
    @DisplayName("모든 연관 데이터가 달린 유저도 탈퇴되고 남의 데이터는 보존된다")
    void withdrawFullyLoadedUser() {
        UserEntity leaver = userRepository.save(TestUtil.makeUserEntity());
        UserEntity other = userRepository.save(TestUtil.makeDifferentUserEntity(leaver));
        Long leaverId = leaver.getId();

        DiaryEntity leaverDiary = diaryRepository.save(TestUtil.makeDiaryEntity(leaver));
        DiaryEntity otherDiary = diaryRepository.save(TestUtil.makeDiaryEntity(other));
        diaryOrderLowService.saveOrder(leaver, List.of(leaverDiary.getId()));

        diaryLikeRepository.save(new DiaryLikeEntity(leaver, otherDiary));
        diaryStoreRepository.save(new DiaryStoreEntity(leaver, DiaryStoreInfo.from(otherDiary, leaver)));
        diaryReportRepository.save(new DiaryReportEntity("신고", otherDiary.getContent(), otherDiary, leaver));
        subscribeRepository.save(new Subscribe(leaver, other));
        subscribeRepository.save(new Subscribe(other, leaver));
        userBlockRepository.save(new UserBlock(leaver, other));
        alarmRepository.save(new Alarm("알림", "/api/diaries/1", leaver, AlarmMessage.LIKE_ALARM));
        deviceTokenRepository.save(new DeviceToken(leaver, "device-token"));
        surveyRepository.save(new SurveyEntity("설문", leaver));

        userGenrePreferenceRepository.save(new UserGenrePreferenceEntity(leaver, "K-Pop", 10));
        userArtistPreferenceRepository.save(new UserArtistPreferenceEntity(leaver, "artist-1", 10));
        userExploreExposureRepository.save(new UserExploreExposureEntity(leaver, otherDiary.getId()));

        CommentEntity leaverComment = commentRepository.save(TestUtil.makeCommentEntity(otherDiary, leaver, "탈퇴자 댓글"));
        CommentEntity othersReply = commentRepository.save(TestUtil.makeReplyEntity(otherDiary, other, leaverComment, "남의 답글"));
        CommentEntity othersComment = commentRepository.save(TestUtil.makeCommentEntity(otherDiary, other, "남의 댓글"));
        commentRepository.save(TestUtil.makeCommentEntity(leaverDiary, other, "탈퇴자 다이어리의 남의 댓글"));
        commentLikeRepository.save(new CommentLikeEntity(leaver, othersComment));
        commentMentionRepository.save(new CommentMentionEntity(othersComment, MentionTargetType.USER, leaverId, 1));
        em.createQuery("update CommentEntity c set c.likeCount = 1 where c.id = :id")
                .setParameter("id", othersComment.getId())
                .executeUpdate();

        em.flush();
        em.clear();

        userService.deleteUser(leaverId);

        em.flush();
        em.clear();

        CommentEntity foundLeaverComment = commentRepository.findById(leaverComment.getId()).orElseThrow();
        CommentEntity foundOthersReply = commentRepository.findById(othersReply.getId()).orElseThrow();
        CommentEntity foundOthersComment = commentRepository.findById(othersComment.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(userRepository.existsById(leaverId)).isFalse();
            softly.assertThat(diaryRepository.existsById(leaverDiary.getId())).isFalse();
            softly.assertThat(diaryRepository.existsById(otherDiary.getId())).isTrue();
            softly.assertThat(userGenrePreferenceRepository.findByUser_IdAndGenreNameRaw(leaverId, "K-Pop")).isEmpty();
            softly.assertThat(userArtistPreferenceRepository.findByUser_IdAndSourceArtistId(leaverId, "artist-1")).isEmpty();
            softly.assertThat(count("UserExploreExposureEntity", "user.id", leaverId)).isZero();
            softly.assertThat(count("CommentEntity", "diary.id", leaverDiary.getId())).isZero();
            softly.assertThat(foundLeaverComment.getStatus()).isEqualTo(CommentStatus.DELETED);
            softly.assertThat(foundLeaverComment.getUser()).isNull();
            softly.assertThat(foundOthersReply.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            softly.assertThat(foundOthersComment.getLikeCount()).isZero();
            softly.assertThat(count("CommentMentionEntity", "targetId", leaverId)).isZero();
        });
    }

    private long count(String entity, String path, Long id) {
        return em.createQuery("select count(e) from " + entity + " e where e." + path + " = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
