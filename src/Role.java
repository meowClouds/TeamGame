public enum Role {
    STRATEGIST("Strategist"),
    ATTACKER("Attacker"),
    DEFENDER("Defender"),
    SUPPORTER("Supporter"),
    COORDINATOR("Coordinator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }
    }


    @Override
    public String toString() {
        return displayName;
    }
}