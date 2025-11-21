public enum PersonalityType {
    LEADER("Leader", 90, 100),
    BALANCED("Balanced", 70, 89),
    THINKER("Thinker", 50, 69);

    private final String displayName;
    private final int minScore;
    private final int maxScore;

    PersonalityType(String displayName, int minScore, int maxScore) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static PersonalityType classifyFromScore(int score) {
        int scaledScore = score * 4; // Scale 5-25 to 20-100

        for (PersonalityType type : values()) {
            if (scaledScore >= type.minScore && scaledScore <= type.maxScore) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid personality score: " + score);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}