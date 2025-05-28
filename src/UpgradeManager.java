import java.util.*;

public class UpgradeManager {
    private long speedBoostEnd = 0;
    private long invincibilityEnd = 0;
    private long doublePointsEnd = 0;
    private long ghostFreezeEnd = 0;

    public boolean isSpeedBoostActive() {
        return System.currentTimeMillis() < speedBoostEnd;
    }

    public boolean isInvincibilityActive() {
        return System.currentTimeMillis() < invincibilityEnd;
    }

    public boolean isDoublePointsActive() {
        return System.currentTimeMillis() < doublePointsEnd;
    }

    public boolean isGhostFreezeActive() {
        return System.currentTimeMillis() < ghostFreezeEnd;
    }

    public void activateUpgrade(UpgradeType type) {
        long currentTime = System.currentTimeMillis();

        switch (type) {
            case SPEED_BOOST:
                speedBoostEnd = currentTime + 10000; // 10 sekund
                break;
            case INVINCIBILITY:
                // Dłuższa nieśmiertelność jeśli zebrana z power-up, krótsza po utracie życia
                long duration = (invincibilityEnd > currentTime) ? 8000 : 2000;
                invincibilityEnd = currentTime + duration;
                break;
            case DOUBLE_POINTS:
                doublePointsEnd = currentTime + 15000; // 15 sekund
                break;
            case GHOST_FREEZE:
                ghostFreezeEnd = currentTime + 5000; // 5 sekund
                break;
            case TELEPORT:
                // Teleportacja jest natychmiastowa
                break;
        }
    }

    public List<String> getActiveEffects() {
        List<String> effects = new ArrayList<>();
        if (isSpeedBoostActive()) effects.add("SPEED BOOST");
        if (isInvincibilityActive()) effects.add("INVINCIBILITY");
        if (isDoublePointsActive()) effects.add("DOUBLE POINTS");
        if (isGhostFreezeActive()) effects.add("GHOST FREEZE");
        return effects;
    }
}
