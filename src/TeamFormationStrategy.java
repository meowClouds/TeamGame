import java.util.List;

public interface TeamFormationStrategy {
    List<Team> formTeams(List<Participant> participants, int teamSize);

    // New method for parallel formation
    default List<Team> formTeamsParallel(List<Participant> participants, int teamSize) {
        return formTeams(participants, teamSize); // Default to sequential
    }

    String getStrategyName();
    String getStrategyDescription();
}