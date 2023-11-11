package com.sgg.users;

import com.sgg.common.SggException;
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

    public UserDto registerUser(@ValidUserRegistration UserRegistrationRequest userRegistrationRequest)
            throws SggException {

        log.info("attempting to register user with username: {}", userRegistrationRequest.getUsername());

        val userDao = UserDao.builder()
                .username(userRegistrationRequest.getUsername())
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .build();

        userRepository.save(userDao);

        return userMapper.userToUserDto(userDao);
    }

    // TODO: change to "deleteAccount" and adjust logic accordingly (this is needed for test cleanup in the meantime)
    @Override
    public void deleteUser(String username) throws SggException {
        userRepository.findByUsernameIgnoreCase(username)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> { throw new SggException(USER_NOT_FOUND_ERROR); }
                );
    }

    public UserDto getUserById(Long id) {
        val userDao = userRepository.findById(id);

        if (userDao.isPresent()) {
            return userMapper.userToUserDto(userDao.get());
        } else {
            throw new SggException(USER_NOT_FOUND_ERROR);
        }
    }

    public UserDto getUserByUsername(String username) {
        val userDao = userRepository.findByUsernameIgnoreCase(username);

        if (userDao.isPresent()) {
            return userMapper.userToUserDto(userDao.get());
        } else {
            throw new SggException(USER_NOT_FOUND_ERROR);
        }
    }

}
