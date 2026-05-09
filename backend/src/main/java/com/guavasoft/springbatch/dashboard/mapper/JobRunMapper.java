package com.guavasoft.springbatch.dashboard.mapper;

import com.guavasoft.springbatch.dashboard.config.TimestampFormatter;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TimestampFormatter.class)
public interface JobRunMapper {

    JobRun toDto(JobRunRow jobRunRow);
}
