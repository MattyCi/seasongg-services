package com.sgg.users;

import com.sgg.users.model.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface UserMapper {
    UserDto userToUserDto(UserDao userDao);
    UserDao userDtoToUser(UserDto userDto);
}
