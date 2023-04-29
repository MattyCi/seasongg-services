package com.sgg.users;

import com.sgg.common.SggException;
import com.sgg.users.security.PasswordEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    private static final String USERNAME_ALREADY_EXISTS_ERROR_TEXT = "The username provided is already in use.";

    @Inject
    UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto registerUser(UserRegistrationRequest userRegistrationRequest) throws SggException {

        log.info("attempting to register user with username: {}", userRegistrationRequest.getUsername());

        // TODO: move to custom validator
        userRepository.findByUsernameIgnoreCase(userRegistrationRequest.getUsername()).ifPresent(
                (user) -> {
                    log.info("user registration failed. username {} already exists",
                            user.getUsername());
                    throw new SggException(USERNAME_ALREADY_EXISTS_ERROR_TEXT);
                }
        );

        User user = User.builder()
                .username(userRegistrationRequest.getUsername())
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .build();

        userRepository.save(user);

        return userMapper.userToUserDto(user);
    }

}
