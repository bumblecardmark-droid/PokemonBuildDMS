package dms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: TeamReadinessCalculator
 * Calculates and explains the planned Team Readiness Score using only selected
 * builds. Type coverage means distinct types represented in the stored team.
 */
public class TeamReadinessCalculator {
    private final BuildValidator validator;
    private final int maxBaseStatTotal = 720;
    private final int maxTypeCoverage = 18;
    private final int maxUniqueRoles = 4;

    public TeamReadinessCalculator(BuildValidator validator) {
        this.validator = validator;
    }

    /**
     * calculateScore evaluates the Module 4 formula for a validated selected team.
     * @param builds available stored records
     * @return readiness score from 0 through 100
     * @throws IllegalArgumentException for no selection, over six, or invalid data
     */
    public double calculateScore(List<PokemonBuild> builds) {
        List<PokemonBuild> team = checkedTeam(builds);
        double statPoints = calculateAverageBst(team) / maxBaseStatTotal * 40.0;
        double typePoints = calculateTypeCoverage(team) / (double) maxTypeCoverage * 25.0;
        // Four roles earn the full role allocation; extra roles cannot exceed 20 points.
        double rolePoints = Math.min(calculateUniqueRoles(team), maxUniqueRoles)
                / (double) maxUniqueRoles * 20.0;
        double usagePoints = calculateAverageUsage(team) / 100.0 * 15.0;
        return statPoints + typePoints + rolePoints + usagePoints;
    }

    /**
     * calculateAverageBst finds the mean total for a supplied team.
     * @param builds selected, validated team records
     * @return average base stat total, or 0 for an empty list
     */
    public double calculateAverageBst(List<PokemonBuild> builds) {
        if (builds.isEmpty()) { return 0.0; }
        double total = 0.0;
        for (PokemonBuild build : builds) { total += build.getBaseStatTotal(); }
        return total / builds.size();
    }

    /**
     * calculateTypeCoverage counts represented types without double counting.
     * @param builds selected, validated team records
     * @return count of distinct primary and nonempty secondary types
     */
    public int calculateTypeCoverage(List<PokemonBuild> builds) {
        Set<String> types = new LinkedHashSet<String>();
        for (PokemonBuild build : builds) {
            types.add(build.getPrimaryType());
            if (!build.getSecondaryType().isEmpty()) { types.add(build.getSecondaryType()); }
        }
        return types.size();
    }

    /**
     * calculateUniqueRoles counts the different roles represented by a team.
     * @param builds selected, validated team records
     * @return number of distinct roles before the four-role scoring cap
     */
    public int calculateUniqueRoles(List<PokemonBuild> builds) {
        Set<String> roles = new LinkedHashSet<String>();
        for (PokemonBuild build : builds) { roles.add(build.getRole()); }
        return roles.size();
    }

    /**
     * calculateAverageUsage finds the mean demo usage percentage for a team.
     * @param builds selected, validated team records
     * @return average percentage, or 0 for an empty list
     */
    public double calculateAverageUsage(List<PokemonBuild> builds) {
        if (builds.isEmpty()) { return 0.0; }
        double total = 0.0;
        for (PokemonBuild build : builds) { total += build.getUsageRate(); }
        return total / builds.size();
    }

    /**
     * buildExplanation shows the team and each weighted component of the score.
     * @param builds available stored records
     * @return a readable calculation breakdown and team balance feedback
     * @throws IllegalArgumentException when the selected team is invalid
     */
    public String buildExplanation(List<PokemonBuild> builds) {
        List<PokemonBuild> team = checkedTeam(builds);
        List<String> names = new ArrayList<String>();
        for (PokemonBuild build : team) { names.add(build.getSpeciesName()); }
        double bst = calculateAverageBst(team);
        int types = calculateTypeCoverage(team);
        int roles = calculateUniqueRoles(team);
        double usage = calculateAverageUsage(team);
        String explanation = String.format(Locale.US,
                "Selected builds (%d): %s%n"
                + "Average BST: %.2f / 720 x 40 = %.2f points%n"
                + "Distinct represented types: %d / 18 x 25 = %.2f points%n"
                + "Distinct roles: %d (up to 4 scored) / 4 x 20 = %.2f points%n"
                + "Average usage: %.2f / 100 x 15 = %.2f points%n"
                + "Team Readiness Score: %.2f / 100%n",
                team.size(), String.join(", ", names), bst, bst / 720.0 * 40.0,
                types, types / 18.0 * 25.0, roles, Math.min(roles, 4) / 4.0 * 20.0,
                usage, usage / 100.0 * 15.0, calculateScore(team));
        if (team.size() < 6) { explanation += "Team note: " + (6 - team.size()) + " team slot(s) remain open.\n"; }
        if (roles < 4) { explanation += "Role note: fewer than four different roles are represented.\n"; }
        if (types < 6) { explanation += "Type note: fewer than six different types are represented.\n"; }
        return explanation;
    }

    /**
     * isTeamSizeValid checks the selected count before attempting a calculation.
     * @param builds available records
     * @return true when one through six nonnull records are selected
     */
    public boolean isTeamSizeValid(List<PokemonBuild> builds) {
        if (builds == null) { return false; }
        int count = 0;
        for (PokemonBuild build : builds) {
            if (build == null) { return false; }
            if (build.isSelectedForTeam()) { count++; }
        }
        return count >= 1 && count <= 6;
    }

    /**
     * checkedTeam rejects invalid team sizes and data before score arithmetic.
     * @param builds available records
     * @return selected records that passed all field and unique-ID checks
     * @throws IllegalArgumentException if selected records are invalid
     */
    private List<PokemonBuild> checkedTeam(List<PokemonBuild> builds) {
        if (!isTeamSizeValid(builds)) {
            throw new IllegalArgumentException("Select 1 to 6 builds before calculating the score.");
        }
        List<PokemonBuild> team = new ArrayList<PokemonBuild>();
        Set<Integer> ids = new LinkedHashSet<Integer>();
        for (PokemonBuild build : builds) {
            if (build.isSelectedForTeam()) {
                String error = validator.getValidationError(build);
                if (!error.isEmpty()) { throw new IllegalArgumentException(error); }
                if (!ids.add(build.getBuildId())) {
                    throw new IllegalArgumentException("The selected team contains a duplicate build ID.");
                }
                team.add(build);
            }
        }
        return team;
    }
}
