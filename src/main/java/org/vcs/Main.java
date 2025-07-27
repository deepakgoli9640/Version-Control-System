package org.vcs;

import org.vcs.Groot;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length == 0) {
            printUsage();
            return;
        }

        Groot groot = new Groot(Path.of("."));
        String command = args[0];

        switch (command) {
            case "add":
                if (args.length < 2) {
                    System.out.println("❌ Please specify at least one file to add.");
                } else {
                    for (int i = 1; i < args.length; i++) {
                        groot.add(args[i]);
                        System.out.println("✅ Added: " + args[i]);
                    }
                }
                break;

            case "commit":
                if (args.length < 2) {
                    System.out.println("❌ Please provide a commit message.");
                } else {
                    String message = args[1];
                    groot.commit(message);
                    System.out.println("✅ Commit complete: " + message);
                }
                break;

            case "log":
                groot.log();
                break;

            default:
                System.out.println("❌ Unknown command: " + command);
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  groot add <file1> <file2> ...");
        System.out.println("  groot commit \"your message\"");
        System.out.println("  groot log");
    }
}
