package com.sgg.users;

import com.sgg.common.exception.SggException;
import com.sgg.users.model.UserDto;

public interface UserService {

    UserDto registerUser(UserRegistrationRequest userRegistrationRequest) throws SggException;

    void deleteUser(String username) throws SggException;

    UserDto getUserById(Long id);

    UserDto getUserByUsername(String username);

}
