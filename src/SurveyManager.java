import java.util.*;

public class SurveyManager {
    private static final String[] QUESTIONS = {
            "I enjoy taking the lead and guiding others during group activities.",
            "I prefer analyzing situations and coming up with strategic solutions.",
            "I work well with others and enjoy collaborative teamwork.",
            "I am calm under pressure and can help maintain team morale.",
            "I like making quick decisions and adapting in dynamic situations."
    };

    public SurveyResponse conductSurvey(String participantId, Scanner scanner) {
        System.out.println("\n=== PERSONALITY SURVEY ===");
        System.out.println("Please rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)");
        System.out.println("=====================================\n");

        int[] answers = new int[QUESTIONS.length];

        for (int i = 0; i < QUESTIONS.length; i++) {
            while (true) {
                try {
                    System.out.printf("Q%d: %s%n", i + 1, QUESTIONS[i]);
                    System.out.print("Your rating (1-5): ");

                    String input = scanner.nextLine().trim();
                    int rating = Integer.parseInt(input);

                    if (rating <= 1 || rating > 5) {
                        System.out.println("Please enter a number between 1 and 5, excluding 1");
                        continue;
                    }

                    answers[i] = rating;
                    break;

                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                }
            }
            System.out.println();
        }

        return new SurveyResponse(participantId, answers);
    }

    public Participant createParticipantFromSurvey(SurveyResponse response, String name,
                                                   String email, String game, Role role, int skillLevel) {
        String participantId = "SURVEY_" + System.currentTimeMillis();
        int personalityScore = response.getTotalScore();

        return new Participant(participantId, name, email, game, skillLevel, role, personalityScore);
    }
}