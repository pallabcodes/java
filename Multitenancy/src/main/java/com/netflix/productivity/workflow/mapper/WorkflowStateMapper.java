package com.netflix.productivity.workflow.mapper;

import com.netflix.productivity.workflow.dto.WorkflowStateDto;
import com.netflix.productivity.workflow.entity.WorkflowState;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowStateMapper {
    WorkflowStateDto toDto(WorkflowState entity);
}


