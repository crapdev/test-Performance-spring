package com.riwi.supportticket;

import org.springframework.boot.SpringApplication;

public class TestSupportTicketApplication {

    public static void main(String[] args) {
        SpringApplication.from(SupportTicketApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
