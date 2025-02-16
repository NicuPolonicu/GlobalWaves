package output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongStats {

    private String name;
    private Integer remainedTime;
    private String repeat;
    private boolean shuffle;
    private boolean paused;


    public SongStats(final String name, final Integer remainedTime, final String repeat,
        final boolean shuffle,
        final boolean paused) {
        this.name = name;
        this.remainedTime = remainedTime;
        this.repeat = repeat;
        this.shuffle = shuffle;
        this.paused = paused;
    }
}
