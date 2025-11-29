import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class BalancedTeamStrategy implements TeamFormationStrategy {
    private static final int MAX_ATTEMPTS = 100;
    private static final int PARALLEL_THRESHOLD = 20;

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

    // New parallel implementation
    public List<Team> formTeamsParallel(List<Participant> participants, int teamSize) {
        System.out.println("Using parallel team formation with " +
                Runtime.getRuntime().availableProcessors() + " processors");

        return IntStream.range(0, MAX_ATTEMPTS)
                .parallel()
                .mapToObj(attempt -> {
                    List<Team> teams = attemptFormation(new ArrayList<>(participants), teamSize);
                    double score = calculateOverallBalanceScore(teams);
                    return new TeamAttempt(teams, score);
                })
                .max(Comparator.comparingDouble(TeamAttempt::getScore))
                .map(TeamAttempt::getTeams)
                .orElse(attemptFormation(new ArrayList<>(participants), teamSize));
    }

    private List<Team> attemptFormation(List<Participant> participants, int teamSize) {
        // Use thread-safe shuffling
        Collections.shuffle(participants, ThreadLocalRandom.current());

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
        return teams.parallelStream()
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

    // Helper class for parallel processing
    private static class TeamAttempt {
        private final List<Team> teams;
        private final double score;

        public TeamAttempt(List<Team> teams, double score) {
            this.teams = teams;
            this.score = score;
        }

        public List<Team> getTeams() { return teams; }
        public double getScore() { return score; }
    }
}