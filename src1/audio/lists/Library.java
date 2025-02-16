package audio.lists;

import audio.files.Song;
import fileio.input.LibraryInput;
import fileio.input.UserInput;
import fileio.input.SongInput;
import fileio.input.PodcastInput;
import lombok.Getter;
import lombok.Setter;
import user.User;
import java.util.ArrayList;

@Getter
@Setter
public final class Library {

    private ArrayList<Song> songs;
    private ArrayList<Podcast> podcasts;
    private ArrayList<User> users;
    private ArrayList<Playlist> playlists;

    public Library(final LibraryInput library) {
        songs = new ArrayList<>();
        podcasts = new ArrayList<>();
        users = new ArrayList<>();
        playlists = new ArrayList<>();

        for (SongInput song : library.getSongs()) {
            songs.add(new Song(song));
        }
        for (PodcastInput podcast : library.getPodcasts()) {
            podcasts.add(new Podcast(podcast));
        }
        for (UserInput user : library.getUsers()) {
            users.add(new User(user, this));
        }
    }

    /**
     * Find a user's data (such as player, search bar, etc.)
     * @param username is the user's username
     * @return the user's data object (User object) or NULL if a
     * user with this username doesn't exist.
     */
    public User findUser(final String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
