package com.taskmanager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(EnvLoaderExtension.class)
public abstract class TestConfig {
}
