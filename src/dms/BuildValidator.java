package dms;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: BuildValidator
 * Provides shared validation for console input, imported records, and stored
 * Pokemon builds so every entry path uses the same data rules.
 */
public class BuildValidator {
    private final List<String> allowedTypes = Arrays.asList(
            "Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting",
            "Poison", "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost",
            "Dragon", "Dark", "Steel", "Fairy");
    private final List<String> allowedRoles = Arrays.asList(
            "Sweeper", "Wall", "Pivot", "Support", "Lead", "Other");

    /**
     * isValidId checks whether an ID can identify a build.
     * @param buildId proposed integer ID
     * @return true when the ID is positive
     */
    public boolean isValidId(int buildId) {
        return buildId > 0;
    }

    /**
     * isValidSpeciesName checks required text and file-safe name characters.
     * @param speciesName proposed species or form name
     * @return true for 1-60 characters containing a letter and no delimiters
     */
    public boolean isValidSpeciesName(String speciesName) {
        if (speciesName == null) {
            return false;
        }
        String name = speciesName.trim();
        return !name.isEmpty() && name.length() <= 60
                && name.matches("[\\p{L}\\p{N} .:'\u2019\\-\u2640\u2642]+")
                && name.matches(".*\\p{L}.*");
    }

    /**
     * isValidType checks the allowed type list, including an optional second type.
     * @param type proposed type text
     * @param allowEmpty whether blank or None represents no secondary type
     * @return true if the proposed type is permitted
     */
    public boolean isValidType(String type, boolean allowEmpty) {
        if (type == null) {
            return false;
        }
        String normalized = normalizeType(type);
        return (allowEmpty && normalized.isEmpty()) || allowedTypes.contains(normalized);
    }

    /**
     * normalizeType returns the standard spelling for a recognized type.
     * @param type entered type text
     * @return canonical type, empty for None, or trimmed unrecognized text
     */
    public String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        String value = type.trim();
        if (value.equalsIgnoreCase("none")) {
            return "";
        }
        for (String allowed : allowedTypes) {
            if (allowed.equalsIgnoreCase(value)) {
                return allowed;
            }
        }
        return value;
    }

    /**
     * isValidRole checks the planned role choices.
     * @param role proposed role
     * @return true when the role is recognized
     */
    public boolean isValidRole(String role) {
        return role != null && allowedRoles.contains(normalizeRole(role));
    }

    /**
     * normalizeRole makes valid role spelling consistent across entry paths.
     * @param role entered role text
     * @return standard role spelling or trimmed unrecognized text
     */
    public String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        for (String allowed : allowedRoles) {
            if (allowed.equalsIgnoreCase(role.trim())) {
                return allowed;
            }
        }
        return role.trim();
    }

    /**
     * isValidBaseStatTotal applies the stat range defined by the project plan.
     * @param baseStatTotal proposed total
     * @return true for totals from 1 through 720
     */
    public boolean isValidBaseStatTotal(int baseStatTotal) {
        return baseStatTotal >= 1 && baseStatTotal <= 720;
    }

    /**
     * isValidUsageRate rejects nonfinite values and invalid percentages.
     * @param usageRate proposed percentage
     * @return true for finite percentages from 0 through 100
     */
    public boolean isValidUsageRate(double usageRate) {
        return !Double.isNaN(usageRate) && !Double.isInfinite(usageRate)
                && usageRate >= 0.0 && usageRate <= 100.0;
    }

    /**
     * isValidDate checks the supported date range for an updated record.
     * @param date proposed calendar date
     * @return true from January 1, 1900 through the current local date
     */
    public boolean isValidDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.of(1900, 1, 1))
                && !date.isAfter(LocalDate.now());
    }

    /**
     * validateBuild checks all fields without changing the object.
     * @param build record to check
     * @return true when no field validation error exists
     */
    public boolean validateBuild(PokemonBuild build) {
        return getValidationError(build).isEmpty();
    }

    /**
     * getValidationError explains the first invalid field in a build.
     * @param build record to check
     * @return a helpful error message, or an empty string for a valid record
     */
    public String getValidationError(PokemonBuild build) {
        if (build == null) {
            return "A build record is required.";
        }
        if (!isValidId(build.getBuildId())) {
            return "Build ID must be a positive whole number.";
        }
        if (!isValidSpeciesName(build.getSpeciesName())) {
            return "Species name must contain a letter, use 1-60 characters, and contain no commas or control characters.";
        }
        if (!isValidType(build.getPrimaryType(), false)) {
            return "Primary type must be one of the 18 allowed Pokemon types.";
        }
        if (!isValidType(build.getSecondaryType(), true)) {
            return "Secondary type must be an allowed type or None.";
        }
        if (build.getPrimaryType().equalsIgnoreCase(build.getSecondaryType())) {
            return "Primary and secondary types must be different.";
        }
        if (!isValidRole(build.getRole())) {
            return "Role must be Sweeper, Wall, Pivot, Support, Lead, or Other.";
        }
        if (!isValidBaseStatTotal(build.getBaseStatTotal())) {
            return "Base stat total must be a whole number from 1 through 720.";
        }
        if (!isValidUsageRate(build.getUsageRate())) {
            return "Usage rate must be a finite number from 0 through 100.";
        }
        if (!isValidDate(build.getLastUpdated())) {
            return "Last updated must be a real date from 1900-01-01 through today.";
        }
        return "";
    }

    /**
     * parseIntInRange converts text without exposing conversion exceptions.
     * @param value input text
     * @param min inclusive minimum
     * @param max inclusive maximum
     * @return a valid integer, or empty for blank, overflow, or out-of-range text
     */
    public Optional<Integer> parseIntInRange(String value, int min, int max) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            int number = Integer.parseInt(value.trim());
            return number >= min && number <= max ? Optional.of(number) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * parseDoubleInRange converts an ordinary decimal or scientific notation.
     * @param value numeric text
     * @param min inclusive minimum
     * @param max inclusive maximum
     * @return a finite in-range number, or empty for invalid input
     */
    public Optional<Double> parseDoubleInRange(String value, double min, double max) {
        if (value == null || !value.trim().matches(
                "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?")) {
            return Optional.empty();
        }
        try {
            double number = Double.parseDouble(value.trim());
            return !Double.isNaN(number) && !Double.isInfinite(number)
                    && number >= min && number <= max ? Optional.of(number) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * parseBoolean requires explicit true or false rather than silently using false.
     * @param value boolean text
     * @return the boolean value, or empty for an unsupported response
     */
    public Optional<Boolean> parseBoolean(String value) {
        if (value != null && value.trim().equalsIgnoreCase("true")) {
            return Optional.of(true);
        }
        if (value != null && value.trim().equalsIgnoreCase("false")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    /**
     * parseDate requires a real ISO calendar date within the supported range.
     * @param value date text in YYYY-MM-DD format
     * @return the valid date, or empty when the text or date is invalid
     */
    public Optional<LocalDate> parseDate(String value) {
        if (value == null || !value.trim().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return Optional.empty();
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return isValidDate(date) ? Optional.of(date) : Optional.empty();
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    /**
     * formatTypes supplies the current allowed choices for console prompts.
     * @return comma-separated list of allowed types; arguments: none
     */
    public String formatTypes() {
        return String.join(", ", allowedTypes);
    }

    /**
     * formatRoles supplies the current allowed choices for console prompts.
     * @return comma-separated list of allowed roles; arguments: none
     */
    public String formatRoles() {
        return String.join(", ", allowedRoles);
    }
}
