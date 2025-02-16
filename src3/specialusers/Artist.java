package specialusers;

import artistentities.Event;
import artistentities.Listens;
import artistentities.Merch;
import audio.files.Song;
import audio.lists.Album;
import audio.lists.Library;
import audio.lists.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import enums.UserType;
import fileio.input.SongInput;
import java.util.ArrayList;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import user.Notification;
import user.User;

@Getter
@Setter
public final class Artist extends User {

    private static final int MAX_DAYS_FEBRUARY = 28;
    private static final int MAX_DAYS_MONTH = 31;
    private static final int MAX_MONTH = 12;
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2023;
    private static final int MAX_ITEMS = 5;
    private ArrayList<Album> albums = new ArrayList<>();
    private ArrayList<Merch> merches = new ArrayList<>();
    private ArrayList<Event> events = new ArrayList<>();
    private int likes;
    private ArrayList<Listens> listeners;
    private double songRevenue;
    private double merchRevenue;
    private String mostProfitableSong;
    private ArrayList<Revenues> songProfits = new ArrayList<>();


    public Artist(final String username, final Integer age, final String city) {
        super(username, age, city, UserType.ARTIST);
        this.likes = 0;
        this.listeners = new ArrayList<>();
        this.songRevenue = 0;
        this.merchRevenue = 0;
    }

    /**
     * Adds a new album to the artist's albums. If an album with the same name exists or if the same
     * song appears twice in the album, the album will not be added.
     */
    @Override
    public void addAlbumReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addAlbumOutput = mapper.createObjectNode();
        addAlbumOutput.put("command", "addAlbum");
        addAlbumOutput.put("user", command.getUsername());
        addAlbumOutput.put("timestamp", command.getTimestamp());

        for (Album album : albums) {
            if (album.getName().equals(command.getName())) {
                //album already exists
                addAlbumOutput.put("message",
                    command.getUsername() + " has another album with the same name.");
                outputs.add(addAlbumOutput);
                return;
            }
        }
        // check if same song appears twice in album
        // make a set of songs and check if the size is the same
        ArrayList<SongInput> songs = command.getSongs();
        ArrayList<String> songNames = new ArrayList<>();
        for (SongInput song : songs) {
            songNames.add(song.getName());
        }
        Set<String> songsSet = Set.copyOf(songNames);
        if (songNames.size() != songsSet.size()) {
            // same song appears twice
            addAlbumOutput.put("message",
                command.getUsername() + " has the same song at least twice in this album.");
            outputs.add(addAlbumOutput);
            return;
        }
        Album album = new Album(command.getName(), this, command.getTimestamp(),
            command.getDescription(), command.getReleaseYear());
        for (SongInput song : songs) {
            // see if it exists in the library
            Song newSong = new Song(song);
            myLibrary.getSongs().add(newSong);
            album.getSongs().add(newSong);
        }
        albums.add(album);
        myLibrary.getAlbums().add(album);
        Notification notification = new Notification("New Album from " + this.getUsername() + ".",
            "New Album");
        for (User subscriber : getSubscribers()) {
            subscriber.updateNotifications(notification);
        }
        // print the output
        addAlbumOutput.put("message", command.getUsername() + " has added new album successfully.");
        outputs.add(addAlbumOutput);


    }

    /**
     * Prints the albums of the artist.
     */
    @Override
    public void showAlbumsReal(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode showAlbumsOutput = mapper.createObjectNode();
        showAlbumsOutput.put("command", "showAlbums");
        showAlbumsOutput.put("user", command.getUsername());
        showAlbumsOutput.put("timestamp", command.getTimestamp());
        ArrayNode albumsArray = mapper.createArrayNode();
        for (Album album : albums) {
            ObjectNode albumObject = mapper.createObjectNode();
            albumObject.put("name", album.getName());
            // add the song names in an array
            ArrayNode songsArray = mapper.createArrayNode();
            for (Song song : album.getSongs()) {
                songsArray.add(song.getName());
            }
            albumObject.set("songs", songsArray);
            albumsArray.add(albumObject);
        }
        showAlbumsOutput.set("result", albumsArray);
        outputs.add(showAlbumsOutput);
    }

    /**
     * Adds a new merchandise item. If an item with the same name exists, the item will not be
     * added.
     */
    @Override
    public void addMerchReal(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addMerchOutput = mapper.createObjectNode();
        addMerchOutput.put("command", "addMerch");
        addMerchOutput.put("user", command.getUsername());
        addMerchOutput.put("timestamp", command.getTimestamp());
        for (Merch merch : merches) {
            if (merch.getName().equals(command.getName())) {
                //merch already exists
                addMerchOutput.put("message",
                    command.getUsername() + " has merchandise with the same name.");
                outputs.add(addMerchOutput);
                return;
            }
        }
        if (command.getPrice() < 0) {
            addMerchOutput.put("message", "Price for merchandise can not be negative.");
            outputs.add(addMerchOutput);
            return;
        }
        Merch merch = new Merch(command.getName(), command.getDescription(), command.getPrice());
        merches.add(merch);
        Notification notification = new Notification(
            "New Merchandise from " + this.getUsername() + ".",
            "New Merchandise");
        for (User subscriber : getSubscribers()) {
            subscriber.updateNotifications(notification);
        }
        addMerchOutput.put("message",
            command.getUsername() + " has added new merchandise successfully.");
        outputs.add(addMerchOutput);
    }

    /**
     * Adds a new event to the artist's events. If an event with the same name exists or the date is
     * not valid, the event will not be added.
     */
    @Override
    public void addEventReal(final Command command, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addEventOutput = mapper.createObjectNode();
        addEventOutput.put("command", "addEvent");
        addEventOutput.put("user", command.getUsername());
        addEventOutput.put("timestamp", command.getTimestamp());
        for (Event event : events) {
            if (event.getName().equals(command.getName())) {
                //event already exists
                addEventOutput.put("message", "Event already exists.");
                outputs.add(addEventOutput);
                return;
            }
        }
        //check if date is correct
        String[] date = command.getDate().split("-");
        if (date[1] == "02" && Integer.parseInt(date[2]) > MAX_DAYS_FEBRUARY) {
            addEventOutput.put("message",
                "Event for " + command.getUsername() + " does not have a valid date.");
            outputs.add(addEventOutput);
            return;
        }
        if (Integer.parseInt(date[0]) > MAX_DAYS_MONTH || Integer.parseInt(date[1]) > MAX_MONTH
            || Integer.parseInt(date[2]) < MIN_YEAR || Integer.parseInt(date[2]) > MAX_YEAR) {
            addEventOutput.put("message",
                "Event for " + command.getUsername() + " does not have a valid date.");
            outputs.add(addEventOutput);
            return;
        }
        Event event = new Event(command.getName(), command.getDate(), command.getDescription());
        events.add(event);
        Notification notification = new Notification("New Event from " + this.getUsername() + ".",
            "New Event");
        for (User subscriber : getSubscribers()) {
            subscriber.updateNotifications(notification);
        }
        addEventOutput.put("message", command.getUsername() + " has added new event successfully.");
        outputs.add(addEventOutput);
    }

    /**
     * Remove an album from the artist's albums. If the album doesn't exist or if the album is not
     * removable (a song within the album is in a queue), the album will not be removed.
     */
    @Override
    public void removeAlbumReal(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode removeAlbumOutput = mapper.createObjectNode();
        removeAlbumOutput.put("command", "removeAlbum");
        removeAlbumOutput.put("user", command.getUsername());
        removeAlbumOutput.put("timestamp", command.getTimestamp());
        for (User currUser : myLibrary.getUsers()) {
            if (currUser.getPlayer() != null) {
                currUser.getPlayer().update(command.getTimestamp(), currUser, myLibrary);
            }
        }
        Album album = null;
        for (Album album1 : albums) {
            if (album1.getName().equals(command.getName())) {
                //album exists
                album = album1;
                break;
            }
        }
        if (album == null) {
            removeAlbumOutput.put("message",
                command.getUsername() + " doesn't have an album with the given name.");
            outputs.add(removeAlbumOutput);
            return;
        }
        boolean removable = isRemovable(myLibrary, album);
        if (!removable) {
            removeAlbumOutput.put("message", command.getUsername() + " can't delete this album.");
            outputs.add(removeAlbumOutput);
            return;
        }
        albums.remove(album);
        myLibrary.getAlbums().remove(album);
        for (Song song : album.getSongs()) {
            for (Playlist playlist : myLibrary.getPlaylists()) {
                if (playlist.getSongs().contains(song)) {
                    playlist.getSongs().remove(song);
                }
            }
            for (User currUser : myLibrary.getUsers()) {
                if (currUser.getLikedSongs().contains(song)) {
                    currUser.getLikedSongs().remove(song);
                }
            }
            myLibrary.getSongs().remove(song);
        }
        removeAlbumOutput.put("message", this.getUsername() + " deleted the album successfully.");
        outputs.add(removeAlbumOutput);
    }

    /**
     * Checks if an album is removable.
     *
     * @return true if the album is removable, false otherwise
     */
    private static boolean isRemovable(final Library myLibrary, final Album album) {
        boolean removable = true;
        for (Song song : album.getSongs()) {
            for (User currUser : myLibrary.getUsers()) {
                if (currUser.getPlayer() != null) {
                    if (currUser.getPlayer().getQueue() != null) {
                        switch (currUser.getPlayer().getQueue().getType()) {
                            case SONG -> {
                                if (currUser.getPlayer().getQueue().equals(song)) {
                                    removable = false;
                                }
                            }
                            case ALBUM -> {
                                if (currUser.getPlayer().getQueue().equals(album)) {
                                    removable = false;
                                }
                            }
                            case PLAYLIST -> {
                                if (((Playlist) currUser.getPlayer().getQueue()).getSongs()
                                    .contains(song)) {
                                    removable = false;
                                }
                            }
                            default -> {
                                continue;
                            }
                        }

                    }
                }
            }
        }
        return removable;
    }

    /**
     * Remove an event from the artist's events. If the event doesn't exist, the event will not be
     * removed.
     */
    @Override
    public void removeEventReal(final Command command, final User user, final Library myLibrary,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode removeEventOutput = mapper.createObjectNode();
        removeEventOutput.put("command", "removeEvent");
        removeEventOutput.put("user", command.getUsername());
        removeEventOutput.put("timestamp", command.getTimestamp());
        Event event = null;
        for (Event event1 : events) {
            if (event1.getName().equals(command.getName())) {
                //event exists
                event = event1;
                break;
            }
        }
        if (event == null) {
            removeEventOutput.put("message", "Event does not exist.");
            outputs.add(removeEventOutput);
            return;
        }
        events.remove(event);
        removeEventOutput.put("message",
            command.getUsername() + " deleted the event successfully.");
        outputs.add(removeEventOutput);
    }

    /**
     * Add a listener to the artist's listeners. If the listener already exists, the number of
     * listens will be incremented.
     */
    public void addListener(final String name) {
        for (Listens listener : listeners) {
            if (listener.getName().equals(name)) {
                listener.addListen();
                return;
            }
        }
        Listens listener = new Listens(name);
        listeners.add(listener);
    }

    /**
     * Wrapped for artist. (top listeners, top songs, top albums)
     */
    @Override
    public void wrapped(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        //first update all users players
        for (User currUser : library.getUsers()) {
            if (currUser.getPlayer() != null) {
                currUser.getPlayer().update(command.getTimestamp(), currUser, library);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrappedOutput = mapper.createObjectNode();
        wrappedOutput.put("command", command.getCommand());
        wrappedOutput.put("user", command.getUsername());
        wrappedOutput.put("timestamp", command.getTimestamp());

        if (listeners.isEmpty()) {
            wrappedOutput.put("message",
                "No data to show for artist " + command.getUsername() + ".");
            outputs.add(wrappedOutput);
            return;
        }

        ObjectNode result = mapper.createObjectNode();
        ObjectNode albumsStuff = mapper.createObjectNode();
        ObjectNode songs = mapper.createObjectNode();
        ArrayNode fans = mapper.createArrayNode();

        getSongListens().sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        getAlbumListens().sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        listeners.sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                // sort by name
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });

        for (int i = 0; i < MAX_ITEMS; i++) {
            if (i < getSongListens().size()) {
                songs.put(getSongListens().get(i).getName(), getSongListens().get(i).getListens());
            }
        }
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (i < getAlbumListens().size()) {
                albumsStuff.put(getAlbumListens().get(i).getName(),
                    getAlbumListens().get(i).getListens());
            }
        }

        for (int i = 0; i < MAX_ITEMS; i++) {
            if (i < listeners.size()) {
                fans.add(listeners.get(i).getName());
            }
        }

        result.putPOJO("topAlbums", albumsStuff);
        result.putPOJO("topSongs", songs);
        result.putPOJO("topFans", fans);
        result.put("listeners", listeners.size());
        wrappedOutput.put("result", result);
        outputs.add(wrappedOutput);
    }

    /**
     * Used for calculating profit for each song.
     */
    public void addSongProfits(final String name, final double value) {
        for (Revenues songProfit : songProfits) {
            if (songProfit.getSongName().equals(name)) {
                songProfit.addRevenue(value);
                return;
            }
        }
        Revenues songProfit = new Revenues(name, value);
        songProfits.add(songProfit);
    }
}
