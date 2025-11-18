// src/main/java/com/healthcare/security/ActorContext.java
package com.healthcare.security;

import java.util.Set;

public record ActorContext(
        Long accountId,
        Long userProfileId,       // null 가능
        Long trainerProfileId,    // null 가능
        Set<String> roles         // e.g. ["USER", "TRAINER", "ADMIN"]
) {
    public boolean isUser()    { return roles.contains("USER"); }
    public boolean isTrainer() { return roles.contains("TRAINER"); }
    public boolean isAdmin()   { return roles.contains("ADMIN"); }
}
