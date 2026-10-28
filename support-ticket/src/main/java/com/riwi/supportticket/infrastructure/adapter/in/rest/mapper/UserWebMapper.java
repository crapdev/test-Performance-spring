package com.riwi.supportticket.infrastructure.adapter.in.rest.mapper;

import com.riwi.supportticket.application.port.in.RegisterUserCommand;
import com.riwi.supportticket.application.port.in.UpdateProfileCommand;
import com.riwi.supportticket.domain.model.user.User;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.RegisterRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.UpdateProfileRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserWebMapper {

    RegisterUserCommand toCommand(RegisterRequest request);

    UpdateProfileCommand toCommand(UpdateProfileRequest request);

    UserResponse toResponse(User user);
}
