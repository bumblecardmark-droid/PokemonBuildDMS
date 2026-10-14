package dms;

import java.util.Scanner;

/**
 * Adriel Portillo Perez | CEN 3024C | October 13, 2026
 * Class: Main
 * Launches a console Data Management System for importing, displaying, adding,
 * removing, and editing PokemonBuild records and calculating team readiness.
 * All application logic uses instances. Java requires this one static entry point.
 */
public class Main {
    /**
     * main constructs the planned layers and starts the console application.
     * @param args command-line arguments, not used by this menu-driven program
     * @return nothing, as required by the Java application entry-point signature
     */
    public static void main(String[] args) {
        BuildValidator validator = new BuildValidator();
        PokemonBuildRepository repository = new PokemonBuildRepository(validator);
        BuildFileService fileService = new BuildFileService(validator);
        TeamReadinessCalculator calculator = new TeamReadinessCalculator(validator);
        PokemonBuildApp app = new PokemonBuildApp(repository, fileService,
                calculator, new Scanner(System.in, "UTF-8"));
        app.run();
        app.close();
    }
}
