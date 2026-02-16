package com.exam.library_management.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestControllerTest {

    @Test
    void userHello_shouldReturnExpectedMessage() {
        TestController controller = new TestController();
        assertEquals("Hello USER – JWT is valid", controller.userHello());
    }

    @Test
    void adminHello_shouldReturnExpectedMessage() {
        TestController controller = new TestController();
        assertEquals("Hello ADMIN – role check passed", controller.adminHello());
    }
}
