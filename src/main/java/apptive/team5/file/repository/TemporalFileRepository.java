package apptive.team5.file.repository;

import apptive.team5.file.domain.TemporalFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TemporalFileRepository extends JpaRepository<TemporalFile, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TemporalFile t where t.id in (:ids)")
    void deleteByIds(List<Long> ids);

    @Query("select t from TemporalFile t where t.createDateTime < :date")
    List<TemporalFile> findOrphanFiles(LocalDateTime date);
}
