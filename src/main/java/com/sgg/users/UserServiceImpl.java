package com.sgg.users;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.OffsetDateTime;

@Singleton
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    UserMapper userMapper;

    @Inject
    UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto registerUser(UserRegistrationRequest userRegistrationRequest) {

        User user = User.builder()
                .username(userRegistrationRequest.getUsername())
                .password(userRegistrationRequest.getPassword())
                .build();

        userRepository.save(user);

        return userMapper.userToUserDto(user);
    }

}
