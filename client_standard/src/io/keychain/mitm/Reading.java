package io.keychain.mitm;

import java.time.LocalDateTime; 

public class Reading {
    public Reading(boolean direction, double accumulatedAmount){
        this.direction = direction;
        this.accumulatedAmount = accumulatedAmount;
        this.readingDateTime = LocalDateTime.now();
    }

    public LocalDateTime readingDateTime;
    public boolean direction;
    public double accumulatedAmount;
}
