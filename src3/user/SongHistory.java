package user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SongHistory {
    private String name;
    private String artist;
    private int listens;

    public SongHistory(final String name, final String artist) {
        this.name = name;
        this.artist = artist;
        this.listens = 1;
    }

    /**
     * Increase the number of listens for the song.
     */
    public void addListen() {
        this.listens++;
    }
}
