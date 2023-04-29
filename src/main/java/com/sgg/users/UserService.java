package com.sgg.users;

import com.sgg.common.SggException;

public interface UserService {

    UserDto registerUser(UserRegistrationRequest userRegistrationRequest) throws SggException;

}
