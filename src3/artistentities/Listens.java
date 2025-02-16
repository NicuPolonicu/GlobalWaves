package artistentities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Listens {

    private String name;
    private int listens;

    public Listens(final String name) {
        this.name = name;
        this.listens = 1;
    }

    /**
     * Increase the number of listens for the song/artist/...
     */
    public void addListen() {
        this.listens++;
    }
}
