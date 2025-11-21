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

        for (PersonalityType type : values()) {
            if (score >= type.minScore && score <= type.maxScore) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid personality score: " + score);
    }

    @Override
    public String toString() {
        return displayName;
    }
}