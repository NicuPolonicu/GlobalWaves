package main;


import audio.lists.Library;
import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import fileio.input.LibraryInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import top.TopsClass;
import user.User;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {

    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD Call the checker
     *
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
        final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(CheckerConstants.TESTS_PATH
                + "library/library.json"),
            LibraryInput.class);
        ArrayNode outputs = objectMapper.createArrayNode();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Library.reset();

        Library myLibrary = Library.getInstance(library);

        List<Command> commands = objectMapper.readValue(
            new File(CheckerConstants.TESTS_PATH + filePath1), new TypeReference<>() {
            });

        TopsClass topsClass = new TopsClass();
        for (Command command : commands) {
            User user = myLibrary.findUser(command.getUsername());
            if (user == null && command.getUsername() != null) {
                // if the user doesn't exist, create it
                if (command.getCommand().equals("addUser")) {
                    myLibrary.addUser(command, myLibrary, outputs);
                } else {
                    // user doesnt exist
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    objectNode.put("command", command.getCommand());
                    objectNode.put("user", command.getUsername());
                    objectNode.put("timestamp", command.getTimestamp());
                    objectNode.put("message", "The username " + command.getUsername()
                        + " doesn't exist.");
                    outputs.add(objectNode);
                }
            } else {
                command.execute(command, myLibrary, user, topsClass, outputs);
                myLibrary.setLastTimestamp(command.getTimestamp());
            }
        }
        myLibrary.endProgram(outputs);

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        objectWriter.writeValue(new File(filePath2), outputs);
    }
}
