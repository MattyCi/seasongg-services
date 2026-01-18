package com.sgg.seasons.model;

public enum SeasonStatus {
    ACTIVE("ACTIVE"), INACTIVE("INACTIVE");

    private final String seasonStatus;

    SeasonStatus(final String seasonStatus) {
        this.seasonStatus = seasonStatus;
    }

    @Override
    public String toString() {
        return seasonStatus;
    }
}