package com.example.iamservice.controller;

import com.example.iamservice.base.RestApiV1;
import com.example.iamservice.base.RestData;
import com.example.iamservice.base.VsResponseUtil;
import com.example.iamservice.domain.entity.Role;
import com.example.iamservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 16:48
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestApiV1
@RequiredArgsConstructor
public class RoleController {
    private final RoleRepository roleRepository;

    @GetMapping("/roles")
    public ResponseEntity<RestData<List<Role>>> findAll() {
        return VsResponseUtil.success(roleRepository.findAll(), "Fetch all role for test authorization", OK);
    }

    @PostMapping("/roles")
    public ResponseEntity<RestData<Role>> createRole(@RequestBody Role role) {
        return VsResponseUtil.success(roleRepository.save(role), "Create role for test authorization", CREATED);
    }
}
