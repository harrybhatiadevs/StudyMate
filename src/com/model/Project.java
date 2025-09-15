package com.model;

import java.time.Instant;

public class Project {
    private final String name;
    private final Instant createdAt = Instant.now();

    public Project(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
