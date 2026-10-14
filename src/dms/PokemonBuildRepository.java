package dms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: PokemonBuildRepository
 * Owns the Phase 1 ArrayList and performs validated CRUD and filtering.
 * Copies prevent outside callers from bypassing duplicate-ID or team-size rules.
 */
public class PokemonBuildRepository {
    private final ArrayList<PokemonBuild> builds = new ArrayList<PokemonBuild>();
    private final BuildValidator validator;
    private String lastError = "";

    public PokemonBuildRepository(BuildValidator validator) {
        this.validator = validator;
    }

    public String getLastError() { return lastError; }

    /**
     * addBuild stores a valid build only if its ID and team selection are allowed.
     * @param build proposed new record
     * @return true if one record was added, otherwise false with lastError set
     */
    public boolean addBuild(PokemonBuild build) {
        lastError = validator.getValidationError(build);
        if (!lastError.isEmpty()) { return false; }
        if (containsId(build.getBuildId())) {
            lastError = "Build ID " + build.getBuildId() + " already exists. Use a unique ID.";
            return false;
        }
        if (!canStoreSelection(build, -1)) {
            lastError = "A team can have at most 6 selected builds. Deselect a build first.";
            return false;
        }
        builds.add(build.copy());
        return true;
    }

    /**
     * removeBuild deletes a stored record by its ID.
     * @param buildId ID of the record to remove
     * @return true if the record existed and was removed
     */
    public boolean removeBuild(int buildId) {
        int index = findIndex(buildId);
        if (index < 0) {
            lastError = "Build ID " + buildId + " was not found.";
            return false;
        }
        builds.remove(index);
        lastError = "";
        return true;
    }

    /**
     * updateBuild replaces a record while retaining its current ID.
     * @param updatedBuild complete edited record
     * @return true if the existing record was safely replaced
     */
    public boolean updateBuild(PokemonBuild updatedBuild) {
        return updatedBuild != null && updateBuild(updatedBuild.getBuildId(), updatedBuild);
    }

    /**
     * updateBuild validates a complete replacement, including a changed ID.
     * @param originalId ID used to find the existing record
     * @param updatedBuild complete proposed replacement
     * @return true on success, or false without changing any stored field
     */
    public boolean updateBuild(int originalId, PokemonBuild updatedBuild) {
        int index = findIndex(originalId);
        if (index < 0) {
            lastError = "Build ID " + originalId + " was not found.";
            return false;
        }
        lastError = validator.getValidationError(updatedBuild);
        if (!lastError.isEmpty()) { return false; }
        if (updatedBuild.getBuildId() != originalId && containsId(updatedBuild.getBuildId())) {
            lastError = "Build ID " + updatedBuild.getBuildId() + " already exists. Use a unique ID.";
            return false;
        }
        if (!canStoreSelection(updatedBuild, originalId)) {
            lastError = "A team can have at most 6 selected builds. Deselect a build first.";
            return false;
        }
        builds.set(index, updatedBuild.copy());
        lastError = "";
        return true;
    }

    /**
     * findBuildById retrieves an independent copy for display or editing.
     * @param buildId requested record ID
     * @return the matching copy, or empty when no record exists
     */
    public Optional<PokemonBuild> findBuildById(int buildId) {
        int index = findIndex(buildId);
        return index < 0 ? Optional.empty() : Optional.of(builds.get(index).copy());
    }

    /**
     * getAllBuilds retrieves every record without exposing internal storage.
     * @return copies of all stored builds in insertion order; arguments: none
     */
    public List<PokemonBuild> getAllBuilds() {
        List<PokemonBuild> results = new ArrayList<PokemonBuild>();
        for (PokemonBuild build : builds) { results.add(build.copy()); }
        return results;
    }

    /**
     * filterByType matches either type field, ignoring capitalization.
     * @param type type to find
     * @return copies of matching builds, or an empty list
     */
    public List<PokemonBuild> filterByType(String type) {
        List<PokemonBuild> results = new ArrayList<PokemonBuild>();
        if (!validator.isValidType(type, false)) { return results; }
        String normalized = validator.normalizeType(type);
        for (PokemonBuild build : builds) {
            if (build.getPrimaryType().equals(normalized)
                    || build.getSecondaryType().equals(normalized)) {
                results.add(build.copy());
            }
        }
        return results;
    }

    /**
     * filterByRole finds records assigned to a planned role.
     * @param role role to find
     * @return copies of matching builds, or an empty list
     */
    public List<PokemonBuild> filterByRole(String role) {
        List<PokemonBuild> results = new ArrayList<PokemonBuild>();
        String normalized = validator.normalizeRole(role);
        for (PokemonBuild build : builds) {
            if (build.getRole().equals(normalized)) { results.add(build.copy()); }
        }
        return results;
    }

    /**
     * searchBySpecies finds names containing the user's search text.
     * @param query nonblank name fragment
     * @return copies of case-insensitive matches, or an empty list
     */
    public List<PokemonBuild> searchBySpecies(String query) {
        List<PokemonBuild> results = new ArrayList<PokemonBuild>();
        if (query == null || query.trim().isEmpty()) { return results; }
        for (PokemonBuild build : builds) {
            if (build.getSpeciesName().toLowerCase(java.util.Locale.ROOT)
                    .contains(query.trim().toLowerCase(java.util.Locale.ROOT))) {
                results.add(build.copy());
            }
        }
        return results;
    }

    /**
     * getSelectedBuilds retrieves the current team.
     * @return copies of builds marked selectedForTeam=true; arguments: none
     */
    public List<PokemonBuild> getSelectedBuilds() {
        List<PokemonBuild> results = new ArrayList<PokemonBuild>();
        for (PokemonBuild build : builds) {
            if (build.isSelectedForTeam()) { results.add(build.copy()); }
        }
        return results;
    }

    /**
     * containsId determines whether an ID is already stored.
     * @param buildId requested ID
     * @return true if a record has this ID
     */
    public boolean containsId(int buildId) {
        return findIndex(buildId) >= 0;
    }

    /**
     * count reports the number of stored records.
     * @return current record count; arguments: none
     */
    public int count() {
        return builds.size();
    }

    /**
     * findIndex supports CRUD without changing the collection during iteration.
     * @param buildId requested ID
     * @return matching index, or -1 if no record matches
     */
    private int findIndex(int buildId) {
        for (int index = 0; index < builds.size(); index++) {
            if (builds.get(index).getBuildId() == buildId) { return index; }
        }
        return -1;
    }

    /**
     * canStoreSelection enforces six slots while excluding a replaced record.
     * @param candidate proposed new or updated build
     * @param excludedId existing ID to ignore, or -1 for an addition
     * @return true when accepting the record will not exceed six selected builds
     */
    private boolean canStoreSelection(PokemonBuild candidate, int excludedId) {
        int selected = candidate.isSelectedForTeam() ? 1 : 0;
        for (PokemonBuild build : builds) {
            if (build.getBuildId() != excludedId && build.isSelectedForTeam()) { selected++; }
        }
        return selected <= 6;
    }
}
