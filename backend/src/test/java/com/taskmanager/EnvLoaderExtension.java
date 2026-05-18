package com.taskmanager;

import com.taskmanager.config.EnvLoader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EnvLoaderExtension implements BeforeAllCallback {
    private static volatile boolean loaded = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!loaded) {
            EnvLoader.load();
            loaded = true;
        }
    }
}
