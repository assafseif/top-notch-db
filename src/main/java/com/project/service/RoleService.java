package com.project.service;

import com.project.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

public interface RoleService {
    List<RoleDto> getAll();
    Page<RoleDto> getAllPaged(PageRequest pageRequest);
    RoleDto create(RoleDto dto);
    RoleDto update(Long id, RoleDto dto);
    void delete(Long id);
}
