package com.project.service;

import com.project.dto.AppUserDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AppUserService {
    AppUserDto create(AppUserDto userDto);
    AppUserDto update(Long id, AppUserDto userDto);
    AppUserDto partialUpdate(Long id, AppUserDto userDto);
    AppUserDto updatePassword(Long id, String password);
    void delete(Long id);
    List<AppUserDto> getAll();
    Page<AppUserDto> getAllPaged(int page, int size);
    AppUserDto getById(Long id);
}

