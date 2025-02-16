package user;

import artistentities.Listens;
import artistentities.Merch;
import audio.files.Song;
import audio.item.AudioItem;
import audio.lists.Library;
import audio.lists.Podcast;
import enums.AudioEnum;
import com.fasterxml.jackson.databind.node.ArrayNode;
import commands.Command;
import enums.Page;
import enums.UserType;
import fileio.input.UserInput;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import searchbar.SearchBar;
import player.Player;
import audio.lists.PodcastProgress;
import java.util.ArrayList;
import audio.lists.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.Status;
import specialusers.Artist;
import specialusers.Host;

@Getter
@Setter
public class User implements Observer {

    private static final int MAX_PRINTED = 5;
    private static final int MIN_LISTENED = 30;
    private static final int TOP_GENRE_SIZE = 3;
    private final Library library;
    private final String username;
    private final int age;
    private final String city;
    private final SearchBar searchBar;
    private final Player player;
    private final ArrayList<PodcastProgress> podcastProgress;
    private final ArrayList<Playlist> playlists;
    private final ArrayList<Song> likedSongs;
    private final ArrayList<Playlist> followedPlaylists;
    private Status status = Status.ONLINE;
    private UserType type;
    private Page currentPage;
    private String pageOwner = "";
    private ArrayList<Listens> artistListens = new ArrayList<>();
    private ArrayList<Listens> genreListens = new ArrayList<>();
    private ArrayList<Listens> songListens = new ArrayList<>();
    private ArrayList<Listens> albumListens = new ArrayList<>();
    private ArrayList<Listens> episodeListens = new ArrayList<>();
    private boolean isPremium = false;
    private ArrayList<User> subscribers = new ArrayList<>();
    private ArrayList<Notification> notifications = new ArrayList<>();
    private ArrayList<Merch> boughtMerch = new ArrayList<>();
    private Song songRecommendation;
    private Playlist playlistRecommendation;
    private ArrayList<PageDetails> prevPages = new ArrayList<>();
    private ArrayList<PageDetails> nextPages = new ArrayList<>();
    private String lastRecommendation = null;

    public User(final UserInput user, final Library myLibrary) {
        library = myLibrary;
        username = user.getUsername();
        age = user.getAge();
        city = user.getCity();
        podcastProgress = new ArrayList<>();
        for (Podcast podcast : myLibrary.getPodcasts()) {
            podcastProgress.add(new PodcastProgress(podcast));
        }
        searchBar = new SearchBar();
        player = new Player(myLibrary);
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
        type = UserType.NORMAL;
        currentPage = Page.HOME;
        artistListens = new ArrayList<>();
        genreListens = new ArrayList<>();
        songListens = new ArrayList<>();
        albumListens = new ArrayList<>();
        episodeListens = new ArrayList<>();
    }

    public User(final String username, final int age, final String city, final Library myLibrary) {
        library = myLibrary;
        this.username = username;
        this.age = age;
        this.city = city;
        podcastProgress = new ArrayList<>();
        for (Podcast podcast : myLibrary.getPodcasts()) {
            podcastProgress.add(new PodcastProgress(podcast));
        }
        searchBar = new SearchBar();
        player = new Player(myLibrary);
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
        type = UserType.NORMAL;
        currentPage = Page.HOME;
        artistListens = new ArrayList<>();
        genreListens = new ArrayList<>();
        songListens = new ArrayList<>();
        albumListens = new ArrayList<>();
        episodeListens = new ArrayList<>();
    }

    public User(final String username, final Integer age, final String city,
        final UserType userType) {
        this.username = username;
        this.age = age;
        this.city = city;
        this.type = userType;
        library = null;
        podcastProgress = null;
        searchBar = null;
        player = null;
        playlists = null;
        likedSongs = null;
        followedPlaylists = null;
        currentPage = null;
        artistListens = new ArrayList<>();
        songListens = new ArrayList<>();
        albumListens = new ArrayList<>();

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
        if (status == Status.OFFLINE) {
            likeOutput.put("message", username + " is offline.");
            outputs.add(likeOutput);
            return;
        }
        player.update(command.getTimestamp(), user, library);
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
        if (user.getStatus() == Status.OFFLINE) {
            followOutput.put("message", "User " + username + " is not online.");
            outputs.add(followOutput);
            return;
        }
        user.getPlayer().update(command.getTimestamp(), user, library);
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

    /**
     * Make user go online/offline. (Only for normal users!)
     */
    public void switchConnectionStatus(final Command command, final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode switchConnectionStatusOutput = mapper.createObjectNode();
        switchConnectionStatusOutput.put("command", "switchConnectionStatus");
        switchConnectionStatusOutput.put("user", command.getUsername());
        switchConnectionStatusOutput.put("timestamp", command.getTimestamp());
        if (user.getType() != UserType.NORMAL) {
            switchConnectionStatusOutput.put("message",
                command.getUsername() + " is not a normal user.");
            outputs.add(switchConnectionStatusOutput);
            return;
        }
        user.getPlayer().update(command.getTimestamp(), user, library);
        if (user.getStatus() == Status.ONLINE) {
            user.setStatus(Status.OFFLINE);
        } else {
            user.setStatus(Status.ONLINE);
        }
        switchConnectionStatusOutput.put("message",
            user.getUsername() + " has changed status successfully.");
        outputs.add(switchConnectionStatusOutput);
    }

    /**
     * Add a new album. (Only for artists!)
     */
    public void addAlbum(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        if (type != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode addAlbumOutput = mapper.createObjectNode();
            addAlbumOutput.put("command", "addAlbum");
            addAlbumOutput.put("user", command.getUsername());
            addAlbumOutput.put("timestamp", command.getTimestamp());
            addAlbumOutput.put("message", command.getUsername() + " is not an artist.");
            outputs.add(addAlbumOutput);
            return;
        }
        // go to function in Artist
        this.addAlbumReal(command, myLibrary, user, outputs);

    }

    /**
     * Show all albums of the artist. (Only for artists, in which case we go to the overriden
     * function in Artist)
     */
    public void showAlbums(final Command command, final User user, final ArrayNode outputs) {
        if (type != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode showAlbumsOutput = mapper.createObjectNode();
            showAlbumsOutput.put("command", "showAlbums");
            showAlbumsOutput.put("user", command.getUsername());
            showAlbumsOutput.put("timestamp", command.getTimestamp());
            showAlbumsOutput.put("message", command.getUsername() + " is not an artist.");
            outputs.add(showAlbumsOutput);
            return;
        }
        this.showAlbumsReal(command, user, outputs);
    }

    /**
     * Print the current page. (Only for normal users!)
     */
    public void printCurrentPage(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode printCurrentPageOutput = mapper.createObjectNode();
        printCurrentPageOutput.put("command", "printCurrentPage");
        printCurrentPageOutput.put("user", command.getUsername());
        printCurrentPageOutput.put("timestamp", command.getTimestamp());
        user.getPlayer().update(command.getTimestamp(), user, library);
        if (user.getStatus() == Status.OFFLINE) {
            printCurrentPageOutput.put("message", username + " is offline.");
            outputs.add(printCurrentPageOutput);
            return;
        }
        StringBuilder pageContent = new StringBuilder();
        switch (currentPage) {
            case HOME -> {
                pageContent.append("Liked songs:\n\t[");
                // make copy of liked songs
                ArrayList<Song> likedSongsCopy = new ArrayList<>(user.getLikedSongs());
                // sort liked songs by name
                likedSongsCopy.sort((o1, o2) -> {
                    if (o1.getLikes().size() == o2.getLikes().size()) {
                        // sort by index
                        return user.getLikedSongs().indexOf(o1) - user.getLikedSongs().indexOf(o2);
                    }
                    return o2.getLikes().size() - o1.getLikes().size();
                });
                for (int i = 0; i < MAX_PRINTED; i++) {
                    if (i < likedSongsCopy.size()) {
                        if (i > 0) {
                            pageContent.append(", ");
                        }
                        pageContent.append(likedSongsCopy.get(i).getName());

                    }
                }
                pageContent.append("]\n\nFollowed playlists:\n\t[");
                for (int i = 0; i < MAX_PRINTED; i++) {
                    if (i < followedPlaylists.size()) {
                        if (i > 0) {
                            pageContent.append(", ");

                        }
                        pageContent.append(followedPlaylists.get(i).getName());
                    }
                }
                pageContent.append("]");
                pageContent.append("\n\nSong recommendations:\n\t[");
                if (songRecommendation != null) {
                    pageContent.append(songRecommendation.getName());
                }
                pageContent.append("]");
                pageContent.append("\n\nPlaylists recommendations:\n\t[");
                if (playlistRecommendation != null) {
                    pageContent.append(playlistRecommendation.getName());
                }
                pageContent.append("]");
                printCurrentPageOutput.put("message", pageContent.toString());
                outputs.add(printCurrentPageOutput);
            }
            case ARTIST -> {
                //find artist with that user
                /* if (player.getQueue() == null) {
                    printCurrentPageOutput.put("message",
                        "Please load a source before printing the current page.");
                    outputs.add(printCurrentPageOutput);
                    return;
                } */
                //Artist artist = library.findArtist(((Song) player.getCurrentFile()).getArtist());
                Artist artist = (Artist) library.findUser(pageOwner);
                //print, in order, albums, merch, events
                pageContent = pageContent.append("Albums:\n\t[");
                // ALL albums/merch/events must be printed
                for (int i = 0; i < artist.getAlbums().size(); i++) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(artist.getAlbums().get(i).getName());
                }
                pageContent = pageContent.append("]\n\nMerch:\n\t[");
                for (int i = 0; i < artist.getMerches().size(); i++) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(artist.getMerches().get(i).getName());
                    pageContent = pageContent.append(" - ");
                    pageContent = pageContent.append(artist.getMerches().get(i).getPrice());
                    pageContent = pageContent.append(":\n\t");
                    pageContent = pageContent.append(artist.getMerches().get(i).getDescription());
                }
                pageContent = pageContent.append("]\n\nEvents:\n\t[");
                for (int i = 0; i < artist.getEvents().size(); i++) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(artist.getEvents().get(i).getName());
                    pageContent = pageContent.append(" - ");
                    pageContent = pageContent.append(artist.getEvents().get(i).getDate());
                    pageContent = pageContent.append(":\n\t");
                    pageContent = pageContent.append(artist.getEvents().get(i).getDescription());
                }
                pageContent = pageContent.append("]");
                printCurrentPageOutput.put("message", pageContent.toString());
                outputs.add(printCurrentPageOutput);
            }
            case HOST -> {
                //find host with that user
                /* if (player.getQueue() == null) {
                    printCurrentPageOutput.put("message",
                        "Please load a source before printing the current page.");
                    outputs.add(printCurrentPageOutput);
                    return;
                } */
                /* if (player.getQueue().getType() != AudioEnum.PODCAST) {
                    printCurrentPageOutput.put("message", "The loaded source is not a podcast.");
                    outputs.add(printCurrentPageOutput);
                    return;
                } */
                //Host host = (Host) library.findUser(((Podcast) player.getQueue()).getOwner());
                Host host = (Host) library.findUser(pageOwner);
                //print, in order, podcasts, announcements
                pageContent = pageContent.append("Podcasts:\n\t[");
                // ALL podcasts/announcements must be printed
                for (int i = 0; i < host.getPodcasts().size(); i++) {
                    if (i > 0) {
                        pageContent = pageContent.append("\n, ");
                    }
                    pageContent = pageContent.append(host.getPodcasts().get(i).getName());
                    pageContent = pageContent.append(":\n\t[");
                    for (int j = 0; j < host.getPodcasts().get(i).getEpisodes().size(); j++) {
                        if (j > 0) {
                            pageContent = pageContent.append(", ");
                        }
                        pageContent = pageContent.append(
                            host.getPodcasts().get(i).getEpisodes().get(j).getName());
                        pageContent = pageContent.append(" - ");
                        pageContent = pageContent.append(
                            host.getPodcasts().get(i).getEpisodes().get(j).getDescription());
                    }
                    pageContent = pageContent.append("]");
                }
                pageContent = pageContent.append("\n]\n\nAnnouncements:\n\t[");
                for (int i = 0; i < host.getAnnouncements().size(); i++) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(host.getAnnouncements().get(i).getName());
                    pageContent = pageContent.append(":\n\t");
                    pageContent = pageContent.append(
                        host.getAnnouncements().get(i).getDescription());
                    pageContent = pageContent.append("\n");
                }
                pageContent = pageContent.append("]");
                printCurrentPageOutput.put("message", pageContent.toString());
                outputs.add(printCurrentPageOutput);
            }
            case LIKES -> {
                pageContent = pageContent.append("Liked songs:\n\t[");
                int i = 0;
                for (Song song : likedSongs) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(song.getName());
                    pageContent = pageContent.append(" - ");
                    pageContent = pageContent.append(song.getArtist());
                    i++;

                }
                pageContent = pageContent.append("]");
                pageContent = pageContent.append("\n\nFollowed playlists:\n\t[");
                i = 0;
                for (Playlist playlist : followedPlaylists) {
                    if (i > 0) {
                        pageContent = pageContent.append(", ");
                    }
                    pageContent = pageContent.append(playlist.getName());
                    pageContent = pageContent.append(" - ");
                    pageContent = pageContent.append(playlist.getOwner().getUsername());
                    i++;
                }
                pageContent = pageContent.append("]");
                printCurrentPageOutput.put("message", pageContent.toString());
                outputs.add(printCurrentPageOutput);
            }
            default -> {

            }
        }
    }

    /**
     * Add a new merchandise item. (Only for artists!)
     */
    public void addMerch(final Command command, final ArrayNode outputs) {
        if (type != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode addMerchOutput = mapper.createObjectNode();
            addMerchOutput.put("command", "addMerch");
            addMerchOutput.put("user", command.getUsername());
            addMerchOutput.put("timestamp", command.getTimestamp());
            addMerchOutput.put("message", command.getUsername() + " is not an artist.");
            outputs.add(addMerchOutput);
            return;
        }
        this.addMerchReal(command, outputs);
    }

    /**
     * Add a new event. (Only for artists!)
     */
    public void addEvent(final Command command, final ArrayNode outputs) {
        if (type != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode addEventOutput = mapper.createObjectNode();
            addEventOutput.put("command", "addEvent");
            addEventOutput.put("user", command.getUsername());
            addEventOutput.put("timestamp", command.getTimestamp());
            addEventOutput.put("message", command.getUsername() + " is not an artist.");
            outputs.add(addEventOutput);
            return;
        }
        this.addEventReal(command, outputs);
    }

    /**
     * Add a new podcast. (Only for hosts!)
     */
    public void addPodcast(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        if (type != UserType.HOST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode addPodcastOutput = mapper.createObjectNode();
            addPodcastOutput.put("command", "addPodcast");
            addPodcastOutput.put("user", command.getUsername());
            addPodcastOutput.put("timestamp", command.getTimestamp());
            addPodcastOutput.put("message", command.getUsername() + " is not a host.");
            outputs.add(addPodcastOutput);
            return;
        }
        this.addPodcastReal(command, myLibrary, user, outputs);
    }

    /**
     * Override in Host
     */
    public void addPodcastReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        // override in Host
    }

    /**
     * Override in Artist
     */
    public void addEventReal(final Command command, final ArrayNode outputs) {
        // override in Artist
    }

    /**
     * Override in Artist
     */
    public void addMerchReal(final Command command, final ArrayNode outputs) {
        // override in Artist
    }

    /**
     * Override in Artist
     */
    public void addAlbumReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        // override in Artist
    }

    /**
     * Override in Artist
     */
    public void showAlbumsReal(final Command command, final User user, final ArrayNode outputs) {
        // override in Artist
    }

    /**
     * Add a new announcement. (Only for hosts!)
     */
    public void addAnnouncement(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        if (type != UserType.HOST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode addAnnouncementOutput = mapper.createObjectNode();
            addAnnouncementOutput.put("command", "addAnnouncement");
            addAnnouncementOutput.put("user", command.getUsername());
            addAnnouncementOutput.put("timestamp", command.getTimestamp());
            addAnnouncementOutput.put("message", command.getUsername() + " is not a host.");
            outputs.add(addAnnouncementOutput);
            return;
        }
        this.addAnnouncementReal(command, myLibrary, user, outputs);
    }

    /**
     * Override in Host
     */
    public void addAnnouncementReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
    }

    /**
     * Remove an announcement. (Only for hosts!)
     */
    public void removeAnnouncement(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        if (type != UserType.HOST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode removeAnnouncementOutput = mapper.createObjectNode();
            removeAnnouncementOutput.put("command", "removeAnnouncement");
            removeAnnouncementOutput.put("user", command.getUsername());
            removeAnnouncementOutput.put("timestamp", command.getTimestamp());
            removeAnnouncementOutput.put("message", command.getUsername() + " is not a host.");
            outputs.add(removeAnnouncementOutput);
            return;
        }
        this.removeAnnouncementReal(command, myLibrary, user, outputs);
    }

    /**
     * Override in Host
     */
    public void removeAnnouncementReal(final Command command, final Library myLibrary,
        final User user,
        final ArrayNode outputs) {

    }

    /**
     * Show all podcasts of the host. (Only for hosts!)
     */
    public void showPodcasts(final Command command, final User user, final ArrayNode outputs) {
        if (type != UserType.HOST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode showPodcastsOutput = mapper.createObjectNode();
            showPodcastsOutput.put("command", "showPodcasts");
            showPodcastsOutput.put("user", command.getUsername());
            showPodcastsOutput.put("timestamp", command.getTimestamp());
            showPodcastsOutput.put("message", command.getUsername() + " is not a host.");
            outputs.add(showPodcastsOutput);
            return;
        }
        this.showPodcastsReal(command, user, outputs);
    }

    /**
     * Override in Host
     */
    public void showPodcastsReal(final Command command, final User user, final ArrayNode outputs) {
        // override in Host
    }

    /**
     * Remove an album. (Only for artists!)
     */
    public void removeAlbum(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        if (user.getType() != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode removeAlbumOutput = mapper.createObjectNode();
            removeAlbumOutput.put("command", "removeAlbum");
            removeAlbumOutput.put("user", command.getUsername());
            removeAlbumOutput.put("timestamp", command.getTimestamp());
            removeAlbumOutput.put("message", command.getUsername() + " is not an artist.");
            outputs.add(removeAlbumOutput);
            return;
        }
        this.removeAlbumReal(command, user, myLibrary, outputs);
    }

    /**
     * Override in Artist
     */
    public void removeAlbumReal(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        // override in Artist
    }

    /**
     * Change the page the (NORMAL!) user is currently on.
     */
    public void changePage(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode changePageOutput = mapper.createObjectNode();
        changePageOutput.put("command", "changePage");
        changePageOutput.put("user", command.getUsername());
        changePageOutput.put("timestamp", command.getTimestamp());
        if (user.getStatus() == Status.OFFLINE) {
            changePageOutput.put("message", "User " + username + " is offline.");
            outputs.add(changePageOutput);
            return;
        }
        switch (command.getNextPage()) {
            case "Home" -> {
                // put current page in prevPages
                prevPages.add(new PageDetails(user.getCurrentPage(), user.getPageOwner()));
                user.setCurrentPage(Page.HOME);
                user.setPageOwner("");
                nextPages.clear();
                changePageOutput.put("message",
                    command.getUsername() + " accessed Home successfully.");
                outputs.add(changePageOutput);
                return;
            }
            case "LikedContent" -> {
                user.setCurrentPage(Page.LIKES);
                user.setPageOwner("");
                changePageOutput.put("message",
                    command.getUsername() + " accessed LikedContent successfully.");
                outputs.add(changePageOutput);
                return;
            }
            case "Artist" -> {
                // put current page in prevPages
                prevPages.add(new PageDetails(user.getCurrentPage(), user.getPageOwner()));
                user.setCurrentPage(Page.ARTIST);
                user.setPageOwner(((Song)player.getCurrentFile()).getArtist());
                nextPages.clear();
                changePageOutput.put("message",
                    command.getUsername() + " accessed Artist successfully.");
                outputs.add(changePageOutput);
                return;
            }
            case "Host" -> {
                // put current page in prevPages
                prevPages.add(new PageDetails(user.getCurrentPage(), user.getPageOwner()));
                user.setCurrentPage(Page.HOST);
                String hostName = ((Podcast)player.getQueue()).getOwner();
                nextPages.clear();
                user.setPageOwner(hostName);
                changePageOutput.put("message",
                    command.getUsername() + " accessed Host successfully.");
                outputs.add(changePageOutput);
                return;
            }
            default -> {
            }
        }
        changePageOutput.put("message", "Invalid page.");
        outputs.add(changePageOutput);
    }


    /**
     * Remove a podcast. (Only for hosts!)
     */
    public void removePodcast(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        if (user.getType() != UserType.HOST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode removePodcastOutput = mapper.createObjectNode();
            removePodcastOutput.put("command", "removePodcast");
            removePodcastOutput.put("user", command.getUsername());
            removePodcastOutput.put("timestamp", command.getTimestamp());
            removePodcastOutput.put("message", command.getUsername() + " is not a host.");
            outputs.add(removePodcastOutput);
            return;
        }
        this.removePodcastReal(command, myLibrary, user, outputs);
    }

    /**
     * Override in Host
     */
    public void removePodcastReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        // override in Host
    }

    /**
     * Remove an event. (Only for artists!)
     */
    public void removeEvent(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        if (user.getType() != UserType.ARTIST) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode removeEventOutput = mapper.createObjectNode();
            removeEventOutput.put("command", "removeEvent");
            removeEventOutput.put("user", command.getUsername());
            removeEventOutput.put("timestamp", command.getTimestamp());
            removeEventOutput.put("message", "User not authorized to perform this operation.");
            outputs.add(removeEventOutput);
            return;
        }
        this.removeEventReal(command, user, myLibrary, outputs);
    }

    /**
     * Override in Artist
     */
    public void removeEventReal(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
    }

    /**
     * Add a listen to the artist's listens. Used for Wrapped to see how many times the user
     * listened to an artist.
     */
    public void addArtistListen(final String name) {
        for (Listens artistListen : artistListens) {
            if (artistListen.getName().equals(name)) {
                artistListen.addListen();
                return;
            }
        }
        // never listened to artist before
        artistListens.add(new Listens(name));

    }

    /**
     * Add a listen to the genre's listens. Used for Wrapped to see how many times the user listened
     * to a genre.
     */
    public void addGenreListen(final String name) {
        for (Listens genreListen : genreListens) {
            if (genreListen.getName().equals(name)) {
                genreListen.addListen();
                return;
            }
        }
        // never listened to genre before
        genreListens.add(new Listens(name));
    }

    /**
     * Add a listen to the song's listens. Used for Wrapped to see how many times the user listened
     * to a song.
     */
    public void addSongListen(final String name) {
        for (Listens songListen : songListens) {
            if (songListen.getName().equals(name)) {
                songListen.addListen();
                return;
            }
        }

        // never listened to song before
        songListens.add(new Listens(name));
    }

    /**
     * Add a listen to the album's listens. Used for Wrapped to see how many times the user listened
     * to an album.
     */
    public void addAlbumListen(final String name) {
        for (Listens albumListen : albumListens) {
            if (albumListen.getName().equals(name)) {
                albumListen.addListen();
                return;
            }
        }
        // never listened to album before
        albumListens.add(new Listens(name));
    }

    /**
     * Add a listen to the episode's listens. Used for Wrapped to see how many times the user
     * listened to an episode.
     */
    public void addEpisodeListen(final String name) {
        for (Listens episodeListen : episodeListens) {
            if (episodeListen.getName().equals(name)) {
                episodeListen.addListen();
                return;
            }
        }
        // never listened to episode before
        episodeListens.add(new Listens(name));
    }

    /**
     * Print the user's Wrapped. (top listened songs, albums, ...)
     */
    public void wrapped(final Command command, final User user, final ArrayNode outputs,
        final Library myLibrary) {
        user.getPlayer().update(command.getTimestamp(), user, library);
        // print top 5 listened artists, genres, songs, albums, podcasts
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrappedOutput = mapper.createObjectNode();
        wrappedOutput.put("command", "wrapped");
        wrappedOutput.put("user", command.getUsername());
        wrappedOutput.put("timestamp", command.getTimestamp());
        if (user.getSongListens().size() == 0 && user.getEpisodeListens().size() == 0) {
            wrappedOutput.put("message", "No data to show for user " + command.getUsername() + ".");
            outputs.add(wrappedOutput);
            return;
        }
        ObjectNode result = mapper.createObjectNode();
        ObjectNode artists = mapper.createObjectNode();
        ObjectNode genres = mapper.createObjectNode();
        ObjectNode songs = mapper.createObjectNode();
        ObjectNode albums = mapper.createObjectNode();
        ObjectNode episodes = mapper.createObjectNode();
        // sort listens by number of listens
        artistListens.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        genreListens.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        songListens.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        albumListens.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        episodeListens.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });

        for (int i = 0; i < MAX_PRINTED; i++) {
            if (i < artistListens.size()) {
                artists.put(artistListens.get(i).getName(), artistListens.get(i).getListens());
            }
        }
        // add top 5 listened genres
        for (int i = 0; i < MAX_PRINTED; i++) {
            if (i < genreListens.size()) {
                genres.put(genreListens.get(i).getName(), genreListens.get(i).getListens());
            }
        }
        // add top 5 listened songs
        for (int i = 0; i < MAX_PRINTED; i++) {
            if (i < songListens.size()) {
                songs.put(songListens.get(i).getName(), songListens.get(i).getListens());
            }
        }
        // add top 5 listened albums
        for (int i = 0; i < MAX_PRINTED; i++) {
            if (i < albumListens.size()) {
                albums.put(albumListens.get(i).getName(), albumListens.get(i).getListens());
            }
        }
        // add top 5 listened podcasts
        for (int i = 0; i < MAX_PRINTED; i++) {
            if (i < episodeListens.size()) {
                episodes.put(episodeListens.get(i).getName(),
                    episodeListens.get(i).getListens());
            }
        }
        result.putPOJO("topArtists", artists);
        result.putPOJO("topGenres", genres);
        result.putPOJO("topSongs", songs);
        result.putPOJO("topAlbums", albums);
        result.putPOJO("topEpisodes", episodes);
        wrappedOutput.putPOJO("result", result);
        outputs.add(wrappedOutput);
    }

    /**
     * Insert an ad in the user's player.
     */
    public void adBreak(final Command command, final User user, final ArrayNode outputs,
        final Library myLibrary) {
        user.getPlayer().update(command.getTimestamp(), user, library);
        // print top 5 listened artists, genres, songs, albums, podcasts
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode adBreakOutput = mapper.createObjectNode();
        adBreakOutput.put("command", "adBreak");
        adBreakOutput.put("user", command.getUsername());
        adBreakOutput.put("timestamp", command.getTimestamp());
        if (user.getPlayer().getQueue() == null) {
            adBreakOutput.put("message", command.getUsername() + " is not playing any music.");
            outputs.add(adBreakOutput);
            return;
        }
        user.getPlayer().setAdBreak(true);
        user.getPlayer().setAdPrice((double) command.getPrice());
        adBreakOutput.put("message", "Ad inserted successfully.");
        outputs.add(adBreakOutput);
    }

    /**
     * Add a subscriber. (For artists and hosts)
     */
    public int addSubscriber(final User user) {
        if (subscribers.contains(user)) {
            subscribers.remove(user);
            return -1;
        }
        subscribers.add(user);
        return 1;
    }

    /**
     * Add a new notification. (Observer pattern)
     */
    public void updateNotifications(final Notification notification) {
        notifications.add(notification);
    }

    /**
     * See the user's notifications. NOTE: They are deleted after being seen.
     */
    public void getUserNotifications(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode notificationsOutput = mapper.createObjectNode();
        notificationsOutput.put("command", "getNotifications");
        notificationsOutput.put("user", command.getUsername());
        notificationsOutput.put("timestamp", command.getTimestamp());
        ArrayNode notificationsArray = mapper.createArrayNode();
        for (Notification notification : notifications) {
            ObjectNode notificationNode = mapper.createObjectNode();
            notificationNode.put("description", notification.getDescription());
            notificationNode.put("name", notification.getName());
            notificationsArray.add(notificationNode);
        }
        notificationsOutput.putPOJO("notifications", notificationsArray);
        outputs.add(notificationsOutput);
        notifications.clear();
    }

    /**
     * Buy merch.
     */
    public void buyMerch(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode buyMerchOutput = mapper.createObjectNode();
        buyMerchOutput.put("command", "buyMerch");
        buyMerchOutput.put("user", command.getUsername());
        buyMerchOutput.put("timestamp", command.getTimestamp());
        for (Artist artist : library.getArtists()) {
            for (Merch merch : artist.getMerches()) {
                if (merch.getName().equals(command.getName())) {
                    Merch newMerch = new Merch(merch.getName(),
                        merch.getDescription(), merch.getPrice());
                    artist.setMerchRevenue(artist.getMerchRevenue() + merch.getPrice());
                    boughtMerch.add(newMerch);
                    buyMerchOutput.put("message", username + " has added new merch successfully.");
                    outputs.add(buyMerchOutput);
                    return;
                }
            }
        }
        buyMerchOutput.put("message", "The merch " + command.getName() + " doesn't exist.");
        outputs.add(buyMerchOutput);
    }

    /**
     * See all merch bought by user.
     */
    public void seeMerch(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode seeMerchOutput = mapper.createObjectNode();
        seeMerchOutput.put("command", "seeMerch");
        seeMerchOutput.put("user", command.getUsername());
        seeMerchOutput.put("timestamp", command.getTimestamp());
        ArrayNode merchArray = mapper.createArrayNode();
        for (Merch merch : boughtMerch) {
            merchArray.add(merch.getName());
        }
        seeMerchOutput.putPOJO("result", merchArray);
        outputs.add(seeMerchOutput);

    }

    /**
     * Buy a premium subscription.
     */
    public void buyPremium(final Command command, final ArrayNode outputs) {
        player.update(command.getTimestamp(), this, library);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode buyPremiumOutput = mapper.createObjectNode();
        buyPremiumOutput.put("command", "buyPremium");
        buyPremiumOutput.put("user", command.getUsername());
        buyPremiumOutput.put("timestamp", command.getTimestamp());
        if (isPremium) {
            buyPremiumOutput.put("message", username + " is already a premium user.");
            outputs.add(buyPremiumOutput);
            return;
        }
        isPremium = true;
        buyPremiumOutput.put("message", username + " bought the subscription successfully.");
        outputs.add(buyPremiumOutput);
    }

    /**
     * Cancel premium subscription.
     */
    public void cancelPremium(final Command command, final ArrayNode outputs) {
        player.update(command.getTimestamp(), this, library);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode cancelPremiumOutput = mapper.createObjectNode();
        cancelPremiumOutput.put("command", "cancelPremium");
        cancelPremiumOutput.put("user", command.getUsername());
        cancelPremiumOutput.put("timestamp", command.getTimestamp());
        if (!isPremium) {
            cancelPremiumOutput.put("message", username + " is not a premium user.");
            outputs.add(cancelPremiumOutput);
            return;
        }
        player.payout(this, library);
        isPremium = false;
        cancelPremiumOutput.put("message", username + " cancelled the subscription successfully.");
        outputs.add(cancelPremiumOutput);
    }

    /**
     * Update recommendation.
     */
    public void updateRecommendations(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode updateRecommendationOutput = mapper.createObjectNode();
        updateRecommendationOutput.put("command", "updateRecommendations");
        updateRecommendationOutput.put("user", command.getUsername());
        updateRecommendationOutput.put("timestamp", command.getTimestamp());
        player.update(command.getTimestamp(), this, library);
        switch (command.getRecommendationType()) {
            case "random_song" -> {
                if (player.getTimeListened() < MIN_LISTENED) {
                    updateRecommendationOutput.put("message",
                        "No new recommendations were found");
                    outputs.add(updateRecommendationOutput);
                    return;
                }
                // add all songs with genre of the listened song to a list
                ArrayList<Song> genreSongs = new ArrayList<>();
                for (Song song : library.getSongs()) {
                    if (song.getGenre().equals(((Song) player.getCurrentFile()).getGenre())) {
                        genreSongs.add(song);
                    }
                }
                // get random song with seed = timeListened
                Random random = new Random(player.getTimeListened());
                int randomIndex = random.nextInt(genreSongs.size());
                Song randomSong = genreSongs.get(randomIndex);
                if (randomSong == null) {
                    updateRecommendationOutput.put("message",
                        "No new recommendations were found");
                    outputs.add(updateRecommendationOutput);
                    return;
                }
                songRecommendation = randomSong;
                lastRecommendation = "song";
                updateRecommendationOutput.put("message",
                    "The recommendations for user " + username
                        + " have been updated successfully.");
                outputs.add(updateRecommendationOutput);
            }
            case "random_playlist" -> {
                ArrayList<Listens> genres = new ArrayList<>();
                for (Song likedSong : likedSongs) {
                    addGenre(genres, likedSong.getGenre());
                }
                for (Playlist playlist : playlists) {
                    for (Song song : playlist.getSongs()) {
                        addGenre(genres, song.getGenre());
                    }
                }

                for (Playlist playlist : followedPlaylists) {
                    for (Song song : playlist.getSongs()) {
                        addGenre(genres, song.getGenre());
                    }
                }
                // sort genres by number of listens
                genres.sort((o1, o2) -> {
                    if (o1.getListens() == o2.getListens()) {
                        // sort by name
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o2.getListens() - o1.getListens();
                });
                Playlist recommendedPlaylist = new Playlist(username + "'s recommendations");
                // top 5 songs from genre #1
                ArrayList<Song> genreSongs = new ArrayList<>();
                for (Song song : library.getSongs()) {
                    if (genres.size() < 1) {
                        updateRecommendationOutput.put("message",
                            "No new recommendations were found");
                        outputs.add(updateRecommendationOutput);
                        return;
                    }
                    if (song.getGenre().equals(genres.get(0).getName())) {
                        genreSongs.add(song);
                    }
                }
                // sort songs by number of listens
                genreSongs.sort((o1, o2) -> {
                    if (o1.getListens() == o2.getListens()) {
                        // sort by name
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o2.getListens() - o1.getListens();
                });
                // add first 5 songs in playlist
                for (int i = 0; i < MAX_PRINTED; i++) {
                    if (i < genreSongs.size()) {
                        recommendedPlaylist.addSong(genreSongs.get(i));
                    }
                }
                genreSongs.clear();
                // top 3 songs from genre #2
                for (Song song : library.getSongs()) {
                    if (genres.size() < 2) {
                        break;
                    }
                    if (song.getGenre().equals(genres.get(1).getName())) {
                        genreSongs.add(song);
                    }
                }
                // sort songs by number of listens
                genreSongs.sort((o1, o2) -> {
                    if (o1.getListens() == o2.getListens()) {
                        // sort by name
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o2.getListens() - o1.getListens();
                });
                // add first 3 songs in playlist
                for (int i = 0; i < MAX_PRINTED - 2; i++) {
                    if (i < genreSongs.size()) {
                        recommendedPlaylist.addSong(genreSongs.get(i));
                    }
                }
                genreSongs.clear();
                // top 2 songs from genre #3
                for (Song song : library.getSongs()) {
                    if (genres.size() < TOP_GENRE_SIZE) {
                        break;
                    }
                    if (song.getGenre().equals(genres.get(2).getName())) {
                        genreSongs.add(song);
                    }
                }
                // sort songs by number of listens
                genreSongs.sort((o1, o2) -> {
                    if (o1.getListens() == o2.getListens()) {
                        // sort by name
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o2.getListens() - o1.getListens();
                });
                // add first 2 songs in playlist
                for (int i = 0; i < MAX_PRINTED - TOP_GENRE_SIZE; i++) {
                    if (i < genreSongs.size()) {
                        recommendedPlaylist.addSong(genreSongs.get(i));
                    }
                }
                genreSongs.clear();
                if (recommendedPlaylist.getSongs().isEmpty()) {
                    updateRecommendationOutput.put("message",
                        "No new recommendations were found");
                    outputs.add(updateRecommendationOutput);
                    return;
                }
                playlistRecommendation = recommendedPlaylist;
                lastRecommendation = "playlist";
                updateRecommendationOutput.put("message",
                    "The recommendations for user " + username
                        + " have been updated successfully.");
                outputs.add(updateRecommendationOutput);

            }
            case "fans_playlist" -> {
                Artist artist = library.findArtist(((Song) player.getCurrentFile()).getArtist());
                // sort artists listeners by number of listens
                if (artist.getListeners() == null) {
                    updateRecommendationOutput.put("message",
                        "No new recommendations were found");
                    outputs.add(updateRecommendationOutput);
                    return;
                }
                Playlist recommendedPlaylist = new Playlist(
                    artist.getUsername() + " Fan Club recommendations");
                artist.getListeners().sort((o1, o2) -> {
                    if (o1.getListens() == o2.getListens()) {
                        // sort by name
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o2.getListens() - o1.getListens();
                });
                // get top 5 listeners
                ArrayList<User> topListeners = new ArrayList<>();
                for (int i = 0; i < MAX_PRINTED; i++) {
                    if (i < artist.getListeners().size()) {
                        String userName = artist.getListeners().get(i).getName();
                        topListeners.add(library.findUser(userName));
                    }
                }
                for (User user : topListeners) {
                    // sort user's liked songs by number of likes
                    user.getLikedSongs().sort((o1, o2) -> {
                        if (o1.getLikes().size() == o2.getLikes().size()) {
                            // sort by name
                            return o1.getName().compareTo(o2.getName());
                        }
                        return o2.getLikes().size() - o1.getLikes().size();
                    });
                    // get top 5 liked songs
                    for (int i = 0; i < MAX_PRINTED; i++) {
                        if (i < user.getLikedSongs().size()) {
                            recommendedPlaylist.addSong(user.getLikedSongs().get(i));
                        }
                    }
                }
                if (recommendedPlaylist.getSongs().isEmpty()) {
                    updateRecommendationOutput.put("message",
                        "No new recommendations were found");
                    outputs.add(updateRecommendationOutput);
                    return;
                }
                playlistRecommendation = recommendedPlaylist;
                lastRecommendation = "playlist";
                updateRecommendationOutput.put("message",
                    "The recommendations for user " + username
                        + " have been updated successfully.");
                outputs.add(updateRecommendationOutput);
            }
            default -> {
                return;
            }
        }
    }

    /**
     * Go to previous page.
     */
    public void previousPage(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode previousPageOutput = mapper.createObjectNode();
        previousPageOutput.put("command", "previousPage");
        previousPageOutput.put("user", command.getUsername());
        previousPageOutput.put("timestamp", command.getTimestamp());
        if (user.getPrevPages().size() == 0) {
            previousPageOutput.put("message", "No page to go back to.");
            outputs.add(previousPageOutput);
            return;
        }
        // put current page in nextPages
        nextPages.add(new PageDetails(user.getCurrentPage(), user.getPageOwner()));
        PageDetails lastPage = user.getPrevPages().get(user.getPrevPages().size() - 1);
        user.setCurrentPage(lastPage.getType());
        user.setPageOwner(lastPage.getOwner());
        user.getPrevPages().remove(user.getPrevPages().size() - 1);
        previousPageOutput.put("message",
            "The user " + username + " has navigated successfully to the previous page.");
        outputs.add(previousPageOutput);
    }

    /**
     * Go to next page.
     */
    public void nextPage(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode nextPageOutput = mapper.createObjectNode();
        nextPageOutput.put("command", "nextPage");
        nextPageOutput.put("user", command.getUsername());
        nextPageOutput.put("timestamp", command.getTimestamp());
        if (user.getNextPages().size() == 0) {
            nextPageOutput.put("message", "There are no pages left to go forward.");
            outputs.add(nextPageOutput);
            return;
        }
        // put current page in prevPages
        user.getPrevPages().add(new PageDetails(user.getCurrentPage(), user.getPageOwner()));
        PageDetails nextPage = user.getNextPages().get(user.getNextPages().size() - 1);
        user.setCurrentPage(nextPage.getType());
        user.setPageOwner(nextPage.getOwner());
        user.getNextPages().remove(user.getNextPages().size() - 1);
        nextPageOutput.put("message",
            "The user " + username + " has navigated successfully to the next page.");
        outputs.add(nextPageOutput);
    }

    /**
     * Used for counting genre appearances in user's playlists,
     * liked songs and followed playlists.
     */
    public void addGenre(final ArrayList<Listens> genres, final String name) {
        for (Listens genre : genres) {
            if (genre.getName().equals(name)) {
                genre.addListen();
                return;
            }
        }
        // never listened to genre before
        genres.add(new Listens(name));
    }

}
