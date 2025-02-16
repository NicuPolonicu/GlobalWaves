package top;

import audio.files.Song;
import audio.lists.Album;
import audio.lists.Library;
import audio.lists.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;

import java.util.ArrayList;
import specialusers.Artist;


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

    /**
     * Prints the top 5 albums. The initial criterion is the sum of likes of all songs in the
     * album. If two albums have an equal amount of likes, the order is lexicographic.
     */
    public void getTop5Albums(final Command command, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode getTop5AlbumsOutput = mapper.createObjectNode();
        getTop5AlbumsOutput.put("command", "getTop5Albums");
        getTop5AlbumsOutput.put("timestamp", command.getTimestamp());
        ArrayList<Album> sortedAlbums = new ArrayList<>(myLibrary.getAlbums());
        // calculate likes for
        for (Album album : sortedAlbums) {
            int likes = 0;
            for (Song song : album.getSongs()) {
                likes += song.getLikes().size();
            }
            album.setLikes(likes);
        }
        sortedAlbums.sort((o1, o2) -> {
            if (o1.getLikes() == o2.getLikes()) {
                // sort alphabetically
                // sort alphabetically
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getLikes() - o1.getLikes();
        });
        ArrayNode albums = mapper.createArrayNode();
        for (int i = 0; i < MAX_NR && i < sortedAlbums.size(); i++) {
            albums.add(sortedAlbums.get(i).getName());
        }
        getTop5AlbumsOutput.putPOJO("result", albums);
        outputs.add(getTop5AlbumsOutput);
    }

    /**
     * Prints the top 5 artists. The initial criterion is the sum of likes of all songs in the
     * artist's albums. If two artists have an equal amount of likes, the order in which they're
     * found in the library is used. (so basically after when they were added)
     */
    public void getTop5Artists(final Command command, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode getTop5ArtistsOutput = mapper.createObjectNode();
        getTop5ArtistsOutput.put("command", "getTop5Artists");
        getTop5ArtistsOutput.put("timestamp", command.getTimestamp());
        ArrayList<Artist> sortedArtists = new ArrayList<>(myLibrary.getArtists());
        for (Artist artist : sortedArtists) {
            int likes = 0;
            for (Album album : artist.getAlbums()) {
                for (Song song : album.getSongs()) {
                    likes += song.getLikes().size();
                }
            }
            artist.setLikes(likes);
        }
        sortedArtists.sort((o1, o2) -> {
                if (o1.getLikes() == o2.getLikes()) {
                    return myLibrary.getArtists().indexOf(o1) - myLibrary.getArtists().indexOf(o2);
                }
                return o2.getLikes() - o1.getLikes();
            }
        );
        ArrayNode artists = mapper.createArrayNode();
        for (int i = 0; i < MAX_NR && i < sortedArtists.size(); i++) {
            artists.add(sortedArtists.get(i).getUsername());
        }
        getTop5ArtistsOutput.putPOJO("result", artists);
        outputs.add(getTop5ArtistsOutput);
    }
}
