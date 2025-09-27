package com.netflix.productivity.workflow.mapper;

import com.netflix.productivity.workflow.dto.WorkflowDto;
import com.netflix.productivity.workflow.entity.Workflow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowMapper {
    WorkflowDto toDto(Workflow entity);
    Workflow toEntity(WorkflowDto dto);
}


