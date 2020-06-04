package de.deutschebahn.bahnhoflive.util;

public class Token {
    private boolean available = true;
    
    public boolean take() {
        if (available) {
            available = false;
            return true;
        }
        return false;
    }

    public void enable() {
        available = true;
    }
}
