package apptive.team5.alarmv2.service;

import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.alarmv2.dto.AlarmV2RecipientItem;
import apptive.team5.alarmv2.repository.AlarmV2RecipientQueryRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlarmV2RecipientService {
    private final AlarmV2RecipientQueryRepository recipientQueryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AlarmV2RecipientItem> getRecipients(UserSearchType type, String query, boolean includeExcluded, Pageable pageable) {
        return recipientQueryRepository.findRecipients(type, query, includeExcluded, pageable).map(AlarmV2RecipientItem::from);
    }

    @Transactional(readOnly = true)
    public long countSelectableRecipients(UserSearchType type, String query) {
        return recipientQueryRepository.countSelectableRecipients(type, query);
    }

    @Transactional(readOnly = true)
    public Set<Long> getSelectableRecipientIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        return userRepository.findAllById(new LinkedHashSet<>(ids)).stream()
                .filter(user -> !user.isLocked() && user.isAlarmEnabled())
                .map(UserEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public java.util.List<AlarmV2RecipientItem> getSelectedRecipients(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.List.of();
        Map<Long, UserEntity> usersById = userRepository.findAllById(new LinkedHashSet<>(ids)).stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));
        return ids.stream().distinct().map(usersById::get).filter(java.util.Objects::nonNull)
                .map(AlarmV2RecipientItem::from).filter(AlarmV2RecipientItem::selectable).toList();
    }
}
