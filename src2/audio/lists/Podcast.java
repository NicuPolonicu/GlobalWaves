package audio.lists;

import audio.files.Episode;
import enums.AudioEnum;
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

    public Podcast(final String name, final ArrayList<EpisodeInput> episodes, final String owner) {
        super(name, AudioEnum.PODCAST);
        this.episodes = new ArrayList<>();
        for (EpisodeInput episode : episodes) {
            this.episodes.add(new Episode(episode));
        }
        this.owner = owner;
    }
}
