package com.sgg.users;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.sql.Timestamp;

@Singleton
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Inject
    UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO registerUser(UserRegistrationRequest userRegistrationRequest) {

        User user = User.builder()
                .username(userRegistrationRequest.getUsername())
                .password(userRegistrationRequest.getPassword())
                .build();

        userRepository.save(user);

        // TODO: use mapstruct to return the DTO
        return UserDTO.builder()
                .username(user.getUsername())
                .registrationTime(new Timestamp(System.currentTimeMillis()))
                .build();
    }

}
