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

    public PodcastProgress(final Podcast newPodcast) {
        this.podcast = newPodcast;
        this.episode = newPodcast.getEpisodes().get(0);
        timeListened = 0;
    }

    /**
     * Set the podcast progress of a user.
     *
     * @param setPodcast   is the podcast the user listened to.
     * @param newEpisode      is the episode the user remained at.
     * @param newTimeListened is how much the user listened from the episode.
     */
    public void setProgress(final Podcast setPodcast, final Episode newEpisode,
        final int newTimeListened) {
        this.podcast = setPodcast;
        this.episode = newEpisode;
        this.timeListened = newTimeListened;
    }
}
