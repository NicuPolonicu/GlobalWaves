package user;

import audio.files.Song;
import audio.item.AudioItem;
import audio.lists.Library;
import audio.lists.Podcast;
import audioenum.AudioEnum;
import com.fasterxml.jackson.databind.node.ArrayNode;
import commands.Command;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;
import searchbar.SearchBar;
import player.Player;
import audio.lists.PodcastProgress;
import java.util.ArrayList;
import audio.lists.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Getter
@Setter
public final class User {

    private final Library myLibrary;
    private final String username;
    private final int age;
    private final String city;
    private final SearchBar searchBar;
    private final Player player;
    private final ArrayList<PodcastProgress> podcastProgress;
    private final ArrayList<Playlist> playlists;
    private final ArrayList<Song> likedSongs;
    private final ArrayList<Playlist> followedPlaylists;

    public User(final UserInput user, final Library myLibrary) {
        this.myLibrary = myLibrary;
        username = user.getUsername();
        age = user.getAge();
        city = user.getCity();
        podcastProgress = new ArrayList<>();
        for (Podcast podcast : myLibrary.getPodcasts()) {
            podcastProgress.add(new PodcastProgress(podcast));
        }
        searchBar = new SearchBar();
        player = new Player();
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
    }

    /**
     * Create a playlist.
     *
     * @param command   is where we find the new playlist's name
     * @param myLibrary we also have to add the playlist in the library's "list of playlists"
     */
    public void createPlaylist(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        for (Playlist playlist : myLibrary.getPlaylists()) {
            if (playlist.getName().equals(command.getPlaylistName())) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode createPlaylistOutput = mapper.createObjectNode();
                createPlaylistOutput.put("command", "createPlaylist");
                createPlaylistOutput.put("user", command.getUsername());
                createPlaylistOutput.put("timestamp", command.getTimestamp());
                createPlaylistOutput.put("message",
                    "A playlist with the same name already exists.");
                outputs.add(createPlaylistOutput);
                return;
            }
        }
        Playlist newPlaylist = new Playlist(command.getPlaylistName(), user,
            command.getTimestamp());
        myLibrary.getPlaylists().add(newPlaylist);
        user.getPlaylists().add(newPlaylist);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode createPlaylistOutput = mapper.createObjectNode();
        createPlaylistOutput.put("command", "createPlaylist");
        createPlaylistOutput.put("user", command.getUsername());
        createPlaylistOutput.put("timestamp", command.getTimestamp());
        createPlaylistOutput.put("message", "Playlist created successfully.");
        outputs.add(createPlaylistOutput);

    }

    /**
     * Like the song the user is currently listening to.
     */
    public void like(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode likeOutput = mapper.createObjectNode();
        likeOutput.put("command", "like");
        likeOutput.put("user", command.getUsername());
        likeOutput.put("timestamp", command.getTimestamp());
        player.update(command.getTimestamp(), user);
        AudioItem queue = player.getQueue();
        if (queue == null) {
            likeOutput.put("message", "Please load a source before liking or unliking.");
            outputs.add(likeOutput);
            return;
        }
        if (queue.getType() == AudioEnum.PODCAST) {
            likeOutput.put("message", "The loaded source is not a song.");
            outputs.add(likeOutput);
        } else if (((Song) player.getCurrentFile()).getLikes().contains(user)) {
            ((Song) player.getCurrentFile()).removeLike(user);
            likedSongs.remove(((Song) player.getCurrentFile()));
            likeOutput.put("message", "Unlike registered successfully.");
            outputs.add(likeOutput);
        } else {
            ((Song) player.getCurrentFile()).addLike(user);
            likedSongs.add(((Song) player.getCurrentFile()));
            likeOutput.put("message", "Like registered successfully.");
            outputs.add(likeOutput);
        }
    }

    /**
     * Show all playlists of the user
     *
     * @param command for the timestamp in the output
     * @param user    whose user's playlists we want
     * @param outputs for appending the output to the other outputs
     */
    public void showPlaylists(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode showPlaylistsOutput = mapper.createObjectNode();
        showPlaylistsOutput.put("command", "showPlaylists");
        showPlaylistsOutput.put("user", command.getUsername());
        showPlaylistsOutput.put("timestamp", command.getTimestamp());
        if (user.getPlaylists().isEmpty()) {
            showPlaylistsOutput.put("message", "No playlists found.");
            outputs.add(showPlaylistsOutput);
            return;
        }
        ArrayNode playlists = mapper.createArrayNode();
        for (Playlist playlist : user.getPlaylists()) {
            ObjectNode playlistNode = mapper.createObjectNode();
            playlistNode.put("name", playlist.getName());
            ArrayNode songs = mapper.createArrayNode();
            for (Song song : playlist.getSongs()) {
                songs.add(song.getName());
            }
            playlistNode.putPOJO("songs", songs);
            if (playlist.getIsPrivate() == 1) {
                playlistNode.put("visibility", "private");
            } else {
                playlistNode.put("visibility", "public");
            }
            playlistNode.put("followers", playlist.getFollowers().size());
            playlists.add(playlistNode);
        }
        showPlaylistsOutput.putPOJO("result", playlists);
        outputs.add(showPlaylistsOutput);
    }

    /**
     * Show all songs liked by user
     *
     * @param command for output timestamp
     * @param user    whose user's liked songs we want
     * @param outputs to append output
     */
    public void showPreferredSongs(final Command command, final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode showPreferredSongsOutput = mapper.createObjectNode();
        showPreferredSongsOutput.put("command", "showPreferredSongs");
        showPreferredSongsOutput.put("user", command.getUsername());
        showPreferredSongsOutput.put("timestamp", command.getTimestamp());
        ArrayNode songs = mapper.createArrayNode();
        if (user.getLikedSongs().isEmpty()) {
            showPreferredSongsOutput.putPOJO("result", songs);
            outputs.add(showPreferredSongsOutput);
            return;
        }
        for (Song song : user.getLikedSongs()) {
            songs.add(song.getName());
        }
        showPreferredSongsOutput.putPOJO("result", songs);
        outputs.add(showPreferredSongsOutput);
    }

    /**
     * Follow the playlist loaded in the user's Player.
     *
     * @param command for the output timestamp and to update the Player
     */
    public void follow(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode followOutput = mapper.createObjectNode();
        followOutput.put("command", "follow");
        followOutput.put("user", command.getUsername());
        followOutput.put("timestamp", command.getTimestamp());
        user.getPlayer().update(command.getTimestamp(), user);
        if (user.getSearchBar().getSelected() == null) {
            followOutput.put("message", "Please select a source before following or unfollowing.");
            outputs.add(followOutput);
            return;
        }
        if (user.getSearchBar().getSelected().getType() != AudioEnum.PLAYLIST) {
            followOutput.put("message", "The selected source is not a playlist.");
            outputs.add(followOutput);
            return;
        }
        Playlist playlist = (Playlist) user.getSearchBar().getSelected();
        if (playlist.getOwner().equals(user)) {
            followOutput.put("message", "You cannot follow or unfollow your own playlist.");
            outputs.add(followOutput);
            return;
        }
        if (playlist.getFollowers().contains(user)) {
            playlist.removeFollower(user);
            user.getFollowedPlaylists().remove(playlist);
            followOutput.put("message", "Playlist unfollowed successfully.");
            outputs.add(followOutput);
        } else {
            playlist.addFollower(user);
            user.getFollowedPlaylists().add(playlist);
            followOutput.put("message", "Playlist followed successfully.");
            outputs.add(followOutput);
        }

    }

    /**
     * Switch visiblity of the selected playlist. (private => public or public => private)
     */
    public void switchVisibility(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode switchVisibilityOutput = mapper.createObjectNode();
        switchVisibilityOutput.put("command", "switchVisibility");
        switchVisibilityOutput.put("user", command.getUsername());
        switchVisibilityOutput.put("timestamp", command.getTimestamp());
        if (command.getPlaylistId() > user.getPlaylists().size()) {
            switchVisibilityOutput.put("message", "The specified playlist ID is too high.");
            outputs.add(switchVisibilityOutput);
            return;
        }
        Playlist playlist = user.getPlaylists().get(command.getPlaylistId() - 1);
        if (playlist.getIsPrivate() == 1) {
            playlist.setIsPrivate(0);
            switchVisibilityOutput.put("message",
                "Visibility status updated successfully to public.");
            outputs.add(switchVisibilityOutput);
        } else {
            playlist.setIsPrivate(1);
            switchVisibilityOutput.put("message",
                "Visibility status updated successfully to private.");
            outputs.add(switchVisibilityOutput);
        }
    }
}
