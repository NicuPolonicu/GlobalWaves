package audio.lists;

import audio.files.Episode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodcastProgress {

    private Podcast podcast;
    private Episode episode;
    private int timeListened;

    public PodcastProgress(final Podcast podcast) {
        this.podcast = podcast;
        this.episode = podcast.getEpisodes().get(0);
        timeListened = 0;
    }

    /**
     * Set the podcast progress of a user.
     *
     * @param podcast      is the podcast the user listened to.
     * @param episode      is the episode the user remained at.
     * @param timeListened is how much the user listened from the episode.
     */
    public void setProgress(final Podcast podcast, final Episode episode, final int timeListened) {
        this.podcast = podcast;
        this.episode = episode;
        this.timeListened = timeListened;
    }
}
