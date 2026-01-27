package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessTaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessTaskItemRepository extends JpaRepository<ProcessTaskItem, Long> {

    Optional<ProcessTaskItem> findByIdAndProcessId(Long id, Long processId);

    List<ProcessTaskItem> findAllByProcessIdOrderBySortOrderAsc(Long processId);

    List<ProcessTaskItem> findAllByProcessIdAndIdIn(Long processId, List<Long> ids);
}
