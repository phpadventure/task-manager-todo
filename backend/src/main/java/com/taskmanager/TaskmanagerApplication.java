package com.taskmanager;

import com.taskmanager.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskmanagerApplication {

    public static void main(String[] args) {
        EnvLoader.load();
        SpringApplication.run(TaskmanagerApplication.class, args);
    }

}
