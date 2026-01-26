package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.enums.ProcessStatus;

public interface StatusCountRow {
    ProcessStatus getStatus();
    long getCnt();
}
