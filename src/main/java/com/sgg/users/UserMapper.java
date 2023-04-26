package com.sgg.users;

import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface UserMapper {
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
}
