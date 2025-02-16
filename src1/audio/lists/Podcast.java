package audio.lists;

import audio.files.Episode;
import audioenum.AudioEnum;
import fileio.input.PodcastInput;
import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Podcast extends AudioList {

    private ArrayList<Episode> episodes;
    private String owner;

    public Podcast(final PodcastInput podcast) {
        super(podcast.getName(), AudioEnum.PODCAST);
        episodes = new ArrayList<>();
        owner = podcast.getOwner();
        // deep copy, not shallow copy
        for (EpisodeInput episode : podcast.getEpisodes()) {
            episodes.add(new Episode(episode));
        }
    }
}
