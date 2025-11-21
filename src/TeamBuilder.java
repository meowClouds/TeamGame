import java.util.List;

public class TeamBuilder {
    private TeamFormationStrategy strategy;

    public TeamBuilder() {
        this.strategy = new BalancedTeamStrategy();  // Default strategy
    }

    public TeamBuilder(TeamFormationStrategy strategy) {
        this.strategy = strategy;  // Dependency injection
    }

    // Method overloading (Compile-time polymorphism)
    public List<Team> formTeams(List<Participant> participants, int teamSize) {
        return strategy.formTeams(participants, teamSize);
    }

    public List<Team> formTeams(List<Participant> participants, int teamSize, String strategyName) {
        // Could implement different strategies based on name
        System.out.println("Using strategy: " + strategy.getStrategyName());
        return formTeams(participants, teamSize);
    }

    public void setStrategy(TeamFormationStrategy strategy) {
        this.strategy = strategy;
    }

    public String getCurrentStrategyInfo() {
        return String.format("%s: %s",
                strategy.getStrategyName(),
                strategy.getStrategyDescription());
    }

    // Static factory method
    public static TeamBuilder createDefaultBuilder() {
        return new TeamBuilder(new BalancedTeamStrategy());
    }
}