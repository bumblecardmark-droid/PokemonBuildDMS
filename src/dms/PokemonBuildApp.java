package dms;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CancellationException;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: PokemonBuildApp
 * Coordinates the Phase 1 console menu and the planned validation, repository,
 * file, and calculation layers. Every action gives feedback and returns to the
 * menu. Pending manual entries can be canceled without changing stored data.
 */
public class PokemonBuildApp {
    private final PokemonBuildRepository repository;
    private final BuildFileService fileService;
    private final TeamReadinessCalculator calculator;
    private final Scanner scanner;
    private final BuildValidator validator = new BuildValidator();

    public PokemonBuildApp(PokemonBuildRepository repository, BuildFileService fileService,
                           TeamReadinessCalculator calculator, Scanner scanner) {
        this.repository = repository;
        this.fileService = fileService;
        this.calculator = calculator;
        this.scanner = scanner;
    }

    /**
     * run keeps the menu available until Exit is chosen or console input closes.
     * @return true for a requested exit, false if the input stream closes;
     * arguments: none
     */
    public boolean run() {
        System.out.println("Pokemon Build Management System - Phase 1");
        System.out.println("Enter :cancel at an action prompt to return to the menu.");
        while (true) {
            try {
                System.out.println(formatMainMenu());
                int choice = readMenuChoice();
                switch (choice) {
                    case 1: loadDataFlow(); break;
                    case 2: displayBuilds(repository.getAllBuilds()); break;
                    case 3: addBuildFlow(); break;
                    case 4: removeBuildFlow(); break;
                    case 5: updateBuildFlow(); break;
                    case 6: calculateScoreFlow(); break;
                    case 7: filterBuildsFlow(); break;
                    case 0:
                        System.out.println("Goodbye.");
                        return true;
                    default: System.out.println("Choose a listed menu option.");
                }
            } catch (CancellationException ex) {
                System.out.println("Action canceled. Stored data is unchanged.");
            } catch (NoSuchElementException ex) {
                System.out.println("Console input closed. Reopen the program to continue.");
                return false;
            } catch (IllegalArgumentException ex) {
                System.out.println("Input error: " + ex.getMessage());
            }
        }
    }

    /**
     * formatMainMenu lists every supported console operation and current counts.
     * @return formatted menu text; arguments: none
     */
    public String formatMainMenu() {
        return "\nMain Menu - " + repository.count() + " record(s), "
                + repository.getSelectedBuilds().size() + " selected\n"
                + "1. Load builds from a text file\n"
                + "2. Display all builds\n"
                + "3. Add a build manually\n"
                + "4. Remove a build\n"
                + "5. Update any build field\n"
                + "6. Calculate Team Readiness Score\n"
                + "7. Search or filter builds\n"
                + "0. Exit";
    }

    /**
     * readMenuChoice rejects blank, nonnumeric, and unlisted menu options.
     * @return a menu number from 0 through 7; arguments: none
     */
    public int readMenuChoice() {
        return readInteger("Choose an option (0-7): ", 0, 7);
    }

    /**
     * loadStartingData delegates file loading to the data-access layer.
     * @param filePath user-supplied location
     * @return number of new records imported
     * @throws IOException if the location or file contents cannot be read
     */
    public int loadStartingData(String filePath) throws IOException {
        return fileService.loadFromCsv(filePath, repository);
    }

    /**
     * loadDataFlow permits retries for blank, missing, improper, or unreadable files.
     * @return number of new records imported, including zero; arguments: none
     */
    public int loadDataFlow() {
        System.out.println("Enter data.txt or the full path to your text file.");
        while (true) {
            String path = readInput("Text file location (:cancel to return): ");
            if (path.isEmpty()) {
                System.out.println("File location cannot be blank. Try again.");
                continue;
            }
            try {
                int added = loadStartingData(path);
                System.out.println("Import complete: " + added + " added, "
                        + fileService.getSkippedLines().size() + " skipped.");
                for (String error : fileService.getSkippedLines()) { System.out.println(error); }
                return added;
            } catch (IOException ex) {
                System.out.println("Unable to load file: " + ex.getMessage() + " Try again.");
            }
        }
    }

    /**
     * displayBuilds prints all nine fields, including dates and selected status.
     * @param builds records to display
     * @return number of records printed
     */
    public int displayBuilds(List<PokemonBuild> builds) {
        if (builds.isEmpty()) {
            System.out.println("No builds to display.");
            return 0;
        }
        System.out.println("ID         | Species              | Types              | Role    | BST | Usage % | Team  | Updated");
        for (PokemonBuild build : builds) { System.out.println(build.toDisplayRow()); }
        System.out.println("Displayed " + builds.size() + " record(s).");
        return builds.size();
    }

    /**
     * createBuildFromInput gathers a full valid record before any storage occurs.
     * @return complete proposed record; arguments: none
     */
    public Optional<PokemonBuild> createBuildFromInput() {
        int id = readUniqueId(-1);
        String species = readSpecies();
        String primary = readType("Primary type: ", false, "");
        String secondary = readType("Secondary type (blank or None for no second type): ", true, primary);
        String role = readRole();
        int bst = readInteger("Base stat total (1-720): ", 1, 720);
        double usage = readUsage();
        boolean selected = readSelectedStatus(false);
        LocalDate date = readDate();
        return Optional.of(new PokemonBuild(id, species, primary, secondary,
                role, bst, usage, selected, date));
    }

    /**
     * addBuildFlow creates and stores one manually entered record, then displays it.
     * @return true if a build was stored; arguments: none
     */
    public boolean addBuildFlow() {
        Optional<PokemonBuild> proposed = createBuildFromInput();
        if (proposed.isPresent() && repository.addBuild(proposed.get())) {
            System.out.println("Build added.");
            displayBuilds(java.util.Collections.singletonList(proposed.get()));
            return true;
        }
        System.out.println("Build was not added: " + repository.getLastError());
        return false;
    }

    /**
     * removeBuildFlow verifies a record and requires an explicit delete confirmation.
     * @return true if a stored build was removed; arguments: none
     */
    public boolean removeBuildFlow() {
        int id = readInteger("Build ID to remove: ", 1, Integer.MAX_VALUE);
        Optional<PokemonBuild> existing = repository.findBuildById(id);
        if (!existing.isPresent()) {
            System.out.println("Build ID " + id + " was not found. Nothing was removed.");
            return false;
        }
        displayBuilds(java.util.Collections.singletonList(existing.get()));
        if (!readConfirmation("Delete this build? (yes/no): ")) {
            System.out.println("Deletion canceled. Build is unchanged.");
            return false;
        }
        boolean removed = repository.removeBuild(id);
        System.out.println(removed ? "Build " + id + " removed." : repository.getLastError());
        return removed;
    }

    /**
     * updateBuildFlow lets the user edit any one field, including ID and date.
     * @return true when a validated replacement is saved; arguments: none
     */
    public boolean updateBuildFlow() {
        int originalId = readInteger("Build ID to update: ", 1, Integer.MAX_VALUE);
        Optional<PokemonBuild> found = repository.findBuildById(originalId);
        if (!found.isPresent()) {
            System.out.println("Build ID " + originalId + " was not found. Nothing was updated.");
            return false;
        }
        PokemonBuild candidate = found.get();
        displayBuilds(java.util.Collections.singletonList(candidate));
        System.out.println("Update field:\n1. Build ID\n2. Species name\n3. Primary type\n"
                + "4. Secondary type\n5. Role\n6. Base stat total\n7. Usage rate\n"
                + "8. Selected for team\n9. Last updated date\n0. Cancel update");
        int field = readInteger("Choose a field (0-9): ", 0, 9);
        switch (field) {
            case 1: candidate.setBuildId(readUniqueId(originalId)); break;
            case 2: candidate.setSpeciesName(readSpecies()); break;
            case 3: candidate.setTypes(readType("New primary type: ", false,
                    candidate.getSecondaryType()), candidate.getSecondaryType()); break;
            case 4: candidate.setTypes(candidate.getPrimaryType(), readType(
                    "New secondary type (blank or None for no second type): ", true,
                    candidate.getPrimaryType())); break;
            case 5: candidate.setRole(readRole()); break;
            case 6: candidate.setBaseStatTotal(readInteger("New base stat total (1-720): ", 1, 720)); break;
            case 7: candidate.setUsageRate(readUsage()); break;
            case 8: candidate.setSelectedForTeam(readSelectedStatus(candidate.isSelectedForTeam())); break;
            case 9: candidate.setLastUpdated(readDate()); break;
            case 0:
                System.out.println("Update canceled. Build is unchanged.");
                return false;
            default: return false;
        }
        if (!repository.updateBuild(originalId, candidate)) {
            System.out.println("Build was not updated: " + repository.getLastError());
            return false;
        }
        System.out.println("Build updated.");
        displayBuilds(java.util.Collections.singletonList(candidate));
        return true;
    }

    /**
     * calculateScoreFlow prints the custom calculation and handles invalid teams.
     * @return score on success, or -1 if no valid team is selected; arguments: none
     */
    public double calculateScoreFlow() {
        List<PokemonBuild> builds = repository.getAllBuilds();
        try {
            double score = calculator.calculateScore(builds);
            System.out.println(calculator.buildExplanation(builds));
            return score;
        } catch (IllegalArgumentException ex) {
            System.out.println("Score unavailable: " + ex.getMessage());
            return -1.0;
        }
    }

    /**
     * filterBuildsFlow implements the species, type, role, and team views in the plan.
     * @return number of matching builds printed; arguments: none
     */
    public int filterBuildsFlow() {
        System.out.println("1. Search species name\n2. Filter by type\n3. Filter by role\n4. View selected team\n0. Return");
        int choice = readInteger("Choose a filter (0-4): ", 0, 4);
        switch (choice) {
            case 1: return displayBuilds(repository.searchBySpecies(readSpecies()));
            case 2: return displayBuilds(repository.filterByType(readType("Type to find: ", false, "")));
            case 3: return displayBuilds(repository.filterByRole(readRole()));
            case 4: return displayBuilds(repository.getSelectedBuilds());
            default: return 0;
        }
    }

    /**
     * readInput retrieves one complete line so incorrect number input is safe.
     * @param prompt text shown to the user
     * @return trimmed input text
     * @throws CancellationException when the user enters :cancel
     * @throws NoSuchElementException when console input is no longer available
     */
    private String readInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) { throw new NoSuchElementException("Input closed."); }
            String value = scanner.nextLine().trim();
            if (value.equalsIgnoreCase(":cancel")) { throw new CancellationException(); }
            if (value.length() > 4096) {
                System.out.println("Input is too long. Enter at most 4096 characters.");
                continue;
            }
            return value;
        }
    }

    /**
     * readInteger repeats the same prompt until an integer is within range.
     * @param prompt question to display
     * @param min inclusive minimum
     * @param max inclusive maximum
     * @return validated integer
     */
    private int readInteger(String prompt, int min, int max) {
        while (true) {
            Optional<Integer> value = validator.parseIntInRange(readInput(prompt), min, max);
            if (value.isPresent()) { return value.get(); }
            System.out.println("Enter a whole number from " + min + " through " + max + ".");
        }
    }

    /**
     * readUniqueId rejects duplicates before a new or updated record is assembled.
     * @param originalId current ID during an update, or -1 when adding
     * @return a positive ID not used by a different record
     */
    private int readUniqueId(int originalId) {
        while (true) {
            int id = readInteger("Build ID (positive and unique): ", 1, Integer.MAX_VALUE);
            if (id == originalId || !repository.containsId(id)) { return id; }
            System.out.println("Build ID " + id + " already exists. Use a unique ID.");
        }
    }

    /**
     * readSpecies validates the required name text.
     * @return a valid trimmed name; arguments: none
     */
    private String readSpecies() {
        while (true) {
            String name = readInput("Species name (1-60 characters): ");
            if (validator.isValidSpeciesName(name)) { return name; }
            System.out.println("Enter a name containing a letter. Commas and control characters are not allowed.");
        }
    }

    /**
     * readType validates a type and prevents identical primary/secondary types.
     * @param prompt question shown to the user
     * @param allowEmpty whether blank or None is allowed
     * @param otherType the other field's type, which cannot be repeated
     * @return normalized allowed type, or empty for an optional secondary type
     */
    private String readType(String prompt, boolean allowEmpty, String otherType) {
        System.out.println("Types: " + validator.formatTypes());
        while (true) {
            String text = readInput(prompt);
            String type = validator.normalizeType(text);
            if (!validator.isValidType(text, allowEmpty)) {
                System.out.println("Choose one of the listed types" + (allowEmpty ? " or None." : "."));
            } else if (!type.isEmpty() && type.equals(otherType)) {
                System.out.println("Primary and secondary types must be different.");
            } else {
                return type;
            }
        }
    }

    /**
     * readRole checks the allowed build-role list.
     * @return a normalized valid role; arguments: none
     */
    private String readRole() {
        System.out.println("Roles: " + validator.formatRoles());
        while (true) {
            String role = readInput("Role: ");
            if (validator.isValidRole(role)) { return validator.normalizeRole(role); }
            System.out.println("Choose one of the listed roles.");
        }
    }

    /**
     * readUsage validates the finite percentage used by the score.
     * @return a percentage from 0 through 100; arguments: none
     */
    private double readUsage() {
        while (true) {
            Optional<Double> usage = validator.parseDoubleInRange(readInput("Usage rate (0-100): "), 0.0, 100.0);
            if (usage.isPresent()) { return usage.get(); }
            System.out.println("Enter a finite number from 0 through 100.");
        }
    }

    /**
     * readSelectedStatus validates true/false and prevents a seventh selected build.
     * @param currentlySelected whether this record already occupies a team slot
     * @return valid selected status
     */
    private boolean readSelectedStatus(boolean currentlySelected) {
        while (true) {
            Optional<Boolean> selected = validator.parseBoolean(readInput("Selected for team (true/false): "));
            if (!selected.isPresent()) {
                System.out.println("Enter true or false.");
            } else if (selected.get() && !currentlySelected && repository.getSelectedBuilds().size() >= 6) {
                System.out.println("A team can have at most 6 selected builds. Enter false or :cancel.");
            } else {
                return selected.get();
            }
        }
    }

    /**
     * readDate requires a real calendar date with the planned LocalDate data type.
     * @return validated date from 1900-01-01 through today; arguments: none
     */
    private LocalDate readDate() {
        while (true) {
            Optional<LocalDate> date = validator.parseDate(readInput("Last updated (YYYY-MM-DD, 1900-01-01 through today): "));
            if (date.isPresent()) { return date.get(); }
            System.out.println("Enter a real YYYY-MM-DD date from 1900-01-01 through today.");
        }
    }

    /**
     * readConfirmation prevents an unrecognized response from approving removal.
     * @param prompt confirmation question
     * @return true for yes/y, false for no/n
     */
    private boolean readConfirmation(String prompt) {
        while (true) {
            String answer = readInput(prompt);
            if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) { return true; }
            if (answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) { return false; }
            System.out.println("Enter yes or no.");
        }
    }

    /**
     * close releases the scanner after the application loop has finished.
     * @return true once the scanner has been closed; arguments: none
     */
    public boolean close() {
        scanner.close();
        return true;
    }
}
