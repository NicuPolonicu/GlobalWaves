package audio.files;

import java.util.ArrayList;

import enums.AudioEnum;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;
import user.User;

@Getter
@Setter
public class Song extends AudioFile {

    private String album;
    private ArrayList<String> tags;
    private String lyrics;
    private String genre;
    private int releaseYear;
    private String artist;
    private ArrayList<User> likes;
    private int listens;


    public Song(final SongInput song) {
        super(song.getName(), song.getDuration(), AudioEnum.SONG);
        tags = new ArrayList<>();
        for (String tag : song.getTags()) {
            tags.add(tag);
        }
        likes = new ArrayList<>();
        album = song.getAlbum();
        tags = song.getTags();
        lyrics = song.getLyrics();
        genre = song.getGenre();
        releaseYear = song.getReleaseYear();
        artist = song.getArtist();
        listens = 0;
    }

    /**
     * Add a like to the song.
     *
     * @param user is the user that liked the song
     */
    public void addLike(final User user) {
        likes.add(user);
    }

    /**
     * Remove a like.
     *
     * @param user is the user that unliked the song
     */
    public void removeLike(final User user) {
        likes.remove(user);
    }

    /**
     * Increase the number of listens for the song.
     */

}
