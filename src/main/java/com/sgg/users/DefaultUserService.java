package com.sgg.users;

import com.sgg.common.exception.NotFoundException;
import com.sgg.common.exception.ClientException;
import com.sgg.users.model.UserDto;
import com.sgg.users.security.PasswordEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class DefaultUserService implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND_ERROR = "The given user could not be found.";
    private static final String USERNAME_ALREADY_EXISTS_ERROR_TEXT = "The username provided is already in use.";

    public UserDto registerUser(UserRegistrationRequest userRegistrationRequest)
            throws NotFoundException, ClientException {
        log.info("attempting to register user with username: {}", userRegistrationRequest.getUsername());

        checkForExistingUser(userRegistrationRequest);

        val userDao = UserDao.builder()
            .username(userRegistrationRequest.getUsername())
            .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
            .build();

        userRepository.save(userDao);

        return userMapper.userToUserDto(userDao);
    }

    /**
     * Move back to UserRegistrationValidator when github.com/micronaut-projects/micronaut-validation/issues/258
     * is resolved
     */
    private void checkForExistingUser(UserRegistrationRequest userRegistrationRequest) {
        if (userRepository.findByUsernameIgnoreCase(userRegistrationRequest.getUsername()).isPresent()) {
            log.info("user registration failed. username {} already exists",
                    userRegistrationRequest.getUsername());
            throw new ClientException(USERNAME_ALREADY_EXISTS_ERROR_TEXT);
        }
    }

    // TODO: change to "deleteAccount" and adjust logic accordingly (this is needed for test cleanup in the meantime)
    @Override
    public void deleteUser(String username) throws NotFoundException {
        userRepository.findByUsernameIgnoreCase(username)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> { throw new NotFoundException(USER_NOT_FOUND_ERROR); }
                );
    }

    public UserDto getUserById(Long id) {
        val userDao = userRepository.findById(id);

        if (userDao.isPresent()) {
            return userMapper.userToUserDto(userDao.get());
        } else {
            throw new NotFoundException(USER_NOT_FOUND_ERROR);
        }
    }

    public UserDto getUserByUsername(String username) {
        val userDao = userRepository.findByUsernameIgnoreCase(username);

        if (userDao.isPresent()) {
            return userMapper.userToUserDto(userDao.get());
        } else {
            throw new NotFoundException(USER_NOT_FOUND_ERROR);
        }
    }

}
