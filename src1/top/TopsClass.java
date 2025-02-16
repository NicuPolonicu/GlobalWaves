package top;

import audio.files.Song;
import audio.lists.Library;
import audio.lists.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;

import java.util.ArrayList;


public final class TopsClass {

    private static final int MAX_NR = 5;

    /**
     * Prints the 5 most liked songs from the Library. If two songs in the top have an equal amount
     * of likes, the one with the lowest index in the library will be printed first.
     *
     * @param command   for the command timestamp
     * @param myLibrary for the songs
     * @param outputs   for adding the JSON-format output to the other outputs
     */
    public void getTop5Songs(final Command command, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode getTop5SongsOutput = mapper.createObjectNode();
        getTop5SongsOutput.put("command", "getTop5Songs");
        getTop5SongsOutput.put("timestamp", command.getTimestamp());
        // top 5 songs
        if (myLibrary.getSongs().isEmpty()) {
            getTop5SongsOutput.put("message", "No songs found.");
            outputs.add(getTop5SongsOutput);
            return;
        }
        // take a list of all songs and sort it (first after likes, then after order in library)
        ArrayList<Song> sortedSongs = new ArrayList<>();
        for (Song song : myLibrary.getSongs()) {
            sortedSongs.add(song);
        }
        sortedSongs.sort((o1, o2) -> {
            if (o1.getLikes().size() == o2.getLikes().size()) {
                return myLibrary.getSongs().indexOf(o1) - myLibrary.getSongs().indexOf(o2);
            }
            return o2.getLikes().size() - o1.getLikes().size();
        });
        ArrayNode songs = mapper.createArrayNode();
        for (int i = 0; i < MAX_NR && i < sortedSongs.size(); i++) {
            songs.add(sortedSongs.get(i).getName());
        }
        getTop5SongsOutput.putPOJO("result", songs);
        outputs.add(getTop5SongsOutput);
    }

    /**
     * Prints the top 5 most followed playlists. If two playlists have an equal amount of followers,
     * the one created the earliest (lowest timestamp) will be printed first.
     *
     * @param command   for the command timestamp
     * @param myLibrary for the array list of playlists
     * @param outputs   for adding the output
     */
    public void getTop5Playlists(final Command command, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode getTop5PlaylistsOutput = mapper.createObjectNode();
        getTop5PlaylistsOutput.put("command", "getTop5Playlists");
        getTop5PlaylistsOutput.put("timestamp", command.getTimestamp());
        ArrayList<Playlist> sortedPlaylists = new ArrayList<>();
        for (Playlist playlist : myLibrary.getPlaylists()) {
            sortedPlaylists.add(playlist);
        }
        sortedPlaylists.sort((o1, o2) -> {
            if (o1.getFollowers().size() == o2.getFollowers().size()) {
                return myLibrary.getPlaylists().indexOf(o1) - myLibrary.getPlaylists().indexOf(o2);
            }
            return o2.getFollowers().size() - o1.getFollowers().size();
        });
        ArrayNode playlists = mapper.createArrayNode();
        for (int i = 0; i < MAX_NR && i < sortedPlaylists.size(); i++) {
            playlists.add(sortedPlaylists.get(i).getName());
        }
        getTop5PlaylistsOutput.putPOJO("result", playlists);
        outputs.add(getTop5PlaylistsOutput);
    }

}
