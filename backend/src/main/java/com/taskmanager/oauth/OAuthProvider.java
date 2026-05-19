package com.taskmanager.oauth;

public enum OAuthProvider {
    GOOGLE("google"),
    GITHUB("github");

    private final String registrationId;

    OAuthProvider(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public static OAuthProvider fromRegistrationId(String registrationId) {
        for (OAuthProvider provider : OAuthProvider.values()) {
            if (provider.registrationId.equals(registrationId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown OAuth provider: " + registrationId);
    }
}
