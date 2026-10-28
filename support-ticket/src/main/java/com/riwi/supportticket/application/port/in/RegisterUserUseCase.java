package com.riwi.supportticket.application.port.in;

import com.riwi.supportticket.domain.model.user.User;

public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);
}
