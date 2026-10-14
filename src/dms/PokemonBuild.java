package dms;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: PokemonBuild
 * Stores the nine planned attributes for one Pokemon build. Validated setters
 * preserve the previous value whenever a proposed change is invalid.
 */
public class PokemonBuild {
    private int buildId;
    private String speciesName;
    private String primaryType;
    private String secondaryType;
    private String role;
    private int baseStatTotal;
    private double usageRate;
    private boolean selectedForTeam;
    private LocalDate lastUpdated;
    private final BuildValidator validator = new BuildValidator();

    public PokemonBuild(int buildId, String speciesName, String primaryType,
                        String secondaryType, String role, int baseStatTotal,
                        double usageRate, boolean selectedForTeam, LocalDate lastUpdated) {
        this.buildId = buildId;
        this.speciesName = speciesName == null ? "" : speciesName.trim();
        this.primaryType = validator.normalizeType(primaryType);
        this.secondaryType = validator.normalizeType(secondaryType);
        this.role = validator.normalizeRole(role);
        this.baseStatTotal = baseStatTotal;
        this.usageRate = usageRate;
        this.selectedForTeam = selectedForTeam;
        this.lastUpdated = lastUpdated;
        String error = validator.getValidationError(this);
        if (!error.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    public int getBuildId() { return buildId; }
    public String getSpeciesName() { return speciesName; }
    public String getPrimaryType() { return primaryType; }
    public String getSecondaryType() { return secondaryType; }
    public String getRole() { return role; }
    public int getBaseStatTotal() { return baseStatTotal; }
    public double getUsageRate() { return usageRate; }
    public boolean isSelectedForTeam() { return selectedForTeam; }
    public LocalDate getLastUpdated() { return lastUpdated; }

    public boolean setBuildId(int buildId) {
        if (!validator.isValidId(buildId)) { return false; }
        this.buildId = buildId;
        return true;
    }

    public boolean setSpeciesName(String speciesName) {
        if (!validator.isValidSpeciesName(speciesName)) { return false; }
        this.speciesName = speciesName.trim();
        return true;
    }

    public boolean setTypes(String primary, String secondary) {
        if (!validator.isValidType(primary, false) || !validator.isValidType(secondary, true)) {
            return false;
        }
        String first = validator.normalizeType(primary);
        String second = validator.normalizeType(secondary);
        if (first.equals(second)) { return false; }
        primaryType = first;
        secondaryType = second;
        return true;
    }

    public boolean setRole(String role) {
        if (!validator.isValidRole(role)) { return false; }
        this.role = validator.normalizeRole(role);
        return true;
    }

    public boolean setBaseStatTotal(int baseStatTotal) {
        if (!validator.isValidBaseStatTotal(baseStatTotal)) { return false; }
        this.baseStatTotal = baseStatTotal;
        return true;
    }

    public boolean setUsageRate(double usageRate) {
        if (!validator.isValidUsageRate(usageRate)) { return false; }
        this.usageRate = usageRate;
        return true;
    }

    public boolean setSelectedForTeam(boolean selectedForTeam) {
        this.selectedForTeam = selectedForTeam;
        return true;
    }

    public boolean setLastUpdated(LocalDate lastUpdated) {
        if (!validator.isValidDate(lastUpdated)) { return false; }
        this.lastUpdated = lastUpdated;
        return true;
    }

    /**
     * copy isolates edits from the repository until a complete update is accepted.
     * @return an independent build with identical values; arguments: none
     */
    public PokemonBuild copy() {
        return new PokemonBuild(buildId, speciesName, primaryType, secondaryType,
                role, baseStatTotal, usageRate, selectedForTeam, lastUpdated);
    }

    /**
     * toCsvLine formats the nine fields in the text-file import order.
     * @return a comma-separated record without a header; arguments: none
     */
    public String toCsvLine() {
        return buildId + "," + speciesName + "," + primaryType + "," + secondaryType
                + "," + role + "," + baseStatTotal + "," + Double.toString(usageRate)
                + "," + selectedForTeam + "," + lastUpdated;
    }

    /**
     * toDisplayRow formats every stored field for the console table.
     * @return one complete table row; arguments: none
     */
    public String toDisplayRow() {
        String types = primaryType + (secondaryType.isEmpty() ? "" : "/" + secondaryType);
        return String.format(Locale.US,
                "%-10d | %-20s | %-18s | %-7s | %3d | %7.2f | %-5s | %s",
                buildId, speciesName, types, role, baseStatTotal, usageRate,
                selectedForTeam, lastUpdated);
    }
}
