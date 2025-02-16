package audio.lists;

import enums.AudioEnum;
import lombok.Getter;
import lombok.Setter;
import user.User;

@Getter
@Setter
public class Album extends Playlist {

    private int likes;
    private String description;
    private int releaseYear;
    private int listens;

    public Album(final String name, final User owner, final int timeCreated,
        final String description, final int releaseYear) {
        super(name, owner, timeCreated, AudioEnum.ALBUM);
        likes = 0;
        this.description = description;
        this.releaseYear = releaseYear;
        listens = 0;
    }

    /**
     * Add a new listen to this album. (A song within the album was listened to).
     */
    public void addListen() {
        listens++;
    }


}
