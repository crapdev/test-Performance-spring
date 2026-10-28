package com.riwi.supportticket.application.port.in;

import com.riwi.supportticket.domain.model.user.User;

// GET y PUT /api/users/me
public interface UpdateProfileUseCase {

    User getProfile(Long userId);

    User updateProfile(Long userId, UpdateProfileCommand command);
}
