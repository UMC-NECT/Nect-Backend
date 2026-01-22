package com.nect.core.repository.team.process;
import com.nect.core.entity.team.process.Process;// FIXME: Entity import 누락으로 java.lang.Process를 잘못 참조하는 문제 수정
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<Process, Long> {
}

