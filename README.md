# Pokemon Build Management System

Adriel Portillo Perez  
CEN 3024C - Module 7: Phase 1 Logic and Input Validation  
October 13, 2026

This Java console program manages Pokemon build records with file import, display,
manual creation, removal, updates, search/filter views, and a Team Readiness Score.
Records are held in an ArrayList for this phase. Restarting begins with an empty
list, and changes do not overwrite the supplied sample file.

## Run on Windows

1. Extract the entire ZIP to a folder.
2. Open the `PokemonBuildDMS` folder.
3. Double-click `run.bat`. A console window opens and stays available for input.
4. Choose `1`, enter `data.txt`, then choose `2` to display the 20 samples.

Java 8 or newer must be installed and available on PATH. The compiled JAR targets
Java 8 bytecode. Java 7 cannot run it because the approved design uses LocalDate
and Optional, which require Java 8.

If you prefer a terminal, open it in the project folder and run:

```text
java -jar dist/PokemonBuildDMS.jar
```

This is a console application. Use `run.bat` or a terminal to launch the JAR.

## Open in IntelliJ

1. Extract the ZIP, then open IntelliJ.
2. Choose **Open** and select the extracted `PokemonBuildDMS` folder.
3. Open **File > Project Structure > Project** and select your installed JDK
   (Java 8 or newer). Keep the language level at **8**.
4. Open `src/dms/Main.java` and run its `main` method, or select the supplied
   `PokemonBuildDMS` run configuration.
5. Keep the run working directory set to the project folder so `data.txt` works.

The project includes an `.iml` file, shared run configuration, and JAR artifact
configuration. The JDK name may need to be selected on your computer.

## Menu

| Option | Action |
| --- | --- |
| 1 | Load records from a user-entered text-file location |
| 2 | Display every field of every stored record |
| 3 | Manually add one complete record |
| 4 | Find a build by ID, confirm it, and remove it |
| 5 | Update any one of the nine fields of an existing record |
| 6 | Calculate and explain the Team Readiness Score |
| 7 | Search species names or filter by type, role, or selected status |
| 0 | Exit |

Enter `:cancel` at an action prompt to return to the menu without saving the pending
manual entry. Incorrect input is rejected with feedback and a retry prompt.
An externally closed console input stream is handled safely rather than spinning
indefinitely. Ordinary invalid input never ends the application.

## Sample data format

`data.txt` contains 20 distinct sample records, with an optional first-line header:

```text
buildId,speciesName,primaryType,secondaryType,role,baseStatTotal,usageRate,selectedForTeam,lastUpdated
101,Garchomp,Dragon,Ground,Sweeper,600,28.4,true,2026-10-13
106,Sceptile,Grass,,Sweeper,530,8.5,false,2026-10-13
```

Use exactly nine unquoted comma-separated fields per data row. Secondary type may
be blank or `None`. Usage values in the sample file are illustrative test values,
not current tournament statistics. The program validates field formats and ranges;
it does not look up names, types, or stats in a live Pokemon database.

Relative paths such as `data.txt` resolve from the current working directory.
Absolute paths and quoted paths with spaces are also supported. Files use UTF-8,
including an optional UTF-8 BOM. Blank rows and rows beginning with `#` are ignored.
Invalid rows, repeated IDs, and excess selected records are skipped with line-numbered
messages. A file read failure leaves preexisting records unchanged.

## Validation rules

| Field | Rule |
| --- | --- |
| buildId | Integer from 1 through 2,147,483,647; unique among stored records |
| speciesName | 1-60 characters, at least one letter; letters, digits, spaces, periods, apostrophes, colons, hyphens, and gender symbols allowed |
| primaryType | One of the 18 standard types |
| secondaryType | An allowed type different from the primary type, or blank/None |
| role | Sweeper, Wall, Pivot, Support, Lead, or Other |
| baseStatTotal | Integer from 1 through 720, following the submitted plan's range |
| usageRate | Finite number from 0 through 100; NaN and infinity rejected |
| selectedForTeam | Explicit true/false; no more than six records selected |
| lastUpdated | Real YYYY-MM-DD calendar date from 1900-01-01 through today |

Types, roles, and booleans accept either capitalization. Dates and IDs can be
edited through option 5. Changing an ID preserves the original record's position
and rejects collisions with other records. Invalid edits retain all old values.
The stored lastUpdated value is user-editable and is not automatically replaced
when another field changes.

## Custom calculation

Only records marked `selectedForTeam=true` are used. Select one through six builds.
Type coverage means the number of distinct primary/secondary types represented
in the team, rather than move coverage or simulated battle effectiveness.

```text
Score = (Average BST / 720 * 40)
      + (Distinct represented types / 18 * 25)
      + (min(Distinct roles, 4) / 4 * 20)
      + (Average usage / 100 * 15)
```

Four distinct roles earn the full 20 role points. The cap resolves the plan's
four-role scoring maximum when a team contains five or six different roles and
keeps that component within its allocation.

The original sample file selects Garchomp, Rotom-Heat, Corviknight, Whimsicott,
and Gyarados:

| Component | Value | Points |
| --- | --- | --- |
| Average BST | 527.00 | 29.27777778 |
| Distinct represented types | 9 | 12.50 |
| Distinct roles | 4 | 20.00 |
| Average demo usage | 17.80 | 2.67 |
| Total | | **64.44777778, displayed as 64.45/100** |

The program prints the component breakdown and simple team balance notes.

## Class design

| File | Responsibility |
| --- | --- |
| Main.java | Required Java launcher |
| PokemonBuild.java | Nine-field object with validated setters and display formatting |
| BuildValidator.java | Shared numeric, text, type, role, boolean, and date rules |
| PokemonBuildRepository.java | ArrayList storage, CRUD, ID uniqueness, filters, and team-size checks |
| BuildFileService.java | User-supplied text-file loading and invalid-row reporting |
| TeamReadinessCalculator.java | Score arithmetic and explanation |
| PokemonBuildApp.java | Console menu, prompts, and user actions |

All application methods are instance methods with return values. Only Java's
required `Main.main` is static and void. Class and method comments follow the
supplied Canvas guidance; constructors, getters, and setters use its exemption.

## Rebuild the JAR

With a JDK available on PATH, run `build.bat` on Windows or `sh build.sh` on macOS/Linux.
The scripts compile the source and replace `dist/PokemonBuildDMS.jar`.
In IntelliJ, use **Build > Build Artifacts > PokemonBuildDMS:jar > Build**.

Compilation was checked with the Java 8 release target on OpenJDK 17. The executable
JAR was exercised with 21 verification scenarios covering the seven rubric tests,
all nine editable fields, malformed input, file failures, and independent score
arithmetic. IntelliJ itself was not available for an interactive IDE check.

## Submission items

Submit the project ZIP, runnable JAR, sample text file, your actual GitHub repository
link, and the required face-and-screen demonstration link. The ZIP already includes
the JAR and sample file. See `DEMO_GUIDE.md` for exact demonstration inputs and a
recording script. Record the demonstration yourself, upload it to cloud storage,
and enable sharing so your instructor can view it.

If GitHub publication is still pending, upload this project to your DMS repository
and verify that the seven Java source files, `data.txt`, and executable JAR are visible.
Do not substitute a GitHub profile URL for the project repository URL.
