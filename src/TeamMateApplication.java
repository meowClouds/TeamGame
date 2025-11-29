import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TeamMateApplication {
    private final CSVDataHandler dataHandler;
    private final SurveyManager surveyManager;
    private final TeamBuilder teamBuilder;
    private List<Participant> currentParticipants;
    private List<Team> currentTeams;
    private final ExecutorService surveyExecutor;
    private final ExecutorService teamFormationExecutor;

    public TeamMateApplication() {
        this.dataHandler = new CSVDataHandler();
        this.surveyManager = new SurveyManager();
        this.teamBuilder = new TeamBuilder();
        this.currentParticipants = Collections.synchronizedList(new ArrayList<>());
        this.currentTeams = Collections.synchronizedList(new ArrayList<>());
        this.surveyExecutor = Executors.newFixedThreadPool(3);
        this.teamFormationExecutor = Executors.newFixedThreadPool(2);
    }

    // Add shutdown method
    public void shutdown() {
        surveyExecutor.shutdown();
        teamFormationExecutor.shutdown();
        dataHandler.shutdown();
        teamBuilder.shutdown();
        try {
            if (!surveyExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                surveyExecutor.shutdownNow();
            }
            if (!teamFormationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                teamFormationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            surveyExecutor.shutdownNow();
            teamFormationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println(" TeamMate: Intelligent Team Formation");
        System.out.println(" University Gaming Club System");
        System.out.println("=========================================\n");

        TeamMateApplication app = new TeamMateApplication();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));

        try {
            app.run();
        } finally {
            app.shutdown();
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Track ongoing async operations
        List<CompletableFuture<?>> ongoingOperations = Collections.synchronizedList(new ArrayList<>());

        while (running) {
            displayMainMenu();
            System.out.print("Select an option (1-7): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                // Check if there are ongoing operations that might conflict
                if (!ongoingOperations.isEmpty()) {
                    boolean hasActiveOperations = ongoingOperations.stream()
                            .anyMatch(future -> !future.isDone());

                    if (hasActiveOperations && (choice == 2 || choice == 4)) {
                        System.out.println("Warning: There are ongoing operations. " +
                                "Please wait for them to complete or select another option.");
                        System.out.print("Continue anyway? (y/n): ");
                        String response = scanner.nextLine().trim().toLowerCase();
                        if (!response.equals("y")) {
                            continue;
                        }
                    }
                }

                // Clean up completed operations
                ongoingOperations.removeIf(CompletableFuture::isDone);

                switch (choice) {
                    case 1:
                        loadParticipantsFromCSV(scanner);
                        break;
                    case 2:
                        CompletableFuture<Void> surveyFuture = CompletableFuture.runAsync(() -> {
                            conductNewSurvey(scanner);
                        }).exceptionally(throwable -> {
                            System.out.println("Survey operation failed: " + throwable.getMessage());
                            return null;
                        });
                        ongoingOperations.add(surveyFuture);
                        break;
                    case 3:
                        viewCurrentData();
                        break;
                    case 4:
                        CompletableFuture<Void> teamFuture = CompletableFuture.runAsync(() -> {
                            formTeams(scanner);
                        }).exceptionally(throwable -> {
                            System.out.println("Team formation failed: " + throwable.getMessage());
                            return null;
                        });
                        ongoingOperations.add(teamFuture);
                        break;
                    case 5:
                        viewTeams();
                        break;
                    case 6:
                        saveResults(scanner);
                        break;
                    case 7:
                        // Wait for ongoing operations to complete before shutdown
                        if (!ongoingOperations.isEmpty()) {
                            System.out.println("Waiting for ongoing operations to complete...");
                            try {
                                CompletableFuture.allOf(
                                        ongoingOperations.toArray(new CompletableFuture[0])
                                ).get(30, TimeUnit.SECONDS);
                                System.out.println("All operations completed.");
                            } catch (TimeoutException e) {
                                System.out.println("Timeout waiting for operations. Forcing shutdown...");
                            } catch (Exception e) {
                                System.out.println("Error waiting for operations: " + e.getMessage());
                            }
                        }
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

        System.out.println("Loading participants asynchronously...");

        // Use async loading
        dataHandler.loadParticipantsAsync(filePath)
                .thenAccept(loadedParticipants -> {
                    synchronized (currentParticipants) {
                        currentParticipants.addAll(loadedParticipants);
                    }
                    System.out.printf("Loaded %d participants. Total participants: %d%n",
                            loadedParticipants.size(), currentParticipants.size());
                })
                .exceptionally(throwable -> {
                    System.out.println("Error loading participants: " + throwable.getCause().getMessage());
                    return null;
                });
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

        // Submit survey processing to executor
        Future<SurveyResponse> surveyFuture = surveyExecutor.submit(() -> {
            System.out.println("Processing survey in background thread: " + Thread.currentThread().getName());
            return surveyManager.conductSurvey(participantId, scanner);
        });

        try {
            // Get the survey result (this will block until complete)
            SurveyResponse response = surveyFuture.get(30, TimeUnit.SECONDS);

            // Process participant creation in background
            Role finalRole = role;
            CompletableFuture<Participant> participantFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("Creating participant in background thread: " + Thread.currentThread().getName());
                return surveyManager.createParticipantFromSurvey(
                        response, name, email, game, finalRole, skillLevel);
            }, surveyExecutor);

            participantFuture.thenAccept(participant -> {
                // This runs async when participant is created
                synchronized (currentParticipants) {
                    currentParticipants.add(participant);
                }
                System.out.printf("Survey completed! Added participant: %s%n", participant.getName());
                System.out.printf("Personality Type: %s%n", participant.getPersonalityType());

                // Async CSV save
                CompletableFuture.runAsync(() -> {
                    try {
                        String csvFilePath = "C:\\Users\\ADMIN\\IdeaProjects\\TeamGame\\src\\participants_sample.csv";
                        dataHandler.appendParticipant(participant, csvFilePath);
                        System.out.println("Participant saved to CSV successfully.");
                    } catch (DataSavingException e) {
                        System.out.println("Warning: Could not save participant to CSV: " + e.getMessage());
                    }
                }, surveyExecutor);
            });

        } catch (TimeoutException e) {
            System.out.println("Survey processing timed out. Please try again.");
            surveyFuture.cancel(true);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error processing survey: " + e.getMessage());
        }
    }

    private void viewCurrentData() {
        System.out.println("\n--- Current Data ---");

        // Create a thread-safe copy to avoid ConcurrentModificationException
        List<Participant> snapshot;
        synchronized (currentParticipants) {
            snapshot = new ArrayList<>(currentParticipants);
        }

        System.out.printf("Total Participants: %d%n", snapshot.size());

        if (snapshot.isEmpty()) {
            System.out.println("No participants loaded yet.");
            return;
        }

        System.out.println("\nParticipants Summary:");
        System.out.println("---------------------");
        snapshot.forEach(p ->
                System.out.printf("- %s: %s | %s | Skill: %d | %s%n",
                        p.getId(), p.getName(), p.getPreferredGame(),
                        p.getSkillLevel(), p.getPersonalityType())
        );

        // Show statistics using the snapshot
        System.out.println("\nStatistics:");
        System.out.println("-----------");

        // Personality distribution
        Map<PersonalityType, Long> personalityDist = new HashMap<>();
        for (Participant p : snapshot) {
            personalityDist.merge(p.getPersonalityType(), 1L, Long::sum);
        }
        System.out.println("Personality Distribution: " + personalityDist);

        // Role distribution
        Map<Role, Long> roleDist = new HashMap<>();
        for (Participant p : snapshot) {
            roleDist.merge(p.getPreferredRole(), 1L, Long::sum);
        }
        System.out.println("Role Distribution: " + roleDist);

        // Average skill
        double avgSkill = snapshot.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
        System.out.printf("Average Skill Level: %.2f%n", avgSkill);
    }

    private void formTeams(Scanner scanner) {
        // Create a thread-safe copy
        List<Participant> snapshot;
        synchronized (currentParticipants) {
            snapshot = new ArrayList<>(currentParticipants);
        }

        if (snapshot.isEmpty()) {
            System.out.println("No participants available. Please load or survey participants first.");
            return;
        }

        System.out.println("\n--- Form Teams ---");
        System.out.print("Enter team size: ");
        int teamSize = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Forming teams with parallel processing...");

        // Use CompletableFuture for async team formation
        CompletableFuture<List<Team>> teamsFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Team formation running in thread: " + Thread.currentThread().getName());
            try {
                return teamBuilder.formTeamsParallel(snapshot, teamSize);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, teamFormationExecutor);

        // Show progress while processing
        System.out.print("Processing");
        CompletableFuture<Void> progressFuture = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(200);
                    System.out.print(".");
                }
                System.out.println();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Combine both and handle result
        teamsFuture.thenCombine(progressFuture, (teams, unused) -> {
            synchronized (currentTeams) {
                currentTeams.clear();
                currentTeams.addAll(teams);
            }
            return teams;
        }).thenAccept(teams -> {
            // This runs when team formation is complete
            System.out.printf("Successfully formed %d teams!%n", teams.size());

            // Calculate statistics in parallel
            CompletableFuture<Long> balancedTeamsFuture = CompletableFuture.supplyAsync(() ->
                    teams.stream().filter(Team::isBalanced).count(), teamFormationExecutor);

            CompletableFuture<Double> avgBalanceFuture = CompletableFuture.supplyAsync(() ->
                    teams.stream().mapToDouble(Team::getBalanceScore).average().orElse(0.0), teamFormationExecutor);

            // Combine results
            CompletableFuture.allOf(balancedTeamsFuture, avgBalanceFuture)
                    .thenRun(() -> {
                        try {
                            long balancedTeams = balancedTeamsFuture.get();
                            double avgBalanceScore = avgBalanceFuture.get();

                            System.out.printf("Balanced teams: %d/%d (%.1f%%)%n",
                                    balancedTeams, teams.size(),
                                    (balancedTeams * 100.0 / teams.size()));
                            System.out.printf("Average balance score: %.2f%n", avgBalanceScore);
                        } catch (InterruptedException | ExecutionException e) {
                            System.out.println("Error calculating statistics: " + e.getMessage());
                        }
                    });
        }).exceptionally(throwable -> {
            System.out.println("Team formation failed: " + throwable.getCause().getMessage());
            return null;
        });
    }

    private void viewTeams() {
        // Create a thread-safe copy
        List<Team> snapshot;
        synchronized (currentTeams) {
            snapshot = new ArrayList<>(currentTeams);
        }

        if (snapshot.isEmpty()) {
            System.out.println("No teams formed yet. Use option 4 to form teams.");
            return;
        }

        System.out.println("\n--- Formed Teams ---");
        System.out.printf("Total Teams: %d%n", snapshot.size());

        for (Team team : snapshot) {
            System.out.println(team.getDetailedInfo());
            System.out.println();
        }
    }

    private void saveResults(Scanner scanner) {
        // Create a thread-safe copy
        List<Team> snapshot;
        synchronized (currentTeams) {
            snapshot = new ArrayList<>(currentTeams);
        }

        if (snapshot.isEmpty()) {
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
            dataHandler.saveTeams(snapshot, teamsFile);
            System.out.println("Results saved successfully!");
            System.out.println("Teams data: " + teamsFile);

        } catch (DataSavingException e) {
            System.out.println("Error saving results: " + e.getMessage());
        }
    }
}