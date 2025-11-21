import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVDataHandler {
    private static final String[] EXPECTED_HEADERS = {
            "ID", "Name", "Email", "PreferredGame", "SkillLevel",
            "PreferredRole", "PersonalityScore", "PersonalityType"
    };

    public List<Participant> loadParticipants(String filePath) throws DataLoadingException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DataLoadingException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new DataLoadingException("File does not exist: " + filePath);
        }

        List<Participant> participants = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    validateHeader(line);
                    isFirstLine = false;
                    continue;
                }

                try {
                    Participant participant = parseParticipantLine(line, lineNumber);
                    participants.add(participant);
                } catch (InvalidDataException e) {
                    System.err.printf("Warning: Skipping line %d - %s%n", lineNumber, e.getMessage());
                }
            }

            if (participants.isEmpty()) {
                throw new DataLoadingException("No valid participant data found in file");
            }

            System.out.printf("Successfully loaded %d participants from %s%n",
                    participants.size(), filePath);

        } catch (IOException e) {
            throw new DataLoadingException("Error reading file: " + e.getMessage(), e);
        }

        return participants;
    }

    private void validateHeader(String headerLine) throws DataLoadingException {
        String[] headers = headerLine.split(",");

        if (headers.length != EXPECTED_HEADERS.length) {
            throw new DataLoadingException(
                    String.format("Invalid header format. Expected %d columns, found %d",
                            EXPECTED_HEADERS.length, headers.length)
            );
        }
    }

    private Participant parseParticipantLine(String line, int lineNumber) throws InvalidDataException {
        String[] fields = line.split(",");

        if (fields.length != EXPECTED_HEADERS.length) {
            throw new InvalidDataException(
                    String.format("Invalid number of fields. Expected %d, found %d",
                            EXPECTED_HEADERS.length, fields.length)
            );
        }

        try {
            String id = validateAndTrim(fields[0], "ID", lineNumber);
            String name = validateAndTrim(fields[1], "Name", lineNumber);
            String email = validateAndTrim(fields[2], "Email", lineNumber);
            String preferredGame = validateAndTrim(fields[3], "PreferredGame", lineNumber);
            int skillLevel = validateSkillLevel(fields[4], lineNumber);
            Role preferredRole = validateRole(fields[5], lineNumber);
            int personalityScore = validatePersonalityScore(fields[6], lineNumber);

            return new Participant(id, name, email, preferredGame, skillLevel,
                    preferredRole, personalityScore);

        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid numeric format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Data validation failed: " + e.getMessage());
        }
    }

    private String validateAndTrim(String value, String fieldName, int lineNumber) throws InvalidDataException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidDataException(
                    String.format("Field '%s' cannot be empty at line %d", fieldName, lineNumber)
            );
        }
        return value.trim();
    }

    private int validateSkillLevel(String skillStr, int lineNumber) throws InvalidDataException {
        try {
            int skill = Integer.parseInt(skillStr.trim());
            if (skill < 1 || skill > 10) {
                throw new InvalidDataException(
                        String.format("Skill level must be between 1-10 at line %d", lineNumber)
                );
            }
            return skill;
        } catch (NumberFormatException e) {
            throw new InvalidDataException(
                    String.format("Invalid skill level format at line %d: %s", lineNumber, skillStr)
            );
        }
    }

    private Role validateRole(String roleStr, int lineNumber) throws InvalidDataException {
        try {
            return Role.fromString(roleStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(
                    String.format("Invalid role '%s' at line %d", roleStr, lineNumber)
            );
        }
    }

    private int validatePersonalityScore(String scoreStr, int lineNumber) throws InvalidDataException {
        try {
            int score = Integer.parseInt(scoreStr.trim());
            if (score < 0 || score > 100) {
                throw new InvalidDataException(
                        String.format("Personality score must be between 0-100 at line %d", lineNumber)
                );
            }
            return score;
        } catch (NumberFormatException e) {
            throw new InvalidDataException(
                    String.format("Invalid personality score format at line %d: %s", lineNumber, scoreStr)
            );
        }
    }

    /**
     * Appends a new participant to the CSV file
     */
    public void appendParticipant(Participant participant, String filePath) throws DataSavingException {
        if (participant == null) {
            throw new DataSavingException("Participant cannot be null");
        }

        Path path = Paths.get(filePath);
        boolean fileExists = Files.exists(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND)) {

            // Write header if file doesn't exist or is empty
            if (!fileExists || Files.size(path) == 0) {
                writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
                writer.newLine();
            }

            // Format participant data for CSV
            String participantLine = String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                    participant.getId(),
                    participant.getName(),
                    participant.getEmail(),
                    participant.getPreferredGame(),
                    participant.getSkillLevel(),
                    participant.getPreferredRole(),
                    participant.getPersonalityScore(),
                    participant.getPersonalityType());

            writer.write(participantLine);
            writer.newLine();

            System.out.printf("Successfully appended participant %s to %s%n",
                    participant.getName(), filePath);

        } catch (IOException e) {
            throw new DataSavingException("Error appending participant to file: " + e.getMessage(), e);
        }
    }

    public void saveTeams(List<Team> teams, String filePath) throws DataSavingException {
        if (teams == null || teams.isEmpty()) {
            throw new DataSavingException("No teams to save");
        }

        Path path = Paths.get(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("TeamID,MemberCount,AverageSkill,BalanceScore,Members");
            writer.newLine();

            // Using polymorphism - Team implements Formattable
            for (Team team : teams) {
                writer.write(team.toCSVFormat());  // Polymorphic call
                writer.newLine();
            }

            System.out.printf("Successfully saved %d teams to %s%n", teams.size(), filePath);

        } catch (IOException e) {
            throw new DataSavingException("Error writing to file: " + e.getMessage(), e);
        }
    }

}