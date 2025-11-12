package com.example;

public class Main {
    public static void main(String[] args) {
        String role = null;
        String[] remaining = new String[0];
        if (args != null && args.length > 0) {
            String first = args[0];
            if (first.startsWith("--role=")) {
                role = first.substring("--role=".length()).trim();
                remaining = java.util.Arrays.copyOfRange(args, 1, args.length);
            } else {
                role = first;
                remaining = java.util.Arrays.copyOfRange(args, 1, args.length);
            }
        }
        if (role == null || role.isBlank()) {
            String envRole = System.getenv("ROLE");
            if (envRole != null && !envRole.isBlank()) role = envRole;
        }
        if (role == null || role.isBlank()) {
            System.err.println("Usage: java -jar merged-app.jar <role> [args...]\nRoles: client, server, gui");
            System.exit(2);
        }
        role = role.toLowerCase();
        try {
            switch (role) {
                case "client":
                    com.example.client.Node.main(remaining);
                    break;
                case "server":
                    com.example.server.DNAOSServer.main(remaining);
                    break;
                case "gui":
                    com.example.gui.Interface.main(remaining);
                    break;
                default:
                    System.err.println("Unknown role: " + role);
                    System.exit(2);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

