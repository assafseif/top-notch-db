package com.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDto {
    private Long id;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fullName;
    private String email;

    // expose role id and name only
    private Long roleId;
    private String roleName;

    private boolean active = true;
    private Date createdAt;
}

