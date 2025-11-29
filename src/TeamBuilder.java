import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TeamBuilder {
    private TeamFormationStrategy strategy;
    private final ExecutorService parallelExecutor;

    public TeamBuilder() {
        this.strategy = new BalancedTeamStrategy();
        this.parallelExecutor = Executors.newWorkStealingPool();
    }

    public TeamBuilder(TeamFormationStrategy strategy) {
        this.strategy = strategy;
        this.parallelExecutor = Executors.newWorkStealingPool();
    }

    // Existing sequential method
    public List<Team> formTeams(List<Participant> participants, int teamSize) {
        return strategy.formTeams(participants, teamSize);
    }

    // New parallel method
    public List<Team> formTeamsParallel(List<Participant> participants, int teamSize) {
        if (participants.size() > 50) {
            System.out.println("Using parallel processing for large dataset (" + participants.size() + " participants)");
            return formTeamsParallelLarge(participants, teamSize);
        } else {
            System.out.println("Using optimized parallel processing");
            return formTeamsParallelOptimized(participants, teamSize);
        }
    }

    private List<Team> formTeamsParallelLarge(List<Participant> participants, int teamSize) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int batchSize = Math.max(10, participants.size() / availableProcessors);

        List<Callable<List<Team>>> tasks = new ArrayList<>();

        // Create multiple formation tasks
        for (int i = 0; i < availableProcessors; i++) {
            final int start = i * batchSize;
            final int end = Math.min(start + batchSize, participants.size());

            if (start >= participants.size()) break;

            tasks.add(() -> {
                List<Participant> batch = participants.subList(start, end);
                return strategy.formTeams(new ArrayList<>(batch), teamSize);
            });
        }

        try {
            // Execute all tasks in parallel
            List<Future<List<Team>>> futures = parallelExecutor.invokeAll(tasks);

            // Combine results
            List<Team> allTeams = new ArrayList<>();
            for (Future<List<Team>> future : futures) {
                allTeams.addAll(future.get());
            }

            return allTeams;

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel team formation failed", e);
        }
    }

    private List<Team> formTeamsParallelOptimized(List<Participant> participants, int teamSize) {
        // Use parallel streams for smaller datasets
        return strategy.formTeamsParallel(participants, teamSize);
    }

    // Rest of existing methods...
    public List<Team> formTeams(List<Participant> participants, int teamSize, String strategyName) {
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

    public void shutdown() {
        parallelExecutor.shutdown();
    }
}