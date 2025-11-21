import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Team extends Person implements Formattable {
    private final List<Participant> members;
    private final Map<String, Integer> gameDistribution;
    private final Map<Role, Integer> roleDistribution;
    private final Map<PersonalityType, Integer> personalityDistribution;

    public Team(String teamId) {
        super(teamId, "Team-" + teamId);  // Team name generated from ID
        this.members = new ArrayList<>();
        this.gameDistribution = new HashMap<>();
        this.roleDistribution = new HashMap<>();
        this.personalityDistribution = new HashMap<>();
    }

    // Implementing Formattable interface methods (Polymorphism)
    @Override
    public String toCSVFormat() {
        String memberString = members.stream()
                .map(p -> String.format("%s(%s)", p.getName(), p.getPreferredRole()))
                .reduce((a, b) -> a + ";" + b)
                .orElse("");

        return String.format("%s,%d,%.2f,%.1f,\"%s\"",
                id, getTeamSize(), getAverageSkill(), getBalanceScore(), memberString);
    }

    @Override
    public String toDisplayFormat() {
        return getDetailedInfo();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("teamId", id);
        map.put("teamSize", getTeamSize());
        map.put("averageSkill", getAverageSkill());
        map.put("balanceScore", getBalanceScore());
        map.put("members", new ArrayList<>(members));
        map.put("isBalanced", isBalanced());
        return map;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Team %s: %d members, Avg Skill: %.1f, Balance: %.1f%%",
                id, getTeamSize(), getAverageSkill(), getBalanceScore());
    }

    // Rest of the Team class methods remain the same...
    public boolean addMember(Participant participant) {
        if (members.contains(participant)) {
            return false;
        }
        members.add(participant);
        updateDistributions(participant);
        return true;
    }

    private void updateDistributions(Participant participant) {
        gameDistribution.merge(participant.getPreferredGame(), 1, Integer::sum);
        roleDistribution.merge(participant.getPreferredRole(), 1, Integer::sum);
        personalityDistribution.merge(participant.getPersonalityType(), 1, Integer::sum);
    }

    public double getAverageSkill() {
        return members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
    }

    public int getTeamSize() {
        return members.size();
    }

    public double getBalanceScore() {
        double score = 0.0;
        boolean hasGameVariety = gameDistribution.values().stream().allMatch(count -> count <= 2);
        boolean hasRoleDiversity = roleDistribution.size() >= Math.min(3, members.size());

        int leaders = personalityDistribution.getOrDefault(PersonalityType.LEADER, 0);
        int thinkers = personalityDistribution.getOrDefault(PersonalityType.THINKER, 0);
        boolean hasGoodPersonalityMix = leaders >= 1 && thinkers >= 1 && thinkers <= 2;

        score += hasGameVariety ? 25 : 0;
        score += hasRoleDiversity ? 25 : 0;
        score += hasGoodPersonalityMix ? 50 : 25;

        return score;
    }

    public boolean isBalanced() {
        return getBalanceScore() >= 80.0;
    }

    public List<String> getBalanceIssues() {
        List<String> issues = new ArrayList<>();
        if (gameDistribution.values().stream().anyMatch(count -> count > 2)) {
            issues.add("Too many players from same game: " + gameDistribution);
        }
        if (roleDistribution.size() < Math.min(3, members.size())) {
            issues.add("Insufficient role diversity: " + roleDistribution);
        }

        int leaders = personalityDistribution.getOrDefault(PersonalityType.LEADER, 0);
        int thinkers = personalityDistribution.getOrDefault(PersonalityType.THINKER, 0);
        if (leaders < 1 || thinkers < 1 || thinkers > 2) {
            issues.add("Poor personality mix: " + personalityDistribution);
        }
        return issues;
    }

    // Getters
    public List<Participant> getMembers() { return new ArrayList<>(members); }
    public Map<String, Integer> getGameDistribution() { return new HashMap<>(gameDistribution); }
    public Map<Role, Integer> getRoleDistribution() { return new HashMap<>(roleDistribution); }
    public Map<PersonalityType, Integer> getPersonalityDistribution() {
        return new HashMap<>(personalityDistribution);
    }

    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team ").append(id).append(":\n");
        sb.append("  Members: ").append(members.size()).append("\n");
        sb.append("  Average Skill: ").append(String.format("%.2f", getAverageSkill())).append("\n");
        sb.append("  Balance Score: ").append(String.format("%.1f", getBalanceScore())).append("\n");
        sb.append("  Games: ").append(gameDistribution).append("\n");
        sb.append("  Roles: ").append(roleDistribution).append("\n");
        sb.append("  Personalities: ").append(personalityDistribution).append("\n");

        List<String> issues = getBalanceIssues();
        if (!issues.isEmpty()) {
            sb.append("  Issues:\n");
            issues.forEach(issue -> sb.append("    - ").append(issue).append("\n"));
        }
        return sb.toString();
    }
}