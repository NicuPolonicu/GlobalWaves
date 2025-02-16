package player;


import audio.files.Episode;
import audio.lists.Library;
import audio.lists.Playlist;
import audio.lists.Podcast;
import audio.lists.PodcastProgress;
import audio.item.AudioItem;
import enums.AudioEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.Command;
import lombok.Getter;
import lombok.Setter;
import specialusers.Artist;
import specialusers.Host;
import user.SongHistory;
import user.User;
import enums.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import audio.files.AudioFile;
import output.SongStats;
import audio.files.Song;


@Getter
@Setter
public final class Player {

    private static final int FW_BW_TIME = 90;
    private static final double PREMIUM_MONEY = 1000000;
    private boolean isPlaying = false;
    private boolean shuffle = false;
    private int lastCheck = 0;
    private int repeatMode;
    private int timeListened;
    private AudioItem queue;
    private AudioFile currentFile;
    private ArrayList<String> songRepeatMessage = new ArrayList<>(
        Arrays.asList("No Repeat", "Repeat Once", "Repeat Infinite"));
    private ArrayList<String> podcastRepeatMessage = new ArrayList<>(
        Arrays.asList("No Repeat", "Repeat Once", "Repeat Infinite"));
    private ArrayList<String> playlistRepeatMessage = new ArrayList<>(
        Arrays.asList("No Repeat", "Repeat All", "Repeat Current Song"));
    private ArrayList<AudioFile> shuffledSongs;
    private ArrayList<SongHistory> songHistory = new ArrayList<>();
    private ArrayList<SongHistory> premiumSongHistory = new ArrayList<>();
    private Song ad;
    private boolean adBreak = false;
    private double adPrice = 0;
    private int lastIndex = 0;

    public Player(final Library myLibrary) {
        this.ad = myLibrary.getSongs().get(0);
    }

    /**
     * Load the selected audio item to the user's player. NOTE: There must be something selected in
     * the searchbar, be it a playlist, podcast or song.
     */
    public void load(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loadOutput = mapper.createObjectNode();

        if (user.getSearchBar().getSelected() == null) {
            loadOutput.put("command", "load");
            loadOutput.put("user", command.getUsername());
            loadOutput.put("timestamp", command.getTimestamp());
            loadOutput.put("message", "Please select a source before attempting to load.");
            outputs.add(loadOutput);
            return;
        }
        queue = user.getSearchBar().getSelected();
        user.getSearchBar().setSearchResults(new ArrayList<>());
        user.getSearchBar().setSelected(null);
        isPlaying = true;
        lastCheck = command.getTimestamp();
        timeListened = 0;
        adBreak = false;
        adPrice = 0;
        switch (queue.getType()) {
            case PLAYLIST, ALBUM -> {
                currentFile = ((Playlist) queue).getSongs().get(0);
                ((Playlist) queue).setShuffledPlaylist(((Playlist) queue).getSongs());
                addSongStat(user, library);
            }
            case PODCAST -> {
                for (PodcastProgress progress : user.getPodcastProgress()) {
                    if (progress.getPodcast().equals(queue)) {
                        currentFile = progress.getEpisode();
                        timeListened = progress.getTimeListened();
                        user.addEpisodeListen(currentFile.getName());
                        Host host = (Host) library.findUser(progress.getPodcast().getOwner());
                        host.addEpisodeListen(currentFile.getName());
                        host.addListener(user.getUsername());
                        break;
                    }
                }
            }
            case SONG -> {
                currentFile = (Song) queue;
                addSongStat(user, library);
            }
            default -> {
                return;
            }
        }
        loadOutput.put("command", "load");
        loadOutput.put("user", command.getUsername());
        loadOutput.put("timestamp", command.getTimestamp());
        loadOutput.put("message", "Playback loaded successfully.");
        outputs.add(loadOutput);
    }

    private void addSongStat(final User user, final Library library) {
        Song currentSong = (Song) currentFile;
        user.addArtistListen(currentSong.getArtist());
        user.addGenreListen(currentSong.getGenre());
        user.addAlbumListen(currentSong.getAlbum());
        user.addSongListen(currentSong.getName());
        Artist artist = library.findArtist(currentSong.getArtist());
        artist.addSongListen(currentSong.getName());
        artist.addAlbumListen(currentSong.getAlbum());
        artist.addListener(user.getUsername());
        if (!user.isPremium()) {
            addSongHistory(currentSong.getName(), currentSong.getArtist());
        } else {
            addPremiumSongHistory(currentSong.getName(), currentSong.getArtist());
        }
    }


    /**
     * Print what song/episode the user is currently listening to.
     */
    public void status(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode statusOutput = mapper.createObjectNode();
        statusOutput.put("command", "status");
        statusOutput.put("user", command.getUsername());
        statusOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (currentFile == null) {
            SongStats songStats = new SongStats("", 0, "No Repeat", false, true);
            statusOutput.putPOJO("stats", songStats);
        } else {
            boolean isPaused = !isPlaying;
            switch (queue.getType()) {
                case SONG -> {
                    SongStats songStats = new SongStats(currentFile.getName(),
                        currentFile.getDuration() - timeListened, songRepeatMessage.get(repeatMode),
                        shuffle, isPaused);
                    statusOutput.putPOJO("stats", songStats);
                }
                case PODCAST -> {
                    SongStats songStats = new SongStats(currentFile.getName(),
                        currentFile.getDuration() - timeListened,
                        podcastRepeatMessage.get(repeatMode), shuffle, isPaused);
                    statusOutput.putPOJO("stats", songStats);
                }
                case PLAYLIST, ALBUM -> {
                    SongStats songStats = new SongStats(currentFile.getName(),
                        currentFile.getDuration() - timeListened,
                        playlistRepeatMessage.get(repeatMode), shuffle, isPaused);
                    statusOutput.putPOJO("stats", songStats);
                }
                default -> {
                    return;
                }
            }
        }
        outputs.add(statusOutput);
    }

    /**
     * When we want to check the status of a user's Player, we need to update it according to the
     * new timestamp. This function updates the Player to reflect how much time has passed.
     */
    public void update(final int timestamp, final User user, final Library library) {
        if (user.getStatus() == Status.OFFLINE) {
            lastCheck = timestamp;
            return;
        }
        if (!isPlaying || queue == null) {
            return;
        }

        switch (queue.getType()) {
            case SONG -> {
                if (timeListened + (timestamp - lastCheck) >= ((Song) queue).getDuration()) {
                    if (repeatMode == 0) {
                        if (adBreak) {
                            payout(user, library);
                            int listenLeft =
                                timeListened + timestamp - lastCheck - ((Song) queue).getDuration();
                            adBreak = false;
                            currentFile = ad;
                            if (listenLeft > ad.getDuration()) {
                                timeListened = 0;
                                currentFile = null;
                                lastCheck = timestamp;
                                isPlaying = false;
                                queue = null;
                                // pay the artists
                                return;
                            }
                            timeListened = listenLeft;
                            lastCheck = timestamp;
                            return;
                        }
                        if (currentFile.getName().equals("Ad Break")) {
                            payout(user, library);
                        }
                        isPlaying = false;
                        currentFile = null;
                        queue = null;
                        timeListened = 0;
                    } else if (repeatMode == 1) {
                        repeatMode = 0;
                        timeListened =
                            timeListened + (timestamp - lastCheck) - ((Song) queue).getDuration();
                        if (timeListened >= ((Song) queue).getDuration()) {
                            timeListened = 0;
                            isPlaying = false;
                            queue = null;
                        }
                    } else {
                        timeListened += (timestamp - lastCheck) % ((Song) queue).getDuration();
                        timeListened %= ((Song) queue).getDuration();
                    }
                } else {
                    timeListened += timestamp - lastCheck;
                }
            }
            case PODCAST -> {
                int playedTime = timestamp - lastCheck;
                int idx = ((Podcast) queue).getEpisodes().indexOf((Episode) currentFile);
                for (Episode episode : ((Podcast) queue).getEpisodes()
                    .subList(idx, ((Podcast) queue).getEpisodes().size())) {
                    if (timeListened + playedTime >= episode.getDuration()) {

                        if (idx < ((Podcast) queue).getEpisodes().size() - 1) {
                            currentFile = ((Podcast) queue).getEpisodes().get(idx + 1);
                            user.addEpisodeListen(currentFile.getName());
                            Host host = (Host) library.findUser(((Podcast) queue).getOwner());
                            host.addEpisodeListen(currentFile.getName());
                        }
                        idx++;
                        playedTime -= (episode.getDuration() - timeListened);
                        timeListened = 0;
                    } else {
                        timeListened += playedTime;
                        currentFile = episode;
                        playedTime = 0;
                        break;
                    }
                }
                if (playedTime > 0) {
                    isPlaying = false;
                    currentFile = null;
                    queue = null;
                }
                for (PodcastProgress progress : user.getPodcastProgress()) {
                    if (progress.getPodcast().equals(queue)) {
                        progress.setProgress((Podcast) queue, (Episode) currentFile, timeListened);
                    }
                }
            }
            case PLAYLIST, ALBUM -> {
                if (!shuffle) {
                    int playedTime = timestamp - lastCheck;
                    int idx = ((Playlist) queue).getSongs().indexOf((Song) currentFile);
                    if (idx == -1) {
                        // i am in the ad
                        if (playedTime + timeListened >= ad.getDuration()) {
                            currentFile = ((Playlist) queue).getSongs().get(lastIndex + 1);
                            idx = lastIndex + 1;
                        }
                    }
                    for (Song song : ((Playlist) queue).getSongs()
                        .subList(idx, ((Playlist) queue).getSongs().size())) {
                        if (timeListened + playedTime >= song.getDuration()) {
                            if (repeatMode == 2) {
                                timeListened += playedTime;
                                timeListened %= song.getDuration();
                                lastCheck = timestamp;
                                return;
                            }
                            if (adBreak) {
                                payout(user, library);
                                adBreak = false;
                                adPrice = 0;
                                currentFile = ad;
                                // remember position of last song
                                lastIndex = idx;
                                int timeLeft = timeListened + playedTime - song.getDuration();
                                if (timeLeft < ad.getDuration()) {
                                    timeListened = timeLeft;
                                    lastCheck = timestamp;
                                    return;
                                } else {
                                    // return to playlist
                                    timeListened = 0;
                                    currentFile = ((Playlist) queue).getSongs().get(lastIndex);
                                    lastCheck = timestamp;
                                }
                            }
                            if (idx < ((Playlist) queue).getSongs().size() - 1) {
                                currentFile = ((Playlist) queue).getSongs().get(idx + 1);
                                addSongStat(user, library);
                            }
                            idx++;
                            playedTime -= (song.getDuration() - timeListened);
                            timeListened = 0;
                        } else {
                            timeListened += playedTime;
                            currentFile = song;
                            playedTime = 0;
                            break;
                        }
                    }
                    if (playedTime > 0) {
                        if (repeatMode == 1) {
                            while (playedTime > 0) {
                                currentFile = ((Playlist) queue).getSongs().get(0);
                                timeListened = 0;
                                idx = 0;
                                for (Song song : ((Playlist) queue).getSongs()) {
                                    if (playedTime > song.getDuration()) {
                                        if (idx < ((Playlist) queue).getSongs().size() - 1) {
                                            currentFile = ((Playlist) queue).getSongs()
                                                .get(idx + 1);
                                        }
                                        playedTime -= song.getDuration();
                                    } else {
                                        timeListened = playedTime;
                                        currentFile = song;
                                        playedTime = 0;
                                        break;
                                    }
                                }
                            }
                        } else {
                            queue = null;
                            isPlaying = false;
                            currentFile = null;
                            timeListened = 0;
                        }

                    }
                } else {
                    int playedTime = timestamp - lastCheck;
                    int idx = ((Playlist) queue).getShuffledPlaylist().indexOf((Song) currentFile);
                    if (idx == -1) {
                        return;
                    }
                    for (Song song : ((Playlist) queue).getShuffledPlaylist()
                        .subList(idx, ((Playlist) queue).getShuffledPlaylist().size())) {
                        if (timeListened + playedTime >= song.getDuration()) {
                            if (repeatMode == 2) {
                                timeListened += playedTime;
                                timeListened %= song.getDuration();
                                lastCheck = timestamp;
                                return;
                            }
                            if (idx < ((Playlist) queue).getSongs().size() - 1) {
                                currentFile = ((Playlist) queue).getSongs().get(idx + 1);
                            }
                            idx++;
                            playedTime -= (song.getDuration() - timeListened);
                            timeListened = 0;
                        } else {
                            timeListened += playedTime;
                            currentFile = song;
                            playedTime = 0;
                            break;
                        }
                    }
                    if (playedTime > 0) {
                        if (repeatMode == 1) {
                            while (playedTime > 0) {
                                currentFile = ((Playlist) queue).getShuffledPlaylist().get(0);
                                timeListened = 0;
                                idx = 0;
                                for (Song song : ((Playlist) queue).getShuffledPlaylist()) {
                                    if (playedTime > song.getDuration()) {
                                        if (idx
                                            < ((Playlist) queue).getShuffledPlaylist().size() - 1) {
                                            currentFile = ((Playlist) queue).getShuffledPlaylist()
                                                .get(idx + 1);
                                        }
                                        playedTime -= song.getDuration();
                                    } else {
                                        timeListened = playedTime;
                                        currentFile = song;
                                        playedTime = 0;
                                        break;
                                    }
                                }
                            }
                        } else {
                            queue = null;
                            isPlaying = false;
                            currentFile = null;
                        }

                    }
                }
            }

            default -> {
                return;
            }
        }
        lastCheck = timestamp;
    }

    /**
     * Pay artists for user's listens.
     */
    public void payout(final User user, final Library library) {
        double money;
        if (user.isPremium()) {
            money = PREMIUM_MONEY;
        } else {
            money = adPrice;
        }
        double listens = 0, songQuant;
        if (!user.isPremium()) {
            for (SongHistory song : songHistory) {
                listens += song.getListens();
            }
        } else {
            for (SongHistory song : premiumSongHistory) {
                listens += song.getListens();
            }
        }
        songQuant = money / listens;
        if (!user.isPremium()) {
            for (SongHistory song : songHistory) {
                Artist artist = library.findArtist(song.getArtist());
                artist.addSongProfits(song.getName(), songQuant * song.getListens());
                artist.setSongRevenue(artist.getSongRevenue() + songQuant * song.getListens());
            }
            // empty history
            songHistory.clear();
        } else {
            for (SongHistory song : premiumSongHistory) {
                Artist artist = library.findArtist(song.getArtist());
                artist.addSongProfits(song.getName(), songQuant * song.getListens());
                artist.setSongRevenue(artist.getSongRevenue() + songQuant * song.getListens());
            }
            // empty history
            premiumSongHistory.clear();
        }
    }

    /**
     * Resume or pause the current episode/song the user is listening to.
     */
    public void playPause(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode playPauseOutput = mapper.createObjectNode();
        playPauseOutput.put("command", "playPause");
        playPauseOutput.put("user", command.getUsername());
        playPauseOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            playPauseOutput.put("message",
                "Please load a source before attempting to pause or resume playback.");
            outputs.add(playPauseOutput);
            return;
        }
        if (!isPlaying) {
            isPlaying = true;
            lastCheck = command.getTimestamp();
            playPauseOutput.put("message", "Playback resumed successfully.");
        } else {
            isPlaying = false;
            playPauseOutput.put("message", "Playback paused successfully.");
        }
        outputs.add(playPauseOutput);
    }

    /**
     * Add (or remove) the loaded song to one of the user's playlists.
     */
    public void addRemoveInPlaylist(final Command command, final User user,
        final ArrayNode outputs, final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode addRemoveToPlaylistOutput = mapper.createObjectNode();
        addRemoveToPlaylistOutput.put("command", "addRemoveInPlaylist");
        addRemoveToPlaylistOutput.put("user", command.getUsername());
        addRemoveToPlaylistOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (currentFile == null) {
            addRemoveToPlaylistOutput.put("message",
                "Please load a source before adding to or removing from the playlist.");
            outputs.add(addRemoveToPlaylistOutput);
            return;
        }
        if (currentFile.getType() == AudioEnum.EPISODE) {
            addRemoveToPlaylistOutput.put("message", "The loaded source is not a song.");
            outputs.add(addRemoveToPlaylistOutput);
            return;
        }
        if (command.getPlaylistId() > user.getPlaylists().size()) {
            addRemoveToPlaylistOutput.put("message", "The specified playlist does not exist.");
            outputs.add(addRemoveToPlaylistOutput);
            return;
        }
        Playlist playlist = user.getPlaylists().get(command.getPlaylistId() - 1);
        if (playlist.getSongs().contains(((Song) currentFile))) {
            playlist.getSongs().remove(currentFile);
            addRemoveToPlaylistOutput.put("message", "Successfully removed from playlist.");
        } else {
            playlist.getSongs().add(((Song) currentFile));
            addRemoveToPlaylistOutput.put("message", "Successfully added to playlist.");
        }
        outputs.add(addRemoveToPlaylistOutput);
    }

    /**
     * Set the repeat mode of the Player.
     */
    public void repeat(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode repeatOutput = mapper.createObjectNode();
        repeatOutput.put("command", "repeat");
        repeatOutput.put("user", command.getUsername());
        repeatOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            repeatOutput.put("message", "Please load a source before setting the repeat status.");
            outputs.add(repeatOutput);
            return;
        }
        switch (queue.getType()) {
            case SONG -> {
                if (repeatMode == 0) {
                    repeatMode = 1;
                    repeatOutput.put("message", "Repeat mode changed to repeat once.");
                } else if (repeatMode == 1) {
                    repeatMode = 2;
                    repeatOutput.put("message", "Repeat mode changed to repeat infinite.");
                } else {
                    repeatMode = 0;
                    repeatOutput.put("message", "Repeat mode changed to no repeat.");
                }
            }
            case PODCAST -> {
                if (repeatMode == 0) {
                    repeatMode = 1;
                    repeatOutput.put("message", "Repeat mode changed to repeat once.");
                } else if (repeatMode == 1) {
                    repeatMode = 2;
                    repeatOutput.put("message", "Repeat mode changed to repeat infinite.");
                } else {
                    repeatMode = 0;
                    repeatOutput.put("message", "Repeat mode changed to no repeat.");
                }
            }
            case PLAYLIST, ALBUM -> {
                if (repeatMode == 0) {
                    repeatMode = 1;
                    repeatOutput.put("message", "Repeat mode changed to repeat all.");
                } else if (repeatMode == 1) {
                    repeatMode = 2;
                    repeatOutput.put("message", "Repeat mode changed to repeat current song.");
                } else {
                    repeatMode = 0;
                    repeatOutput.put("message", "Repeat mode changed to no repeat.");
                }
            }
            default -> {
                return;
            }
        }
        outputs.add(repeatOutput);
    }

    /**
     * Set the shuffle mode of the current playlist. NOTE: a seed for the Random function must be
     * provided through the Command object.
     */
    public void shuffle(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode shuffleOutput = mapper.createObjectNode();
        shuffleOutput.put("command", "shuffle");
        shuffleOutput.put("user", command.getUsername());
        shuffleOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null || currentFile == null) {
            shuffleOutput.put("message", "Please load a source before using the shuffle function.");
            outputs.add(shuffleOutput);
            return;
        }
        if (queue.getType() != AudioEnum.PLAYLIST && queue.getType() != AudioEnum.ALBUM) {
            shuffleOutput.put("message", "The loaded source is not a playlist or an album.");
            outputs.add(shuffleOutput);
            return;
        }
        if (!shuffle) {
            shuffle = true;
            ((Playlist) queue).setShuffledPlaylist(
                new ArrayList<Song>(((Playlist) queue).getSongs()));
            Collections.shuffle(((Playlist) queue).getShuffledPlaylist(),
                new Random(command.getSeed()));
            shuffleOutput.put("message", "Shuffle function activated successfully.");
        } else {
            shuffle = false;
            ((Playlist) queue).setShuffledPlaylist(null);
            shuffleOutput.put("message", "Shuffle function deactivated successfully.");
        }
        outputs.add(shuffleOutput);
    }

    /**
     * Play the next song/episode.
     */
    public void next(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode nextOutput = mapper.createObjectNode();
        nextOutput.put("command", "next");
        nextOutput.put("user", command.getUsername());
        nextOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            nextOutput.put("message", "Please load a source before skipping to the next track.");
            outputs.add(nextOutput);
            return;
        }
        if (queue.getType() == AudioEnum.SONG) {
            if (repeatMode == 0) {
                queue = null;
                isPlaying = false;
                currentFile = null;
                nextOutput.put("message",
                    "Please load a source before skipping to the next track.");
            } else if (repeatMode == 1) {
                currentFile = (Song) queue;
                timeListened = 0;
                nextOutput.put("message",
                    "Skipped to next track successfully. The current track is "
                        + currentFile.getName() + ".");
            } else {
                timeListened = 0;
                nextOutput.put("message",
                    "Skipped to next track successfully. The current track is "
                        + currentFile.getName() + ".");
            }
        } else if (queue.getType() == AudioEnum.PODCAST) {
            int idx = ((Podcast) queue).getEpisodes().indexOf((Episode) currentFile);
            if (idx == ((Podcast) queue).getEpisodes().size() - 1) {
                nextOutput.put("message",
                    "Please load a source before skipping to the next track.");
                queue = null;
                isPlaying = false;
                currentFile = null;
                outputs.add(nextOutput);
                return;
            }
            currentFile = ((Podcast) queue).getEpisodes().get(idx + 1);
            timeListened = 0;
            nextOutput.put("message",
                "Skipped to next track successfully. The current track is " + currentFile.getName()
                    + ".");
        } else if (queue.getType() == AudioEnum.PLAYLIST || queue.getType() == AudioEnum.ALBUM) {
            if (repeatMode == 2) {
                timeListened = 0;
                lastCheck = command.getTimestamp();
                // print remaining time and timestamp please
                isPlaying = true;
                nextOutput.put("message",
                    "Skipped to next track successfully. The current track is "
                        + currentFile.getName() + ".");
            } else {
                int idx;
                if (shuffle) {
                    idx = ((Playlist) queue).getShuffledPlaylist().indexOf((Song) currentFile);
                    if (idx == ((Playlist) queue).getShuffledPlaylist().size() - 1) {
                        if (repeatMode == 0) {
                            queue = null;
                            isPlaying = false;
                            currentFile = null;
                            nextOutput.put("message",
                                "Please load a source before skipping to the next track.");
                            outputs.add(nextOutput);
                            return;
                        } else if (repeatMode == 1) {
                            currentFile = ((Playlist) queue).getShuffledPlaylist().get(0);
                            timeListened = 0;
                            isPlaying = true;
                            nextOutput.put("message",
                                "Skipped to next track successfully. The current track is "
                                    + currentFile.getName() + ".");
                            outputs.add(nextOutput);
                            return;
                        }
                        nextOutput.put("message",
                            "Please load a source before skipping to the next track.");
                        outputs.add(nextOutput);
                        return;
                    }
                    currentFile = ((Playlist) queue).getShuffledPlaylist().get(idx + 1);
                } else {
                    idx = ((Playlist) queue).getSongs().indexOf((Song) currentFile);
                    if (idx == ((Playlist) queue).getSongs().size() - 1) {
                        if (repeatMode == 0) {
                            queue = null;
                            isPlaying = false;
                            currentFile = null;
                            timeListened = 0;
                            nextOutput.put("message",
                                "Please load a source before skipping to the next track.");
                            outputs.add(nextOutput);
                            return;
                        } else if (repeatMode == 1) {
                            currentFile = ((Playlist) queue).getSongs().get(0);
                            timeListened = 0;
                            isPlaying = true;
                            nextOutput.put("message",
                                "Skipped to next track successfully. The current track is "
                                    + currentFile.getName() + ".");
                            outputs.add(nextOutput);
                            return;
                        }
                    }
                    currentFile = ((Playlist) queue).getSongs().get(idx + 1);
                }
                lastCheck = command.getTimestamp();
                timeListened = 0;
                isPlaying = true;
                nextOutput.put("message",
                    "Skipped to next track successfully. The current track is "
                        + currentFile.getName() + ".");
            }
        }
        outputs.add(nextOutput);
    }

    /**
     * Play the previous song/episode (if we've already played at least 1 second of the current
     * song/episode) or restart the current one.
     */
    public void prev(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode prevOutput = mapper.createObjectNode();
        prevOutput.put("command", "prev");
        prevOutput.put("user", command.getUsername());
        prevOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            prevOutput.put("message",
                "Please load a source before returning to the previous track.");
            outputs.add(prevOutput);
            return;
        }
        if (queue.getType() == AudioEnum.SONG) {
            timeListened = 0;
            isPlaying = true;
            prevOutput.put("message",
                "Returned to previous track successfully. The current track is "
                    + currentFile.getName() + ".");
        } else if (queue.getType() == AudioEnum.PODCAST) {
            int idx = ((Podcast) queue).getEpisodes().indexOf((Episode) currentFile);
            if (timeListened > 0 || idx == 0) {
                timeListened = 0;
                isPlaying = true;
                lastCheck = command.getTimestamp();
                prevOutput.put("message",
                    "Returned to previous track successfully. The current track is "
                        + currentFile.getName() + ".");
                outputs.add(prevOutput);
                return;
            }
            currentFile = ((Podcast) queue).getEpisodes().get(idx - 1);
            timeListened = 0;
            prevOutput.put("message",
                "Returned to previous track successfully. The current track is "
                    + currentFile.getName() + ".");
        } else if (queue.getType() == AudioEnum.PLAYLIST || queue.getType() == AudioEnum.ALBUM) {
            if (timeListened > 0) {
                timeListened = 0;
                lastCheck = command.getTimestamp();
                isPlaying = true;
                prevOutput.put("message",
                    "Returned to previous track successfully. The current track is "
                        + currentFile.getName() + ".");
                outputs.add(prevOutput);
                return;
            }
            int idx;
            if (shuffle) {
                idx = ((Playlist) queue).getShuffledPlaylist().indexOf((Song) currentFile);
                if (idx == 0) {
                    timeListened = 0;
                    prevOutput.put("message",
                        "Returned to previous track successfully. The current track is "
                            + currentFile.getName() + ".");
                } else {
                    currentFile = ((Playlist) queue).getShuffledPlaylist().get(idx - 1);
                    timeListened = 0;
                    prevOutput.put("message",
                        "Returned to previous track successfully. The current track is "
                            + currentFile.getName() + ".");
                }

            } else {
                idx = ((Playlist) queue).getSongs().indexOf((Song) currentFile);
                if (idx == 0) {
                    timeListened = 0;
                    prevOutput.put("message",
                        "Returned to previous track successfully. The current track is "
                            + currentFile.getName() + ".");
                } else {
                    currentFile = ((Playlist) queue).getSongs().get(idx - 1);
                    timeListened = 0;
                    prevOutput.put("message",
                        "Returned to previous track successfully. The current track is "
                            + currentFile.getName() + ".");
                }
            }
        }
        lastCheck = command.getTimestamp();
        isPlaying = true;

        outputs.add(prevOutput);
    }

    /**
     * Go forward 90 seconds on the current podcast episode. If there aren't 90 seconds left in the
     * episode, skip to the next one.
     */
    public void forward(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode forwardOutput = mapper.createObjectNode();
        forwardOutput.put("command", "forward");
        forwardOutput.put("user", command.getUsername());
        forwardOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            forwardOutput.put("message", "Please load a source before attempting to forward.");
            outputs.add(forwardOutput);
            return;
        }
        if (queue.getType() != AudioEnum.PODCAST) {
            forwardOutput.put("message", "The loaded source is not a podcast.");
            outputs.add(forwardOutput);
            return;
        }
        int idx = ((Podcast) queue).getEpisodes().indexOf((Episode) currentFile);
        if (timeListened + FW_BW_TIME >= currentFile.getDuration()) {
            if (idx == ((Podcast) queue).getEpisodes().size() - 1) {
                forwardOutput.put("message", "There are no more episodes to play.");
                queue = null;
                isPlaying = false;
                currentFile = null;
                outputs.add(forwardOutput);
                return;
            }
            currentFile = ((Podcast) queue).getEpisodes().get(idx + 1);
            timeListened = 0;
            forwardOutput.put("message", "Skipped forward successfully.");
            outputs.add(forwardOutput);
            return;
        }
        timeListened += FW_BW_TIME;
        forwardOutput.put("message", "Skipped forward successfully.");
        outputs.add(forwardOutput);


    }

    /**
     * Rewind 90 seconds on the current podcast episode. If we haven't played 90 seconds of the
     * episode yet, restart it.
     */
    public void backward(final Command command, final User user, final ArrayNode outputs,
        final Library library) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode backwardOutput = mapper.createObjectNode();
        backwardOutput.put("command", "backward");
        backwardOutput.put("user", command.getUsername());
        backwardOutput.put("timestamp", command.getTimestamp());
        update(command.getTimestamp(), user, library);
        if (queue == null) {
            backwardOutput.put("message", "Please load a source attempting to backward.");
            outputs.add(backwardOutput);
            return;
        }
        if (queue.getType() != AudioEnum.PODCAST) {
            backwardOutput.put("message", "The loaded source is not a podcast.");
            outputs.add(backwardOutput);
            return;
        }
        if (timeListened - FW_BW_TIME < 0) {
            timeListened = 0;
        }
        timeListened -= FW_BW_TIME;
        backwardOutput.put("message", "Rewound successfully.");
        outputs.add(backwardOutput);
    }

    /**
     * Add a song to the song history. To be used in payout.
     */
    public void addSongHistory(final String name, final String artist) {
        for (SongHistory song : songHistory) {
            if (song.getName().equals(name) && song.getArtist().equals(artist)) {
                song.addListen();
                return;
            }
        }
        songHistory.add(new SongHistory(name, artist));
    }

    /**
     * Add a song to the premium song history. To be used in payout.
     */
    public void addPremiumSongHistory(final String name, final String artist) {
        for (SongHistory song : premiumSongHistory) {
            if (song.getName().equals(name) && song.getArtist().equals(artist)) {
                song.addListen();
                return;
            }
        }
        premiumSongHistory.add(new SongHistory(name, artist));
    }


    /**
     * Load the last generated recommendation in user's player. Works just like a normal load
     * (overwrites everything in the player)
     */
    public void loadRecommendations(final Command command, final User user,
        final ArrayNode outputs, final Library myLibrary) {
        update(command.getTimestamp(), user, myLibrary);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loadRecommendationsOutput = mapper.createObjectNode();
        loadRecommendationsOutput.put("command", "loadRecommendations");
        loadRecommendationsOutput.put("user", command.getUsername());
        loadRecommendationsOutput.put("timestamp", command.getTimestamp());
        if (user.getSongRecommendation() == null && user.getPlaylistRecommendation() == null) {
            loadRecommendationsOutput.put("message", "No recommendations available.");
            outputs.add(loadRecommendationsOutput);
            return;
        }
        if (user.getLastRecommendation().equals("song")) {
            loadRecommendationsOutput.put("message",
                "Playback loaded successfully.");
            outputs.add(loadRecommendationsOutput);
            queue = user.getSongRecommendation();
            currentFile = user.getSongRecommendation();
            timeListened = 0;
            isPlaying = true;
            adBreak = false;
            adPrice = 0;
            lastCheck = command.getTimestamp();
            addSongStat(user, myLibrary);
        } else {
            loadRecommendationsOutput.put("message",
                "Playlist recommendation successfully loaded.");
            outputs.add(loadRecommendationsOutput);
            queue = user.getPlaylistRecommendation();
            currentFile = user.getPlaylistRecommendation().getSongs().get(0);
            timeListened = 0;
            isPlaying = true;
            adBreak = false;
            adPrice = 0;
            lastCheck = command.getTimestamp();
            Song currentSong = (Song) currentFile;
            user.addArtistListen(currentSong.getArtist());
            user.addGenreListen(currentSong.getGenre());
            user.addAlbumListen(currentSong.getAlbum());
            user.addSongListen(currentSong.getName());
            Artist artist = myLibrary.findArtist(currentSong.getArtist());
            artist.addSongListen(currentSong.getName());
            artist.addAlbumListen(currentSong.getAlbum());
            artist.addListener(user.getUsername());
            if (!user.isPremium()) {
                addSongHistory(currentSong.getName(), currentSong.getArtist());
            } else {
                addPremiumSongHistory(currentSong.getName(), currentSong.getArtist());
            }
        }

    }

}
