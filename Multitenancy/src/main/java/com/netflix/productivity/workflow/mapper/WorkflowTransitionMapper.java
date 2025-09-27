package com.netflix.productivity.workflow.mapper;

import com.netflix.productivity.workflow.dto.WorkflowTransitionDto;
import com.netflix.productivity.workflow.entity.WorkflowTransition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowTransitionMapper {
    WorkflowTransitionDto toDto(WorkflowTransition entity);
}


