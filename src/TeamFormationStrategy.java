import java.util.List;

public interface TeamFormationStrategy {
    List<Team> formTeams(List<Participant> participants, int teamSize);
    String getStrategyName();
    String getStrategyDescription();
}