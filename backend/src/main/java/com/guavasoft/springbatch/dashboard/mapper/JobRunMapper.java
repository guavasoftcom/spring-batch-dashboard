package com.guavasoft.springbatch.dashboard.mapper;

import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface JobRunMapper {

    DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "formatTimestamp")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "formatTimestamp")
    JobRun toDto(JobRunRow jobRunRow);

    @Named("formatTimestamp")
    default String formatTimestamp(LocalDateTime value) {
        return value == null ? null : value.format(TIMESTAMP_FORMAT);
    }
}
