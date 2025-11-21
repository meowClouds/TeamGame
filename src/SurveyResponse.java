import java.util.Arrays;

public class SurveyResponse {
    private final String participantId;
    private final int[] answers;
    private final int totalScore;

    public SurveyResponse(String participantId, int[] answers) {
        this.participantId = participantId;
        this.answers = Arrays.copyOf(answers, answers.length);

        // Validate answers
        for (int answer : answers) {
            if (answer < 1 || answer > 5) {
                throw new IllegalArgumentException("Answers must be between 1 and 5");
            }
        }

        this.totalScore = (Arrays.stream(answers).sum())*4;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int[] getAnswers() {
        return Arrays.copyOf(answers, answers.length);
    }

    public String getParticipantId() {
        return participantId;
    }

    @Override
    public String toString() {
        return String.format("SurveyResponse{participantId='%s', totalScore=%d}",
                participantId, totalScore);
    }
}