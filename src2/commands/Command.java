package commands;

import audio.files.Filters;
import audio.lists.Library;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import top.TopsClass;
import user.User;

@Getter
@Setter
public final class Command {

    private String command;
    private String username;
    private Integer timestamp;

    private String type;
    private Filters filters;
    private Integer itemNumber;
    private Integer seed;
    private Integer playlistId;
    private String playlistName;
    private Integer age;
    private String city;
    private String name;
    private ArrayList<SongInput> songs;
    private ArrayList<EpisodeInput> episodes;
    private String description;
    private Integer releaseYear;
    private String date;
    private Integer price;
    private String nextPage;

    public Command() {
        // Default constructor needed for JSON deserialization
        songs = new ArrayList<>();
        episodes = new ArrayList<>();
    }

    /**
     * Execute the command.
     */
    public void execute(final Command command, final Library myLibrary, final User user,
        final TopsClass topsClass,
        final ArrayNode outputs) {
        switch (command.getCommand()) {
            case "search" -> user.getSearchBar().search(command, myLibrary, user, outputs);
            case "select" -> user.getSearchBar().select(command, outputs, user);
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
            case "switchConnectionStatus" -> user.switchConnectionStatus(command, user, outputs);
            case "getOnlineUsers" -> myLibrary.getOnlineUsers(command, outputs);
            case "addAlbum" -> user.addAlbum(command, myLibrary, user, outputs);
            case "showAlbums" -> user.showAlbums(command, user, outputs);
            case "addUser" -> myLibrary.addUser(command, myLibrary, outputs);
            case "printCurrentPage" -> user.printCurrentPage(command, user, outputs);
            case "addMerch" -> user.addMerch(command, outputs);
            case "addEvent" -> user.addEvent(command, outputs);
            case "getAllUsers" -> myLibrary.getAllUsers(command, outputs);
            case "deleteUser" -> myLibrary.deleteUser(command, outputs);
            case "addPodcast" -> user.addPodcast(command, myLibrary, user, outputs);
            case "addAnnouncement" -> user.addAnnouncement(command, myLibrary, user, outputs);
            case "removeAnnouncement" -> user.removeAnnouncement(command, myLibrary, user, outputs);
            case "showPodcasts" -> user.showPodcasts(command, user, outputs);
            case "removeAlbum" -> user.removeAlbum(command, user, myLibrary, outputs);
            case "changePage" -> user.changePage(command, user, outputs);
            case "removePodcast" -> user.removePodcast(command, user, myLibrary, outputs);
            case "removeEvent" -> user.removeEvent(command, user, myLibrary, outputs);
            case "getTop5Albums" -> topsClass.getTop5Albums(command, myLibrary, outputs);
            case "getTop5Artists" -> topsClass.getTop5Artists(command, myLibrary, outputs);
            default -> {
                // do nothing
            }
        }
    }
}
