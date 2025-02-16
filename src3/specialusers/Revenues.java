package specialusers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Revenues {

    private String songName;
    private double revenue;

    public Revenues(final String songName, final double revenue) {
        this.songName = songName;
        this.revenue = revenue;
    }

    /**
     * Add new revenue to this song.
     */
    public void addRevenue(final double rev) {
        this.revenue += rev;
    }
}
