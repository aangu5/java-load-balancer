package com.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Determine role from first arg or from --role=... or ROLE env
        String role = null;
        String[] remaining = new String[0];

        if (args != null && args.length > 0) {
            // Support --role=server or just "server" as first arg
            String first = args[0];
            if (first.startsWith("--role=")) {
                role = first.substring("--role=".length()).trim();
                remaining = java.util.Arrays.copyOfRange(args, 1, args.length);
            } else if (first.equals("-r") || first.equals("--r")) {
                if (args.length > 1) {
                    role = args[1];
                    remaining = java.util.Arrays.copyOfRange(args, 2, args.length);
                }
            } else {
                role = first;
                remaining = java.util.Arrays.copyOfRange(args, 1, args.length);
            }
        }

        if (role == null || role.isBlank()) {
            String envRole = System.getenv("ROLE");
            if (envRole != null && !envRole.isBlank()) {
                role = envRole.trim();
            }
        }

        if (role == null || role.isBlank()) {
            printUsage();
            System.exit(2);
        }

        role = role.toLowerCase();

        try {
            switch (role) {
                case "client":
                    // Delegate to client main
                    com.example.Client.main(remaining);
                    break;
                case "server":
                case "dnaosserver":
                    // Delegate to server main (DNAOSServer or Server class)
                    // try DNAOSServer first then fallback to Server if class exists
                    try {
                        com.example.DNAOSServer.main(remaining);
                    } catch (NoClassDefFoundError | Exception e) {
                        // fallback to Server if available
                        try {
                            com.example.Server.main(remaining);
                        } catch (NoClassDefFoundError | Exception ex) {
                            System.err.println("Server entrypoint not found or failed: " + ex.getMessage());
                            ex.printStackTrace(System.err);
                            System.exit(1);
                        }
                    }
                    break;
                case "gui":
                    com.example.GUI.main(remaining);
                    break;
                default:
                    System.err.println("Unknown role: " + role);
                    printUsage();
                    System.exit(2);
            }
        } catch (Throwable t) {
            System.err.println("Unhandled exception in role '" + role + "': " + t.getMessage());
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar app.jar <role> [args...]\nRoles: client, server, gui\nYou can also pass --role=<role> or set ROLE env var.");
    }
}