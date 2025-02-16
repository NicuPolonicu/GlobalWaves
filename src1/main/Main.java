package main;

import com.fasterxml.jackson.annotation.JsonInclude;
import commands.Command;
import audio.lists.Library;
import fileio.input.LibraryInput;
import top.TopsClass;
import user.User;

import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.List;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {

    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

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
     * @param filePathInput  for input file
     * @param filePathOutput for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePathInput,
        final String filePathOutput) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(LIBRARY_PATH), LibraryInput.class);

        ArrayNode outputs = objectMapper.createArrayNode();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Library myLibrary = new Library(library);

        // TODO add your implementation

        // read all the commands from input file

        List<Command> commands = objectMapper.readValue(
            new File(CheckerConstants.TESTS_PATH + filePathInput), new TypeReference<>() {
            });

        TopsClass topsClass = new TopsClass();
        for (Command command : commands) {
            User user = myLibrary.findUser(command.getUsername());
            switch (command.getCommand()) {
                case "search" -> user.getSearchBar().search(command, myLibrary, user, outputs);
                case "select" -> user.getSearchBar().select(command, outputs);
                case "load" -> user.getPlayer().load(command, user, outputs);
                case "status" -> user.getPlayer().status(command, user, outputs);
                case "playPause" -> user.getPlayer().playPause(command, user, outputs);
                case "createPlaylist" -> user.createPlaylist(command, myLibrary, user, outputs);
                case "addRemoveInPlaylist" ->
                    user.getPlayer().addRemoveInPlaylist(command, user, outputs);
                case "like" -> user.like(command, user, outputs);
                case "showPlaylists" -> user.showPlaylists(command, user, outputs);
                case "showPreferredSongs" -> user.showPreferredSongs(command, user, outputs);
                case "follow" -> user.follow(command, user, outputs);
                case "switchVisibility" -> user.switchVisibility(command, user, outputs);
                case "repeat" -> user.getPlayer().repeat(command, user, outputs);
                case "shuffle" -> user.getPlayer().shuffle(command, user, outputs);
                case "next" -> user.getPlayer().next(command, user, outputs);
                case "prev" -> user.getPlayer().prev(command, user, outputs);
                case "forward" -> user.getPlayer().forward(command, user, outputs);
                case "backward" -> user.getPlayer().backward(command, user, outputs);
                case "getTop5Songs" -> topsClass.getTop5Songs(command, myLibrary, outputs);
                case "getTop5Playlists" -> topsClass.getTop5Playlists(command, myLibrary, outputs);
                default -> {
                    return;
                }
            }
        }

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePathOutput), outputs);
    }
}
