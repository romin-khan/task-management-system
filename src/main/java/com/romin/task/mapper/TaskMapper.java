package com.romin.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.user.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    @Mapping(target = "assignedBy", source = "assignedBy.id")
    @Mapping(target = "assignedTo", source = "assignedTo.id")
    TaskResponseDto toResponseDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completionDate", ignore = true)
    @Mapping(target = "assignedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    Task toEntity(TaskRequestDto request, User assignedBy, User assignedTo);
}
