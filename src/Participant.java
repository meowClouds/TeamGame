public class Participant extends Person {
    private final String email;
    private final String preferredGame;
    private final int skillLevel;
    private final Role preferredRole;
    private final int personalityScore;
    private final PersonalityType personalityType;

    public Participant(String id, String name, String email, String preferredGame,
                       int skillLevel, Role preferredRole, int personalityScore) {
        super(id, name);  // Calling parent constructor
        this.email = email;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityType.classifyFromScore(personalityScore);

        validateParticipant();
    }

    @Override
    public String getDisplayInfo() {
        return String.format("%s: %s | %s | Skill: %d | %s",
                id, name, preferredGame, skillLevel, personalityType);
    }

    private void validateParticipant() {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (skillLevel < 1 || skillLevel > 10) {
            throw new IllegalArgumentException("Skill level must be between 1-10");
        }
    }

    // Business methods using polymorphism
    public boolean isHighSkill() {
        return skillLevel >= 8;
    }

    public boolean hasLeadershipPotential() {
        return personalityType == PersonalityType.LEADER;
    }

    // Getters remain the same...
    public String getEmail() { return email; }
    public String getPreferredGame() { return preferredGame; }
    public int getSkillLevel() { return skillLevel; }
    public Role getPreferredRole() { return preferredRole; }
    public int getPersonalityScore() { return personalityScore; }
    public PersonalityType getPersonalityType() { return personalityType; }
}