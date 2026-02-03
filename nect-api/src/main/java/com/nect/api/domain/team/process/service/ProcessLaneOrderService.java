package com.nect.api.domain.team.process.service;

import com.nect.core.entity.team.process.ProcessLaneOrder;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.repository.team.process.ProcessLaneOrderRepository;
import com.nect.core.entity.team.process.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessLaneOrderService {
    private final ProcessLaneOrderRepository processLaneOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureLaneOrderRowsExistWriteTx(
            Long projectId,
            ProcessStatus status,
            String laneKey,
            List<Process> laneProcesses
    ) {
        if (laneProcesses == null || laneProcesses.isEmpty()) return;

        // 1) 이미 존재하는 lane order row 조회
        // - 보통 "deleted_at is null" 조건이 repo에 들어가 있어야 함
        List<ProcessLaneOrder> existingOrders =
                processLaneOrderRepository.findLaneOrders(projectId, laneKey, status);

        Set<Long> existingProcessIds = existingOrders.stream()
                .map(o -> o.getProcess().getId())
                .collect(Collectors.toSet());

        // 2) 현재 laneProcesses 중 order row가 없는 프로세스만 추림
        List<Process> missing = laneProcesses.stream()
                .filter(p -> p != null && p.getId() != null)
                .filter(p -> !existingProcessIds.contains(p.getId()))
                .toList();

        if (missing.isEmpty()) return;

        // 3) tail sort_order 부여 (현재 max 뒤부터)
        int nextSortOrder = existingOrders.stream()
                .map(ProcessLaneOrder::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        List<ProcessLaneOrder> toSave = new ArrayList<>();
        for (Process p : missing) {
            ProcessLaneOrder row = ProcessLaneOrder.builder()
                    .projectId(projectId) // 엔티티 구조에 따라 project 엔티티면 .project(...)로 바꿔줘
                    .process(p)
                    .laneKey(laneKey)
                    .status(status)
                    .sortOrder(nextSortOrder++)
                    .build();
            toSave.add(row);
        }

        processLaneOrderRepository.saveAll(toSave);
    }
}

