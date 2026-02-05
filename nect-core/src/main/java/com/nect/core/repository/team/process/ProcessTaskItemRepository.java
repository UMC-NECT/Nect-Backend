package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessTaskItem;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessTaskItemRepository extends JpaRepository<ProcessTaskItem, Long> {

    Optional<ProcessTaskItem> findByIdAndProcessIdAndDeletedAtIsNull(Long id, Long processId);

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullOrderBySortOrderAsc(Long processId);

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullAndIdIn(Long processId, List<Long> ids);

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldNameOrderBySortOrderAsc(
            Long processId, RoleField roleField, String customRoleFieldName
    );

    List<ProcessTaskItem> findAllByProcessIdAndDeletedAtIsNullAndRoleFieldAndCustomRoleFieldNameAndIdIn(
            Long processId, RoleField roleField, String customRoleFieldName, List<Long> ids
    );

    @Query("""
        select ti
        from ProcessTaskItem ti
        where ti.process.id = :processId
          and ti.deletedAt is null
          and ti.roleField = :roleField
          and (
                (:customName is null and ti.customRoleFieldName is null)
             or (:customName is not null and ti.customRoleFieldName = :customName)
          )
        order by
          ti.sortOrder asc nulls last,
          ti.id asc
    """)
    List<ProcessTaskItem> findWeekMissionGroupItemsOrdered(
            @Param("processId") Long processId,
            @Param("roleField") RoleField roleField,
            @Param("customName") String customName
    );
}
