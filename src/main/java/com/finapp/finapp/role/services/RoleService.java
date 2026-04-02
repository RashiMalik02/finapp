package com.finapp.finapp.role.services;

import com.finapp.finapp.res.Response;
import com.finapp.finapp.role.entity.Role;

import java.util.List;

public interface RoleService {

    Response<Role> createRole(Role roleRequest);

    Response<Role> updateRole(Role roleRequest);

    Response<List<Role>> getAllRoles();

    Response<?> deleteRole(long id);
}
