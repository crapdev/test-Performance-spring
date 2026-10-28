package com.riwi.supportticket.application.service;

import com.riwi.supportticket.application.port.in.UpdateProfileCommand;
import com.riwi.supportticket.application.port.in.UpdateProfileUseCase;
import com.riwi.supportticket.application.port.out.UserRepositoryPort;
import com.riwi.supportticket.domain.exception.user.UserNotFoundException;
import com.riwi.supportticket.domain.model.user.User;

import java.util.Optional;

public class UpdateProfileService implements UpdateProfileUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateProfileService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getProfile(Long userId) {
        Optional<User> found = userRepository.findById(userId);
        if (found.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        return found.get();
    }

    @Override
    public User updateProfile(Long userId, UpdateProfileCommand command) {
        User user = getProfile(userId);
        user.updateProfile(command.fullName(), command.sede(), command.area());
        return userRepository.save(user);
    }
}
