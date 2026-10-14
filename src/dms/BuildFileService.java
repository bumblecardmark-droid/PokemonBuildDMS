package dms;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: BuildFileService
 * Reads nine-field comma-separated records from a user-supplied text file.
 * Invalid rows are reported individually while valid records can be imported.
 */
public class BuildFileService {
    private final BuildValidator validator;
    private final ArrayList<String> skippedLines = new ArrayList<String>();
    private String parseError = "";

    public BuildFileService(BuildValidator validator) {
        this.validator = validator;
    }

    /**
     * isReadableFile checks a path without exposing file-system exceptions.
     * @param filePath user-entered location, optionally surrounded by quotes
     * @return true for a readable regular file with a nonblank valid path
     */
    public boolean isReadableFile(String filePath) {
        try {
            Path path = resolvePath(filePath);
            return Files.isRegularFile(path) && Files.isReadable(path);
        } catch (IllegalArgumentException | SecurityException ex) {
            return false;
        }
    }

    /**
     * loadFromCsv reads a whole file before storing valid rows in the repository.
     * @param filePath text file containing the nine planned fields
     * @param repo repository receiving valid, unique records
     * @return number of records successfully added
     * @throws IOException if the file cannot be read; no pending rows are committed
     */
    public int loadFromCsv(String filePath, PokemonBuildRepository repo) throws IOException {
        skippedLines.clear();
        Path path;
        try {
            path = resolvePath(filePath);
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new IOException("Choose an existing readable text file, not a folder.");
            }
            if (Files.size(path) > 5L * 1024L * 1024L) {
                throw new IOException("The text file is too large. Use a file smaller than 5 MB.");
            }
        } catch (InvalidPathException | SecurityException ex) {
            throw new IOException("The file location is invalid or cannot be accessed.", ex);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage(), ex);
        }

        List<PokemonBuild> pending = new ArrayList<PokemonBuild>();
        List<Integer> sourceLines = new ArrayList<Integer>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) { line = line.substring(1); }
                if (line.trim().isEmpty() || line.trim().startsWith("#")) { continue; }
                if (lineNumber == 1 && isHeader(line)) { continue; }
                Optional<PokemonBuild> parsed = parseLine(line);
                if (parsed.isPresent()) {
                    pending.add(parsed.get());
                    sourceLines.add(lineNumber);
                } else {
                    skippedLines.add("Line " + lineNumber + ": " + parseError);
                }
            }
        } catch (SecurityException ex) {
            throw new IOException("The file cannot be read with the current permissions.", ex);
        }

        // Reading finishes first so an I/O failure cannot leave a half-imported file.
        int added = 0;
        for (int index = 0; index < pending.size(); index++) {
            if (repo.addBuild(pending.get(index))) {
                added++;
            } else {
                skippedLines.add("Line " + sourceLines.get(index) + ": " + repo.getLastError());
            }
        }
        return added;
    }

    /**
     * parseLine validates the exact field count and every field in one record.
     * @param line one unquoted comma-separated data row
     * @return a valid build, or empty with an explanatory parseError
     */
    public Optional<PokemonBuild> parseLine(String line) {
        parseError = "";
        if (line == null) { return reject("A data row is required."); }
        String[] fields = line.split(",", -1);
        if (fields.length != 9) {
            return reject("Expected exactly 9 comma-separated fields.");
        }
        Optional<Integer> id = validator.parseIntInRange(fields[0], 1, Integer.MAX_VALUE);
        Optional<Integer> bst = validator.parseIntInRange(fields[5], 1, 720);
        Optional<Double> usage = validator.parseDoubleInRange(fields[6], 0.0, 100.0);
        Optional<Boolean> selected = validator.parseBoolean(fields[7]);
        Optional<LocalDate> date = validator.parseDate(fields[8]);
        if (!id.isPresent()) { return reject("Build ID must be a positive whole number."); }
        if (!bst.isPresent()) { return reject("Base stat total must be a whole number from 1 through 720."); }
        if (!usage.isPresent()) { return reject("Usage rate must be a finite number from 0 through 100."); }
        if (!selected.isPresent()) { return reject("Selected status must be true or false."); }
        if (!date.isPresent()) { return reject("Last updated must be a real YYYY-MM-DD date from 1900-01-01 through today."); }
        try {
            return Optional.of(new PokemonBuild(id.get(), fields[1], fields[2], fields[3],
                    fields[4], bst.get(), usage.get(), selected.get(), date.get()));
        } catch (IllegalArgumentException ex) {
            return reject(ex.getMessage());
        }
    }

    /**
     * getSkippedLines supplies import errors without exposing the internal list.
     * @return copy of line-numbered errors from the last load; arguments: none
     */
    public List<String> getSkippedLines() {
        return new ArrayList<String>(skippedLines);
    }

    /**
     * resolvePath accepts a relative or absolute location, including pasted quotes.
     * @param filePath supplied location
     * @return parsed file-system path
     * @throws IllegalArgumentException when the input is blank or malformed
     */
    private Path resolvePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File location cannot be blank.");
        }
        String value = filePath.trim();
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            value = value.substring(1, value.length() - 1).trim();
        }
        if (value.isEmpty()) { throw new IllegalArgumentException("File location cannot be blank."); }
        return Paths.get(value);
    }

    /**
     * isHeader recognizes the optional exact field-name row, ignoring case/spaces.
     * @param line first text-file line
     * @return true when the line lists the nine supported field names
     */
    private boolean isHeader(String line) {
        return line.replace(" ", "").equalsIgnoreCase(
                "buildId,speciesName,primaryType,secondaryType,role,baseStatTotal,usageRate,selectedForTeam,lastUpdated");
    }

    /**
     * reject records the reason a row failed parsing.
     * @param message explanation for the rejected row
     * @return an empty build result
     */
    private Optional<PokemonBuild> reject(String message) {
        parseError = message;
        return Optional.empty();
    }
}
