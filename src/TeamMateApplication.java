import java.util.*;

public class TeamMateApplication {
    private final CSVDataHandler dataHandler;
    private final SurveyManager surveyManager;
    private final TeamBuilder teamBuilder;
    private List<Participant> currentParticipants;
    private List<Team> currentTeams;

    public TeamMateApplication() {
        this.dataHandler = new CSVDataHandler();
        this.surveyManager = new SurveyManager();
        this.teamBuilder = new TeamBuilder();
        this.currentParticipants = new ArrayList<>();
        this.currentTeams = new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println(" TeamMate: Intelligent Team Formation");
        System.out.println(" University Gaming Club System");
        System.out.println("=========================================\n");

        TeamMateApplication app = new TeamMateApplication();
        app.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            displayMainMenu();
            System.out.print("Select an option (1-7): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        loadParticipantsFromCSV(scanner);
                        break;
                    case 2:
                        conductNewSurvey(scanner);
                        break;
                    case 3:
                        viewCurrentData();
                        break;
                    case 4:
                        formTeams(scanner);
                        break;
                    case 5:
                        viewTeams();
                        break;
                    case 6:
                        saveResults(scanner);
                        break;
                    case 7:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please select 1-7.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }

        System.out.println("Thank you for using TeamMate!");
    }

    private void displayMainMenu() {
        System.out.println("=== MAIN MENU ===");
        System.out.println("1. Load Participants from CSV");
        System.out.println("2. Conduct New Survey");
        System.out.println("3. View Current Data");
        System.out.println("4. Form Teams");
        System.out.println("5. View Teams");
        System.out.println("6. Save Results");
        System.out.println("7. Exit");
        System.out.println("=================");
    }

    private void loadParticipantsFromCSV(Scanner scanner) {
        System.out.println("\n--- Load Participants from CSV ---");
        System.out.print("Enter CSV file path (or press Enter for default 'participants_sample.csv'): ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            filePath = "C:\\Users\\ADMIN\\IdeaProjects\\TeamGame\\src\\participants_sample.csv";
        }

        try {
            List<Participant> loadedParticipants = dataHandler.loadParticipants(filePath);
            currentParticipants.addAll(loadedParticipants);
            System.out.printf("Loaded %d participants. Total participants: %d%n",
                    loadedParticipants.size(), currentParticipants.size());

        } catch (DataLoadingException e) {
            System.out.println("Error loading participants: " + e.getMessage());
        }
    }

    private void conductNewSurvey(Scanner scanner) {
        System.out.println("\n--- Conduct New Survey ---");

        System.out.print("Enter participant name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter preferred game: ");
        String game = scanner.nextLine().trim();

        System.out.println("Available roles: " + Arrays.toString(Role.values()));
        System.out.print("Enter preferred role: ");
        String roleStr = scanner.nextLine().trim();

        Role role;
        try {
            role = Role.fromString(roleStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role. Using SUPPORTER as default.");
            role = Role.SUPPORTER;
        }

        System.out.print("Enter skill level (1-10): ");
        int skillLevel = Integer.parseInt(scanner.nextLine().trim());

        String participantId = "SURVEY_" + System.currentTimeMillis();
        SurveyResponse response = surveyManager.conductSurvey(participantId, scanner);

        Participant participant = surveyManager.createParticipantFromSurvey(
                response, name, email, game, role, skillLevel);

        currentParticipants.add(participant);
        System.out.printf("Survey completed! Added participant: %s%n", participant.getName());
        System.out.printf("Personality Type: %s%n", participant.getPersonalityType());

        // Save to CSV file
        try {
            String csvFilePath = "C:\\Users\\ADMIN\\IdeaProjects\\TeamGame\\src\\participants_sample.csv";
            dataHandler.appendParticipant(participant, csvFilePath);
        } catch (DataSavingException e) {
            System.out.println("Warning: Could not save participant to CSV: " + e.getMessage());
            // Continue anyway - the participant is still in memory
        }
    }

    private void viewCurrentData() {
        System.out.println("\n--- Current Data ---");
        System.out.printf("Total Participants: %d%n", currentParticipants.size());

        if (currentParticipants.isEmpty()) {
            System.out.println("No participants loaded yet.");
            return;
        }

        System.out.println("\nParticipants Summary:");
        System.out.println("---------------------");
        currentParticipants.forEach(p ->
                System.out.printf("- %s: %s | %s | Skill: %d | %s%n",
                        p.getId(), p.getName(), p.getPreferredGame(),
                        p.getSkillLevel(), p.getPersonalityType())
        );

        // Show statistics
        System.out.println("\nStatistics:");
        System.out.println("-----------");

        // Personality distribution
        Map<PersonalityType, Long> personalityDist = new HashMap<>();
        for (Participant p : currentParticipants) {
            personalityDist.merge(p.getPersonalityType(), 1L, Long::sum);
        }
        System.out.println("Personality Distribution: " + personalityDist);

        // Role distribution
        Map<Role, Long> roleDist = new HashMap<>();
        for (Participant p : currentParticipants) {
            roleDist.merge(p.getPreferredRole(), 1L, Long::sum);
        }
        System.out.println("Role Distribution: " + roleDist);

        // Average skill
        double avgSkill = currentParticipants.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
        System.out.printf("Average Skill Level: %.2f%n", avgSkill);
    }

    private void formTeams(Scanner scanner) {
        if (currentParticipants.isEmpty()) {
            System.out.println("No participants available. Please load or survey participants first.");
            return;
        }

        System.out.println("\n--- Form Teams ---");
        System.out.print("Enter team size: ");
        int teamSize = Integer.parseInt(scanner.nextLine().trim());

        try {
            System.out.println("Forming teams...");
            currentTeams = teamBuilder.formTeams(currentParticipants, teamSize);

            System.out.printf("Successfully formed %d teams!%n", currentTeams.size());

            // Show team formation summary
            long balancedTeams = currentTeams.stream().filter(Team::isBalanced).count();
            double avgBalanceScore = currentTeams.stream()
                    .mapToDouble(Team::getBalanceScore)
                    .average()
                    .orElse(0.0);

            System.out.printf("Balanced teams: %d/%d (%.1f%%)%n",
                    balancedTeams, currentTeams.size(),
                    (balancedTeams * 100.0 / currentTeams.size()));
            System.out.printf("Average balance score: %.2f%n", avgBalanceScore);

        } catch (Exception e) {
            System.out.println("Team formation failed: " + e.getMessage());
        }
    }

    private void viewTeams() {
        if (currentTeams.isEmpty()) {
            System.out.println("No teams formed yet. Use option 4 to form teams.");
            return;
        }

        System.out.println("\n--- Formed Teams ---");
        System.out.printf("Total Teams: %d%n", currentTeams.size());

        for (Team team : currentTeams) {
            System.out.println(team.getDetailedInfo());
            System.out.println();
        }
    }

    private void saveResults(Scanner scanner) {
        if (currentTeams.isEmpty()) {
            System.out.println("No teams to save. Please form teams first.");
            return;
        }

        System.out.println("\n--- Save Results ---");
        System.out.print("Enter base filename (without extension): ");
        String baseName = scanner.nextLine().trim();

        if (baseName.isEmpty()) {
            baseName = "teams_output";
        }

        String teamsFile = "C:\\Users\\ADMIN\\IdeaProjects\\TeamGame\\src\\" + baseName + ".csv";

        try {
            dataHandler.saveTeams(currentTeams, teamsFile);
            System.out.println("Results saved successfully!");
            System.out.println("Teams data: " + teamsFile);

        } catch (DataSavingException e) {
            System.out.println("Error saving results: " + e.getMessage());
        }
    }
}