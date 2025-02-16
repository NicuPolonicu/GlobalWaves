package audio.files;

import audioenum.AudioEnum;
import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Episode extends AudioFile {

    private String description;

    public Episode(final EpisodeInput episode) {
        super(episode.getName(), episode.getDuration(), AudioEnum.EPISODE);
        description = episode.getDescription();
    }

}
