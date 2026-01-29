package com.sgg.users;

import com.sgg.common.exception.NotFoundException;
import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.SggException;
import com.sgg.users.model.UserDto;
import com.sgg.users.security.PasswordEncoder;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class DefaultUserService implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityService securityService;
    Validator validator;

    private static final String USER_NOT_FOUND_ERROR = "The given user could not be found.";
    private static final String USERNAME_ALREADY_EXISTS_ERROR_TEXT = "The username provided is already in use.";
    private static final String ERR_MUST_BE_AUTHENTICATED = "You must be logged in to perform that action.";

    public UserDto registerUser(UserRegistrationRequest userRegistrationRequest)
            throws NotFoundException, ClientException {
        log.info("attempting to register user with username: {}", userRegistrationRequest.getUsername());
        val violations = validator.validate(userRegistrationRequest);
        if (!violations.isEmpty()) {
            throw new ClientException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" "))
            );
        }
        if (!userRegistrationRequest.getPassword().equals(userRegistrationRequest.getPasswordVerify())) {
            throw new ClientException("The provided passwords do not match.");
        }
        checkForExistingUser(userRegistrationRequest); 
        val userDao = UserDao.builder()
            .username(userRegistrationRequest.getUsername().trim())
            .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
            .registrationTime(OffsetDateTime.now(ZoneId.of("America/New_York")))
            .build();

        userRepository.save(userDao);

        return userMapper.userToUserDto(userDao);
    }

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

    public UserDto getCurrentUser() {
        return securityService.getAuthentication()
                .orElseThrow(() -> new SggException(ERR_MUST_BE_AUTHENTICATED))
                .getAttributes()
                .entrySet().stream()
                .filter((e) -> "userId".equals(e.getKey()))
                .map((e) -> getUserById(Long.valueOf(e.getValue().toString())))
                .findFirst()
                .orElseThrow(() -> new SggException("Unexpected error occurred trying to retrieve current user."));
    }
}
