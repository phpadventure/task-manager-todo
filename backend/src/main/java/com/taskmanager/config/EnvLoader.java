package com.taskmanager.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class EnvLoader {
    public static void load() {
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(envFile));
                props.forEach((key, value) -> {
                    if (System.getProperty(key.toString()) == null && System.getenv(key.toString()) == null) {
                        System.setProperty(key.toString(), value.toString());
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load .env file: " + e.getMessage());
        }
    }
}
