package audio.lists;


import java.util.ArrayList;
import audioenum.AudioEnum;
import lombok.Getter;
import lombok.Setter;
import user.User;
import audio.files.Song;

@Getter
@Setter
public final class Playlist extends AudioList {

    private User owner;
    private ArrayList<User> followers;
    private ArrayList<Song> songs;
    private Integer isPrivate = 0;
    private int timeCreated;
    private ArrayList<Song> shuffledPlaylist = null;

    public Playlist(final String name, final User owner, final int timeCreated) {
        super(name, AudioEnum.PLAYLIST);
        this.owner = owner;
        this.timeCreated = timeCreated;
        followers = new ArrayList<>();
        songs = new ArrayList<>();
    }

    /**
     * Add a follower to this playlist.
     */
    public void addFollower(final User user) {
        followers.add(user);
    }

    /**
     * Remove a follower from this playlist.
     */
    public void removeFollower(final User user) {
        followers.remove(user);
    }

}
