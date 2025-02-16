package specialusers;

import artistentities.Announcement;
import artistentities.Listens;
import audio.files.Episode;
import audio.lists.Library;
import audio.lists.Podcast;
import audio.lists.PodcastProgress;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import enums.UserType;
import fileio.input.EpisodeInput;
import java.util.ArrayList;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import user.User;

@Getter
@Setter
public final class Host extends User {

    private ArrayList<Podcast> podcasts;
    private ArrayList<Announcement> announcements;
    private ArrayList<String> listeners = new ArrayList<>();

    public Host(final String username, final int age, final String city) {
        super(username, age, city, UserType.HOST);
        this.podcasts = new ArrayList<>();
        this.announcements = new ArrayList<>();

    }

    @Override
    public void addPodcastReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        // create podcast
        // check if podcast already exists
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addPodcastOutput = mapper.createObjectNode();
        addPodcastOutput.put("command", "addPodcast");
        addPodcastOutput.put("user", command.getUsername());
        addPodcastOutput.put("timestamp", command.getTimestamp());

        for (Podcast podcast : podcasts) {
            if (podcast.getName().equals(command.getName())) {
                // podcast already exists
                addPodcastOutput.put("message",
                    command.getUsername() + " has another podcast with the same name.");
                outputs.add(addPodcastOutput);
                return;
            }
        }
        // create podcast
        // check if there is duplicate episode
        ArrayList<EpisodeInput> episodes = command.getEpisodes();
        ArrayList<String> episodeNames = new ArrayList<>();
        for (EpisodeInput episode : episodes) {
            episodeNames.add(episode.getName());
        }
        if (episodeNames.size() != Set.copyOf(episodeNames).size()) {
            // duplicate episode
            addPodcastOutput.put("message", "Duplicate episode.");
            outputs.add(addPodcastOutput);
            return;
        }
        // add podcast to library
        Podcast podcast = new Podcast(command.getName(), command.getEpisodes(),
            command.getUsername());
        myLibrary.getPodcasts().add(podcast);
        // add podcast to host
        podcasts.add(podcast);
        // add PodcastProgress to all users
        for (User user1 : myLibrary.getUsers()) {
            user1.getPodcastProgress().add(new PodcastProgress(podcast));
        }
        addPodcastOutput.put("message",
            command.getUsername() + " has added new podcast successfully.");
        outputs.add(addPodcastOutput);
    }

    @Override
    public void addAnnouncementReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addAnnouncementOutput = mapper.createObjectNode();
        addAnnouncementOutput.put("command", "addAnnouncement");
        addAnnouncementOutput.put("user", command.getUsername());
        addAnnouncementOutput.put("timestamp", command.getTimestamp());
        // check if event exists
        for (Announcement announcement : announcements) {
            if (announcement.getName().equals(command.getName())) {
                // event already exists
                addAnnouncementOutput.put("message", "Announcement already exists.");
                outputs.add(addAnnouncementOutput);
                return;
            }
        }
        // create event
        Announcement announcement = new Announcement(command.getName(), command.getDescription());
        announcements.add(announcement);
        addAnnouncementOutput.put("message",
            command.getUsername() + " has successfully added new announcement.");
        outputs.add(addAnnouncementOutput);
    }

    @Override
    public void removeAnnouncementReal(final Command command, final Library myLibrary,
        final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode removeAnnouncementOutput = mapper.createObjectNode();
        removeAnnouncementOutput.put("command", "removeAnnouncement");
        removeAnnouncementOutput.put("user", command.getUsername());
        removeAnnouncementOutput.put("timestamp", command.getTimestamp());
        // check if event exists
        for (Announcement announcement : announcements) {
            if (announcement.getName().equals(command.getName())) {
                // event exists
                announcements.remove(announcement);
                removeAnnouncementOutput.put("message",
                    command.getUsername() + " has successfully deleted the announcement.");
                outputs.add(removeAnnouncementOutput);
                return;
            }
        }
        // event doesn't exist
        removeAnnouncementOutput.put("message",
            command.getUsername() + " has no announcement with the given name.");
        outputs.add(removeAnnouncementOutput);
    }

    @Override
    public void showPodcastsReal(final Command command, final User user, final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode showPodcastsOutput = mapper.createObjectNode();
        showPodcastsOutput.put("command", "showPodcasts");
        showPodcastsOutput.put("user", command.getUsername());
        showPodcastsOutput.put("timestamp", command.getTimestamp());
        // make result array
        ArrayNode result = mapper.createArrayNode();
        for (Podcast podcast : podcasts) {
            //print podcast and its episodes
            ArrayNode episodes = mapper.createArrayNode();
            for (Episode episode : podcast.getEpisodes()) {
                episodes.add(episode.getName());
            }
            ObjectNode podcastNode = mapper.createObjectNode();
            podcastNode.put("name", podcast.getName());
            podcastNode.putPOJO("episodes", episodes);
            result.add(podcastNode);
        }
        // podcast doesn't exist
        showPodcastsOutput.putPOJO("result", result);
        outputs.add(showPodcastsOutput);
    }

    @Override
    public void removePodcastReal(final Command command, final Library myLibrary, final User user,
        final ArrayNode outputs) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode removePodcastOutput = mapper.createObjectNode();
        removePodcastOutput.put("command", "removePodcast");
        removePodcastOutput.put("user", command.getUsername());
        removePodcastOutput.put("timestamp", command.getTimestamp());

        for (User currUser : myLibrary.getUsers()) {
            if (currUser.getPlayer() != null) {
                currUser.getPlayer().update(command.getTimestamp(), currUser, myLibrary);
            }
        }
        // check if podcast exists
        Podcast podcast = null;
        for (Podcast currPodcast : podcasts) {
            if (currPodcast.getName().equals(command.getName())) {
                // podcast exists
                podcast = currPodcast;
                break;
            }
        }
        if (podcast == null) {
            // podcast doesn't exist
            removePodcastOutput.put("message",
                command.getUsername() + " doesn't have a podcast with the given name.");
            outputs.add(removePodcastOutput);
            return;
        }
        // check if podcast is loaded anywhere
        for (User currUser : myLibrary.getUsers()) {
            if (currUser.getPlayer() != null) {
                if (currUser.getPlayer().getQueue() != null) {
                    switch (currUser.getPlayer().getQueue().getType()) {
                        case PODCAST -> {
                            if (((Podcast) currUser.getPlayer().getQueue()).getName()
                                .equals(command.getName())) {
                                // podcast is loaded
                                removePodcastOutput.put("message",
                                    command.getUsername() + " can't delete this podcast.");
                                outputs.add(removePodcastOutput);
                                return;
                            }
                        }
                        default -> {
                            continue;
                        }
                    }
                }
            }
        }
        // if we got here, podcast is not loaded anywhere
        // remove podcast from library
        myLibrary.getPodcasts().remove(podcast);
        // remove podcast from host
        podcasts.remove(podcast);
        // remove podcast from all users
        for (User currUser : myLibrary.getUsers()) {
            for (PodcastProgress podcastProgress : currUser.getPodcastProgress()) {
                if (podcastProgress.getPodcast().getName().equals(command.getName())) {
                    currUser.getPodcastProgress().remove(podcastProgress);
                    break;
                }
            }
        }
        removePodcastOutput.put("message",
            command.getUsername() + " deleted the podcast successfully.");
        outputs.add(removePodcastOutput);
    }

    @Override
    public void wrapped(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrappedOutput = mapper.createObjectNode();
        wrappedOutput.put("command", "wrapped");
        wrappedOutput.put("user", command.getUsername());
        wrappedOutput.put("timestamp", command.getTimestamp());
        // make result array
        ObjectNode result = mapper.createObjectNode();
        getEpisodeListens().sort((o1, o2) -> {
            if (o1.getListens() == o2.getListens()) {
                return o1.getName().compareTo(o2.getName());
            }
            return o2.getListens() - o1.getListens();
        });
        ObjectNode episodes = mapper.createObjectNode();
        for (Listens episodeListen : getEpisodeListens()) {
            episodes.put(episodeListen.getName(), episodeListen.getListens());
        }
        result.putPOJO("topEpisodes", episodes);
        result.put("listeners", listeners.size());
        wrappedOutput.putPOJO("result", result);
        outputs.add(wrappedOutput);
    }

    /**
     * Add a listener to the host's listeners list. If the listener is already in the list, add a
     * listen.
     */
    public void addListener(final String username) {
        if (!listeners.contains(username)) {
            listeners.add(username);
        }
    }
}
