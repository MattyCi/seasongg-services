package com.sgg.users;

import com.sgg.common.SggException;
import com.sgg.users.model.UserDto;
import com.sgg.users.security.PasswordEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class DefaultUserService implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    private static final String USERNAME_ALREADY_EXISTS_ERROR_TEXT = "The username provided is already in use.";

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

        UserDao userDao = UserDao.builder()
                .username(userRegistrationRequest.getUsername())
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .build();

        userRepository.save(userDao);

        return userMapper.userToUserDto(userDao);
    }

}
