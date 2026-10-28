package com.riwi.supportticket.application.port.out;

import com.riwi.supportticket.domain.model.user.User;

public interface TokenPort {

    // JWT con sub = id, email y role
    String generate(User user);
}
