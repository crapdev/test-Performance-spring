package com.riwi.techniciandispatch.controller;

import com.riwi.techniciandispatch.dto.DispatchRequest;
import com.riwi.techniciandispatch.dto.DispatchResponse;
import com.riwi.techniciandispatch.service.DispatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/technician-dispatch")
public class TechnicianDispatchController {

    private final DispatchService dispatchService;

    public TechnicianDispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping
    public DispatchResponse dispatch(@Valid @RequestBody DispatchRequest request) {
        return dispatchService.dispatch(request);
    }
}
