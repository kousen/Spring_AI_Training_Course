package com.oreilly.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ModerationDemoTest {
    @Autowired
    private ModerationDemo moderationDemo;

    @Test
    void demo() {
        moderationDemo.demo();
        assertNotNull(moderationDemo);
    }

}