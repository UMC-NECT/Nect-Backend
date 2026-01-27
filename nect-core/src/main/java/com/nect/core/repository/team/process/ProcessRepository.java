package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {

    // 소속 검증 + 소프트 delete 제외
    Optional<Process> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

}

