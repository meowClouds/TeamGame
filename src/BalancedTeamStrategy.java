import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BalancedTeamStrategy implements TeamFormationStrategy {
    private static final int MAX_ATTEMPTS = 100;

    @Override
    public List<Team> formTeams(List<Participant> participants, int teamSize) {
        List<Team> bestTeams = null;
        double bestScore = -1;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            List<Team> currentTeams = attemptFormation(new ArrayList<>(participants), teamSize);
            double currentScore = calculateOverallBalanceScore(currentTeams);

            if (currentScore > bestScore) {
                bestScore = currentScore;
                bestTeams = currentTeams;
            }
        }

        return bestTeams != null ? bestTeams :
                attemptFormation(new ArrayList<>(participants), teamSize);
    }

    private List<Team> attemptFormation(List<Participant> participants, int teamSize) {
        Collections.shuffle(participants);

        List<Team> teams = new ArrayList<>();
        int teamCount = (int) Math.ceil((double) participants.size() / teamSize);

        for (int i = 0; i < teamCount; i++) {
            teams.add(new Team("T" + (i + 1)));
        }

        // Sort by personality to distribute leaders first
        participants.sort((p1, p2) -> {
            boolean p1Leader = p1.getPersonalityType() == PersonalityType.LEADER;
            boolean p2Leader = p2.getPersonalityType() == PersonalityType.LEADER;
            boolean p1Thinker = p1.getPersonalityType() == PersonalityType.THINKER;
            boolean p2Thinker = p2.getPersonalityType() == PersonalityType.THINKER;

            if (p1Leader && !p2Leader) return -1;
            if (!p1Leader && p2Leader) return 1;
            if (p1Thinker && !p2Thinker) return -1;
            if (!p1Thinker && p2Thinker) return 1;
            return 0;
        });

        // Round-robin distribution
        int teamIndex = 0;
        for (Participant participant : participants) {
            teams.get(teamIndex).addMember(participant);
            teamIndex = (teamIndex + 1) % teams.size();
        }

        return teams;
    }

    private double calculateOverallBalanceScore(List<Team> teams) {
        return teams.stream()
                .mapToDouble(Team::getBalanceScore)
                .average()
                .orElse(0.0);
    }

    @Override
    public String getStrategyName() {
        return "Balanced Team Strategy";
    }

    @Override
    public String getStrategyDescription() {
        return "Forms teams with balanced distribution of games, roles, and personality types";
    }
}