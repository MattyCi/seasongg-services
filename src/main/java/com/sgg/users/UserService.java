package com.sgg.users;

import com.sgg.common.SggException;
import com.sgg.users.model.UserDto;

public interface UserService {

    UserDto registerUser(UserRegistrationRequest userRegistrationRequest) throws SggException;

}
