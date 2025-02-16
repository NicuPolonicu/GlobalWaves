package searchbar;

import audio.lists.Library;
import audio.lists.Podcast;
import audio.lists.AudioList;
import audio.lists.Playlist;
import audioenum.AudioEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import commands.Command;
import java.util.ArrayList;
import audio.item.AudioItem;
import lombok.Getter;
import lombok.Setter;
import user.User;
import audio.files.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import audio.files.AudioFile;

@Getter
@Setter
public final class SearchBar {

    private static final int MAX_SIZE = 5;
    private ArrayList<AudioItem> searchResults;
    private AudioItem selected;

    private int searched = 0;


    /**
     * Search for playlists/songs/podcasts with the given filter(s). NOTE: The filters must be
     * provided through the command object.
     */
    public void search(final Command command, final Library library, final User user,
        final ArrayNode outputs) {
        searchResults = new ArrayList<>();
        if (user.getPlayer().getQueue() != null
            && user.getPlayer().getQueue().getType() == AudioEnum.PODCAST) {
            user.getPlayer().update(command.getTimestamp(), user);
        }
        user.getPlayer().setQueue(null);
        user.getPlayer().setCurrentFile(null);
        user.getPlayer().setPlaying(false);
        user.getPlayer().setRepeatMode(0);
        user.getPlayer().setShuffle(false);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode searchOutput = mapper.createObjectNode();
        searchOutput.put("command", "search");
        searchOutput.put("user", command.getUsername());
        searchOutput.put("timestamp", command.getTimestamp());
        switch (command.getType()) {
            case "song" -> {
                int countFilters = 0;
                if (command.getFilters().getName() != null) {
                    countFilters++;
                }
                if (command.getFilters().getArtist() != null) {
                    countFilters++;
                }
                if (command.getFilters().getAlbum() != null) {
                    countFilters++;
                }
                if (command.getFilters().getTags() != null) {
                    countFilters++;
                }
                if (command.getFilters().getLyrics() != null) {
                    countFilters++;
                }
                if (command.getFilters().getGenre() != null) {
                    countFilters++;
                }
                if (command.getFilters().getReleaseYear() != null) {
                    countFilters++;
                }
                for (Song song : library.getSongs()) {
                    int nrMatches = 0;
                    if (command.getFilters().getName() != null) {
                        if (song.getName().startsWith(command.getFilters().getName())) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getArtist() != null) {

                        if (song.getArtist().contentEquals(command.getFilters().getArtist())) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getAlbum() != null) {
                        if (song.getAlbum().equals(command.getFilters().getAlbum())) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getTags() != null) {
                        int nrFittingTags = 0;
                        for (String tag : command.getFilters().getTags()) {
                            if (song.getTags().contains(tag)) {
                                nrFittingTags++;
                            }
                        }
                        if (nrFittingTags == command.getFilters().getTags().size()) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getLyrics() != null) {
                        if (song.getLyrics().toLowerCase()
                            .contains(command.getFilters().getLyrics().toLowerCase())) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getGenre() != null) {
                        String genre = command.getFilters().getGenre().substring(0, 1).toUpperCase()
                            + command.getFilters().getGenre().substring(1);
                        if (song.getGenre().equals(genre)) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getReleaseYear() != null) {
                        int releaseYear = Integer.parseInt(
                            command.getFilters().getReleaseYear().substring(1));
                        if (command.getFilters().getReleaseYear().charAt(0) == '<') {
                            if (releaseYear > song.getReleaseYear()) {
                                nrMatches++;
                            }
                        } else {
                            if (releaseYear < song.getReleaseYear()) {
                                nrMatches++;
                            }
                        }
                    }
                    if (nrMatches == countFilters && searchResults.size() < MAX_SIZE) {
                        searchResults.add(song);
                    }
                }
                if (searchResults.isEmpty()) {

                    searchOutput.put("message", "Search returned 0 results");
                    searchOutput.putPOJO("results", searchResults);
                    outputs.add(searchOutput);
                } else {
                    ArrayNode resultNames = mapper.createArrayNode();

                    for (AudioItem item : searchResults) {
                        resultNames.add(((Song) item).getName());
                    }
                    searchOutput.put("message",
                        "Search returned " + searchResults.size() + " results");
                    searchOutput.put("results", resultNames);
                    outputs.add(searchOutput);
                }
            }
            case "podcast" -> {
                int countFilters = 0;
                if (command.getFilters().getName() != null) {
                    countFilters++;
                }
                if (command.getFilters().getOwner() != null) {
                    countFilters++;
                }
                for (Podcast podcast : library.getPodcasts()) {
                    int nrMatches = 0;
                    if (command.getFilters().getName() != null) {
                        if (podcast.getName().startsWith(command.getFilters().getName())) {
                            nrMatches++;
                        }
                    }
                    if (command.getFilters().getOwner() != null) {
                        if (podcast.getOwner().equals(command.getFilters().getOwner())) {
                            nrMatches++;
                        }
                    }
                    if (nrMatches == countFilters && searchResults.size() < MAX_SIZE) {
                        searchResults.add(podcast);
                    }
                }
                if (searchResults.isEmpty()) {
                    searchOutput.put("message", "Search returned 0 results");
                    searchOutput.putPOJO("results", searchResults);
                    outputs.add(searchOutput);
                } else {
                    ArrayNode resultNames = mapper.createArrayNode();
                    for (AudioItem item : searchResults) {
                        resultNames.add(((Podcast) item).getName());
                    }
                    searchOutput.put("message",
                        "Search returned " + searchResults.size() + " results");
                    searchOutput.put("results", resultNames);

                    outputs.add(searchOutput);
                }
            }
            case "playlist" -> {
                if (command.getFilters().getName() != null) {
                    for (Playlist playlist : user.getPlaylists()) {
                        if (playlist.getName().startsWith(command.getFilters().getName())
                            && searchResults.size() < MAX_SIZE) {
                            searchResults.add(playlist);
                        }
                    }
                    for (Playlist playlist : library.getPlaylists()) {
                        if (playlist.getName().startsWith(command.getFilters().getName())
                            && searchResults.size() < MAX_SIZE && !playlist.getOwner().equals(user)
                            && playlist.getIsPrivate() == 0) {
                            searchResults.add(playlist);
                        }
                    }
                }
                if (command.getFilters().getOwner() != null) {
                    for (Playlist playlist : user.getPlaylists()) {
                        if (playlist.getOwner().getUsername()
                            .equals(command.getFilters().getOwner())
                            && searchResults.size() < MAX_SIZE) {
                            searchResults.add(playlist);
                        }
                    }
                    for (Playlist playlist : library.getPlaylists()) {
                        if (playlist.getOwner().getUsername()
                            .equals(command.getFilters().getOwner()) && !playlist.getOwner()
                            .equals(user)
                            && searchResults.size() < MAX_SIZE && playlist.getIsPrivate() == 0) {
                            searchResults.add(playlist);
                        }
                    }
                }
                if (searchResults.isEmpty()) {
                    searchOutput.put("message", "Search returned 0 results");
                    ArrayList<Integer> emptyVec = new ArrayList<Integer>();
                    searchOutput.putPOJO("results", emptyVec);
                    outputs.add(searchOutput);
                } else {
                    ArrayNode resultNames = mapper.createArrayNode();

                    for (AudioItem item : searchResults) {
                        resultNames.add(((Playlist) item).getName());
                    }
                    searchOutput.put("message",
                        "Search returned " + searchResults.size() + " results");
                    searchOutput.put("results", resultNames);
                    outputs.add(searchOutput);
                }

            }
            default -> {
                return;
            }
        }
        searched = 1;
    }

    /**
     * From the search results, select an item.
     */
    public void select(final Command command, final ArrayNode outputs) {
        if (searchResults.isEmpty() && searched == 0) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode selectOutput = mapper.createObjectNode();
            selectOutput.put("command", "select");
            selectOutput.put("user", command.getUsername());
            selectOutput.put("timestamp", command.getTimestamp());
            selectOutput.put("message", "Please conduct a search before making a selection.");
            outputs.add(selectOutput);
            selected = null;
            return;
        }
        if (command.getItemNumber() > searchResults.size()) {

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode selectOutput = mapper.createObjectNode();
            selectOutput.put("command", "select");
            selectOutput.put("user", command.getUsername());
            selectOutput.put("timestamp", command.getTimestamp());
            selectOutput.put("message", "The selected ID is too high.");
            selected = null;
            outputs.add(selectOutput);
        } else {
            selected = searchResults.get(command.getItemNumber() - 1);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode selectOutput = mapper.createObjectNode();
            selectOutput.put("command", "select");
            selectOutput.put("user", command.getUsername());
            selectOutput.put("timestamp", command.getTimestamp());
            if (selected.getType() == AudioEnum.SONG) {
                selectOutput.put("message",
                    "Successfully selected " + ((AudioFile) selected).getName() + ".");
            } else {
                selectOutput.put("message",
                    "Successfully selected " + ((AudioList) selected).getName() + ".");
            }
            outputs.add(selectOutput);
        }
        searched = 0;
    }
}
