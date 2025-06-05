import java.util.*;

public class UpgradeManager {
    private long speedBoostEnd = 0;
    private long invincibilityEnd = 0;
    private long doublePointsEnd = 0;
    private long ghostFreezeEnd = 0;
    private long extraLifeEnd = 0;

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
    public boolean extraLifeActivation() {
        return System.currentTimeMillis() < extraLifeEnd;
    }

    public void activateUpgrade(UpgradeType type) {
        long currentTime = System.currentTimeMillis();

        switch (type) {
            case SPEED_BOOST:
                speedBoostEnd = currentTime + 5000;
                break;
            case INVINCIBILITY:
                invincibilityEnd = currentTime + 5000;
                break;
            case DOUBLE_POINTS:
                doublePointsEnd = currentTime + 15000;
                break;
            case GHOST_FREEZE:
                ghostFreezeEnd = currentTime + 5000;
                break;
            case EXTRA_LIFE:
                extraLifeEnd = currentTime + 5000; //info purpouse
                break;
        }
    }

    public List<String> getActiveEffects() {
        List<String> effects = new ArrayList<>();
        if (isSpeedBoostActive()) effects.add("GHOST SPEED BOOST");
        if (isInvincibilityActive()) effects.add("INVINCIBILITY");
        if (isDoublePointsActive()) effects.add("DOUBLE POINTS");
        if (isGhostFreezeActive()) effects.add("GHOST FREEZE");
        if (extraLifeActivation()) effects.add("EXTRA LIFE");
        return effects;
    }
}
