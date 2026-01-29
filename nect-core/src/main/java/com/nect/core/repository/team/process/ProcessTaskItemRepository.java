package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessTaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessTaskItemRepository extends JpaRepository<ProcessTaskItem, Long> {

    Optional<ProcessTaskItem> findByIdAndProcessIdAndDeletedAtIsNull(Long id, Long processId);

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(Long processId);

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullAndIdIn(Long processId, List<Long> ids);
}
