package audio.lists;

import audio.files.Song;
import audio.item.AudioItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import enums.AudioEnum;
import enums.Status;
import fileio.input.LibraryInput;
import fileio.input.UserInput;
import fileio.input.SongInput;
import fileio.input.PodcastInput;
import java.util.Iterator;
import lombok.Getter;
import lombok.Setter;
import specialusers.Artist;
import specialusers.Host;
import user.User;
import java.util.ArrayList;

@Getter
@Setter
public final class Library {

    private static final double HUNDRED = 100.0;
    private ArrayList<Song> songs = new ArrayList<>();
    private ArrayList<Podcast> podcasts = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Artist> artists = new ArrayList<>();
    private ArrayList<Host> hosts = new ArrayList<>();
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Album> albums = new ArrayList<>();
    private static Library instance = null;
    private int lastTimestamp;

    private Library(final LibraryInput library) {
        for (SongInput song : library.getSongs()) {
            songs.add(new Song(song));
        }
        for (PodcastInput podcast : library.getPodcasts()) {
            podcasts.add(new Podcast(podcast));
            hosts.add(new Host(podcast.getOwner(), 0, ""));
        }
        for (UserInput user : library.getUsers()) {
            users.add(new User(user, this));
        }
    }

    /**
     * Singleton pattern inititation
     */
    public static Library getInstance(final LibraryInput library) {
        if (instance == null) {
            instance = new Library(library);
        }
        return instance;
    }

    /**
     * Singleton pattern reset
     */
    public static void reset() {
        instance = null;
    }

    /**
     * Find a user's data (such as player, search bar, etc.)
     *
     * @param username is the user's username
     * @return the user's data object (User object) or NULL if a user with this username doesn't
     * exist.
     */
    public User findUser(final String username) {
        if (username == null) {
            return null;
        }
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        for (User artist : artists) {
            if (artist.getUsername().equals(username)) {
                return artist;
            }
        }
        for (User host : hosts) {
            if (host.getUsername().equals(username)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Retrieves and generates a list of usernames for online users based on the provided command
     * and appends the result to the given ArrayNode.
     * <p>
     * This method iterates through a collection of User objects, checks their status, and includes
     * the usernames of those with an online status in the result.
     *
     * @param command The Command object associated with the request.
     * @param outputs The ArrayNode to which the result will be appended.
     */
    public void getOnlineUsers(final Command command, final ArrayNode outputs) {
        //go through users and print all usernames of online users
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode getOnlineUsersOutput = mapper.createObjectNode();
        getOnlineUsersOutput.put("command", "getOnlineUsers");
        getOnlineUsersOutput.put("timestamp", command.getTimestamp());
        ArrayNode usersList = mapper.createArrayNode();
        for (User user : users) {
            if (user.getStatus() == Status.ONLINE) {
                usersList.add(user.getUsername());
            }
        }
        getOnlineUsersOutput.putPOJO("result", usersList);
        outputs.add(getOnlineUsersOutput);
    }

    /**
     * Processes a command to add a new user, artist, or host to the system and provides feedback
     * through the specified ArrayNode.
     * <p>
     * This method checks if the specified username already exists in the users, artists, or hosts
     * arrays. If the username is already taken, an appropriate message is added to the output.
     * Otherwise, a new user, artist, or host is created based on the command type and added to the
     * corresponding array.
     *
     * @param command   The Command object containing information about the user to be added.
     * @param myLibrary The Library object associated with the system.
     * @param outputs   The ArrayNode to which the feedback message will be appended.
     */
    public void addUser(final Command command, final Library myLibrary, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addUserOutput = mapper.createObjectNode();
        addUserOutput.put("command", "addUser");
        addUserOutput.put("timestamp", command.getTimestamp());
        addUserOutput.put("user", command.getUsername());
        //check if user already exists, in users and artists array
        boolean userExists = false;
        for (User user : users) {
            if (user.getUsername().equals(command.getUsername())) {
                userExists = true;
                break;
            }
        }
        for (User artist : artists) {
            if (artist.getUsername().equals(command.getUsername())) {
                userExists = true;
                break;
            }
        }
        for (User host : hosts) {
            if (host.getUsername().equals(command.getUsername())) {
                userExists = true;
                break;
            }
        }
        if (userExists) {
            if (command.getType().equals("host")) {
                addUserOutput.put("message",
                    "The username " + command.getUsername() + " has been added successfully.");
                outputs.add(addUserOutput);
                return;
            }
            addUserOutput.put("message",
                "The username " + command.getUsername() + " is already taken.");
            outputs.add(addUserOutput);
            return;
        }
        //create new user and add it to users array
        switch (command.getType()) {
            case "user" -> {
                User newUser = new User(command.getUsername(), command.getAge(), command.getCity(),
                    this);
                users.add(newUser);
            }
            case "artist" -> {
                Artist newArtist = new Artist(command.getUsername(), command.getAge(),
                    command.getCity());
                artists.add(newArtist);
            }
            case "host" -> {
                Host newHost = new Host(command.getUsername(), command.getAge(), command.getCity());
                hosts.add(newHost);
            }
            default -> {
                break;
            }
        }
        addUserOutput.put("message",
            "The username " + command.getUsername() + " has been added successfully.");
        outputs.add(addUserOutput);
    }

    /**
     * Finds a song in the library based on its name.
     *
     * @param name The name of the song to be found.
     * @return The song object (reference) if it exists, null otherwise.
     */
    public Song getSong(final String name) {
        for (Song song : songs) {
            if (song.getName().equals(name)) {
                return song;
            }
        }
        return null;
    }

    /**
     * Show all users, offline or online, regardless of type. (user, artist, host)
     */
    public void getAllUsers(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode printAllUsersOutput = mapper.createObjectNode();
        printAllUsersOutput.put("command", "getAllUsers");
        printAllUsersOutput.put("timestamp", command.getTimestamp());
        //array of all usernames
        ArrayNode usernames = mapper.createArrayNode();
        for (User user : users) {
            usernames.add(user.getUsername());
        }
        for (Artist artist : artists) {
            usernames.add(artist.getUsername());
        }
        for (Host host : hosts) {
            usernames.add(host.getUsername());
        }
        printAllUsersOutput.putPOJO("result", usernames);
        outputs.add(printAllUsersOutput);
    }

    /**
     * Processes a command to delete a user, artist, or host from the system and provides feedback
     * through the specified ArrayNode. This method checks if the user exists, then (if existing)
     * checks if the user can be deleted. If the user can be deleted, all references to the user are
     * removed from the system. Otherwise, an appropriate message is added to the output.
     */
    public void deleteUser(final Command command, final ArrayNode outputs, final Library library) {
        // update timestamp
        for (User user : users) {
            user.getPlayer().update(command.getTimestamp(), user, library);
        }
        // find user (is he normal, artist or host?)
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode deleteUserOutput = mapper.createObjectNode();
        deleteUserOutput.put("command", "deleteUser");
        deleteUserOutput.put("timestamp", command.getTimestamp());
        deleteUserOutput.put("user", command.getUsername());
        User user = findUser(command.getUsername());
        if (user == null) {
            deleteUserOutput.put("message", "User " + command.getUsername() + " not found!");
            outputs.add(deleteUserOutput);
            return;
        }
        boolean deletable = true;
        switch (user.getType()) {
            case NORMAL -> {
                // check if i can delete user
                // see if anyone has his playlist loaded
                for (User user1 : users) {
                    AudioItem queue = user1.getPlayer().getQueue();
                    if (queue != null && queue.getType() == AudioEnum.PLAYLIST
                        && ((Playlist) queue).getOwner().equals(user)) {
                        deletable = false;
                        break;

                    }
                }
                if (deletable) {
                    //remove all likes and follows of user
                    Iterator<Song> iterator = songs.iterator();
                    while (iterator.hasNext()) {
                        Song song = iterator.next();
                        if (song.getLikes().contains(user)) {
                            song.getLikes().remove(user);
                        }
                    }
                    Iterator<Playlist> iterator1 = playlists.iterator();
                    while (iterator1.hasNext()) {
                        Playlist playlist = iterator1.next();
                        if (playlist.getOwner().equals(user)) {
                            iterator1.remove();
                        }
                        if (playlist.getFollowers().contains(user)) {
                            playlist.getFollowers().remove(user);
                        }
                    }
                    // remove user's playlist from other users follows
                    for (User user1 : users) {
                        Iterator<Playlist> iterator2 = user1.getFollowedPlaylists().iterator();
                        while (iterator2.hasNext()) {
                            Playlist playlist = iterator2.next();
                            if (playlist.getOwner().equals(user)) {
                                iterator2.remove();
                            }
                        }
                    }

                    users.remove(user);
                    // success message
                    deleteUserOutput.put("message",
                        command.getUsername() + " was successfully deleted.");
                    outputs.add(deleteUserOutput);
                } else {
                    deleteUserOutput.put("message", command.getUsername() + " can't be deleted.");
                    outputs.add(deleteUserOutput);
                }
            }
            case ARTIST -> {
                Artist artist = (Artist) user;
                //check if i can delete artist
                //see if anyone has his song loaded
                for (User user1 : users) {
                    AudioItem currentQueue = user1.getPlayer().getQueue();
                    if (currentQueue != null) {
                        switch (currentQueue.getType()) {
                            case SONG -> {
                                if (((Song) currentQueue).getArtist()
                                    .equals(artist.getUsername())) {
                                    deletable = false;
                                    break;
                                }
                            }
                            case ALBUM -> {
                                if (((Album) currentQueue).getOwner().getUsername()
                                    .equals(artist.getUsername())) {
                                    deletable = false;
                                    break;
                                }
                            }
                            case PLAYLIST -> {
                                for (Song song : ((Playlist) currentQueue).getSongs()) {
                                    if (song.getArtist().equals(artist.getUsername())) {
                                        deletable = false;
                                        break;
                                    }
                                }
                            }
                            default -> {
                                break;
                            }
                        }
                    }
                    // check if user is on artist page
                    if (user1.getPageOwner().equals(artist.getUsername())) {
                        deletable = false;
                        break;
                    }
                }
                if (deletable) {
                    // remove all songs from library

                    // remove all albums from library
                    Iterator<Album> iterator2 = albums.iterator();
                    while (iterator2.hasNext()) {
                        Album album = iterator2.next();
                        if (album.getOwner().equals(artist)) {
                            iterator2.remove();
                        }
                    }
                    // remove all songs from playlists
                    for (Playlist playlist : playlists) {
                        for (Song song : playlist.getSongs()) {
                            if (song.getArtist().equals(artist.getUsername())) {
                                playlist.getSongs().remove(song);
                            }
                        }
                    }
                    for (User user1 : users) {
                        // remove all likes of user
                        Iterator<Song> iterator3 = user1.getLikedSongs().iterator();
                        while (iterator3.hasNext()) {
                            Song song = iterator3.next();
                            if (song.getArtist().equals(artist.getUsername())) {
                                iterator3.remove();
                            }
                        }

                        // get queue of user
                        AudioItem queue = user1.getPlayer().getQueue();
                        // if queue is a playlist, remove all songs from artist
                        if (queue != null && queue.getType() == AudioEnum.PLAYLIST) {
                            for (Song song : ((Playlist) queue).getSongs()) {
                                if (song.getArtist().equals(artist.getUsername())) {
                                    ((Playlist) queue).getSongs().remove(song);
                                }
                            }
                        }
                    }

                    Iterator<Song> iterator = songs.iterator();
                    while (iterator.hasNext()) {
                        Song song = iterator.next();
                        if (song.getArtist().equals(artist.getUsername())) {
                            iterator.remove();
                        }
                    }
                    // success
                    artists.remove(artist);
                    deleteUserOutput.put("message",
                        command.getUsername() + " was successfully deleted.");
                    outputs.add(deleteUserOutput);
                } else {
                    deleteUserOutput.put("message",
                        command.getUsername() + " can't be deleted.");
                    outputs.add(deleteUserOutput);
                }
            }
            case HOST -> {
                Host host = (Host) user;
                //check if i can delete host
                //see if anyone has his podcast loaded
                for (User user1 : users) {
                    AudioItem currentQueue = user1.getPlayer().getQueue();
                    if (currentQueue != null) {
                        if (currentQueue.getType() == AudioEnum.PODCAST) {
                            if (((Podcast) currentQueue).getOwner().equals(host.getUsername())) {
                                deletable = false;
                                break;
                            }
                        }
                    }
                    //also check if they're on his page
                    if (user1.getPageOwner().equals(host.getUsername())) {
                        deletable = false;
                        break;
                    }
                }
                if (deletable) {
                    // remove all podcasts from library
                    Iterator<Podcast> iterator = podcasts.iterator();
                    while (iterator.hasNext()) {
                        Podcast podcast = iterator.next();
                        if (podcast.getOwner().equals(host.getUsername())) {
                            iterator.remove();
                        }
                    }
                    // remove all podcasts from users
                    for (User user1 : users) {
                        Iterator<PodcastProgress> iterator1 = user1.getPodcastProgress().iterator();
                        while (iterator1.hasNext()) {
                            PodcastProgress podcastProgress = iterator1.next();
                            if (podcastProgress.getPodcast().getOwner()
                                .equals(host.getUsername())) {
                                iterator1.remove();
                            }
                        }
                    }
                    // success
                    hosts.remove(host);
                    deleteUserOutput.put("message",
                        command.getUsername() + " was successfully deleted.");
                    outputs.add(deleteUserOutput);
                } else {
                    deleteUserOutput.put("message",
                        command.getUsername() + " can't be deleted.");
                    outputs.add(deleteUserOutput);
                }
            }
            default -> {
                break;
            }
        }
    }

    /**
     * Find an artists based on his name.
     */
    public Artist findArtist(final String name) {
        for (Artist artist : artists) {
            if (artist.getUsername().equals(name)) {
                return artist;
            }
        }
        return null;
    }

    /**
     * Calculate all the revenue for artists, as well as their
     * most profitable songs.
     */
    public void endProgram(final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode endProgramOutput = mapper.createObjectNode();
        endProgramOutput.put("command", "endProgram");

        for (User user : users) {
            user.getPlayer().update(lastTimestamp, user, this);
            if (user.isPremium() && !user.getPlayer().getPremiumSongHistory().isEmpty()) {
                user.getPlayer().payout(user, this);
            }
        }
        //sort artists by revenue
        artists.sort((artist1, artist2) -> {
            if (artist1.getSongRevenue() + artist1.getMerchRevenue()
                > artist2.getSongRevenue() + artist2.getMerchRevenue()) {
                return -1;
            } else if (artist1.getSongRevenue() + artist1.getMerchRevenue()
                < artist2.getSongRevenue() + artist2.getMerchRevenue()) {
                return 1;
            } else {
                return artist1.getUsername().compareTo(artist2.getUsername());
            }
        });
        ObjectNode result = mapper.createObjectNode();
        int printed = 1;
        for (Artist artist : artists) {
            if (artist.getSongRevenue() + artist.getMerchRevenue() > 0
                || artist.getListeners().size() > 0) {
                ObjectNode artistNode = mapper.createObjectNode();
                artist.setSongRevenue(Math.round(artist.getSongRevenue() * HUNDRED) / HUNDRED);
                artistNode.put("songRevenue", artist.getSongRevenue());
                artistNode.put("merchRevenue", artist.getMerchRevenue());
                artistNode.put("ranking", printed);
                if (artist.getSongRevenue() == 0) {
                    artistNode.put("mostProfitableSong", "N/A");
                } else {
                    // sort songs by revenue
                    artist.getSongProfits().sort((song1, song2) -> {
                        if (song1.getRevenue() > song2.getRevenue()) {
                            return -1;
                        } else if (song1.getRevenue() < song2.getRevenue()) {
                            return 1;
                        } else {
                            return song1.getSongName().compareTo(song2.getSongName());
                        }
                    });
                    artistNode.put("mostProfitableSong",
                        artist.getSongProfits().get(0).getSongName());
                }
                printed++;
                result.putPOJO(artist.getUsername(), artistNode);
            }
        }
        endProgramOutput.putPOJO("result", result);
        outputs.add(endProgramOutput);
    }
}
