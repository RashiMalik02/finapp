package com.finapp.finapp.role.services;

import com.finapp.finapp.exceptions.BadRequestException;
import com.finapp.finapp.exceptions.NotFoundException;
import com.finapp.finapp.res.Response;
import com.finapp.finapp.role.entity.Role;
import com.finapp.finapp.role.repo.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo repo;

    @Override
    public Response<Role> createRole(Role roleRequest) {
        if(repo.findByName(roleRequest.getName()).isPresent()) {
            throw new BadRequestException("Role already exists");
        }

        Role savedRole = repo.save(roleRequest);

        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role saved successfully")
                .data(savedRole)
                .build();
    }

    @Override
    public Response<Role> updateRole(Role roleRequest) {
        Role role = repo.findById(roleRequest.getId())
                .orElseThrow(() ->new NotFoundException("role not found"));

        role.setName(roleRequest.getName());
        Role updatedRole = repo.save(role);
        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role saved successfully")
                .data(updatedRole)
                .build();
    }

    @Override
    public Response<List<Role>> getAllRoles() {
        List<Role> roles = repo.findAll();

        return Response.<List<Role>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roles)
                .build();
    }

    @Override
    public Response<?> deleteRole(long id) {
        if(!repo.existsById(id)) {
            throw new NotFoundException("role not found");
        }

        repo.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role deleted successfully")
                .build();
    }
}
