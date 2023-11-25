package main;

import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import fileio.input.LibraryInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
//import java.util.Locale;


/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePathInput for input file
     * @param filePathOutput for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePathInput,
                              final String filePathOutput) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(LIBRARY_PATH), LibraryInput.class);

        ArrayNode outputs = objectMapper.createArrayNode();
        // TODO add your implementation
        File file = new File(CheckerConstants.TESTS_PATH + filePathInput);
        ArrayList<Command> commands = objectMapper.readValue(file, new TypeReference<>() { });
        Command.setAllPlaylists(new ArrayList<>());
        Command.repeat = "No Repeat";
        Command.shuffleFlag = false;
        Command.setLibrary(library);
        for (Command command: commands) {
            command.execute(outputs);
        }
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePathOutput), outputs);
    }
}

class Command {
    protected static LibraryInput library;
    private String command;
    private String username;
    private int timestamp;
    private int itemNumber;
    private String type;
    private HashMap<String, Object> filters;
    private String playlistName;
    private int playlistId;

    public int getSeed() {
        return seed;
    }

    protected enum LastType {
        SONG, PLAYLIST, PODCAST
    }
    protected static LastType lastType;
    protected static final ArrayList<SongInput> SEARCH_RES_SONG = new ArrayList<>();
    protected static final ArrayList<Playlist> SEARCHED_PLAYLIST = new ArrayList<>();
    protected static final ArrayList<PodcastInput> SEARCH_RES_PODCAST = new ArrayList<>();
    protected static SongInput selectedSong = new SongInput();
    protected static PodcastInput selectedPodcast = new PodcastInput();
    protected static Playlist selectedPlaylist = new Playlist();
    protected static SongInput loadedSong = new SongInput();
    protected static EpisodeInput loadedEpisode = new EpisodeInput();
    protected static PodcastInput loadedPodcast = new PodcastInput();
    protected static Playlist loadedPlaylist = new Playlist();
    protected static Playlist loadedPlaylistShuffle = new Playlist();
    protected static boolean searchNull = false;
    protected static int originalDuration = 0;
    protected static boolean paused = true;
    protected static boolean shuffle = false;
    protected static int currentTimestamp = 0;
    protected static String repeat = "No Repeat";
    protected static boolean playlist = false;
    protected static boolean finished = false;
    protected static String lastCommand = "";
    protected static boolean shuffleFlag = false;
    protected static ArrayList<Playlist> allPlaylists = new ArrayList<>();

    public static void setAllPlaylists(ArrayList<Playlist> allPlaylists) {
        Command.allPlaylists = allPlaylists;
    }

    private final int seed = 0;
    protected static final ArrayList<USER_LIKED> USER_LIKED = new ArrayList<>();

    public int seed() {
        return seed;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, Object> filters) {
        this.filters = filters;
    }

    public static void setLibrary(LibraryInput library) {
        Command.library = library;
    }
    public void execute(ArrayNode outputs) {
        if (!paused) {
            if (shuffleFlag) {
                while (!loadedPlaylistShuffle.getSongs().isEmpty()) {
                    loadedPlaylistShuffle.getSongs().remove(0);
                }
                loadedPlaylistShuffle.getSongs().addAll(loadedPlaylist.getSongs());
                Collections.shuffle(loadedPlaylistShuffle.getSongs(), new Random(seed));
            }
            if (lastType == LastType.SONG && loadedSong == null) {
                finished = true;
                paused = true;
            }
            if (lastType == LastType.SONG && loadedSong != null) {
                loadedSong.setDuration(loadedSong.getDuration() - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0 && !playlist && repeat.equals("No Repeat")) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    loadedSong.setDuration(loadedSong.getDuration() + duration);
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        == loadedPlaylist.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && !playlist
                        && repeat.equals("Repeat Once")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration + duration);
                    repeat = "No Repeat";
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1 && repeat.equals("Repeat once")) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    loadedSong.setDuration(loadedSong.getDuration() + duration);
                } else if (loadedSong.getDuration() <= 0 && !playlist
                        && repeat.equals("Repeat Infinite")) {
                    int duration = loadedSong.getDuration();
                    while (duration + originalDuration < 0) {
                        duration += originalDuration;
                    }
                    loadedSong.setDuration(originalDuration + duration);
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1 && repeat.equals("Repeat infinite")) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    loadedSong.setDuration(loadedSong.getDuration() + duration);
                }
            } else if (lastType == LastType.PODCAST && loadedEpisode != null
                    && loadedEpisode.getDuration() != null) {
                loadedEpisode.setDuration(loadedEpisode.getDuration()
                        - (timestamp - currentTimestamp));
                if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        == loadedPodcast.getEpisodes().size() - 1
                        && !playlist && repeat.equals("No Repeat")) {
                    finished = true;
                    paused = true;
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        != loadedPodcast.getEpisodes().size() - 1 && repeat.equals("No Repeat")) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(loadedPodcast.getEpisodes()
                                    .indexOf(loadedEpisode) + 1);
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        == loadedPodcast.getEpisodes().size() - 1
                        && !playlist && repeat.equals("Repeat once")) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(0);
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        != loadedPodcast.getEpisodes().size() - 1
                        && !playlist && repeat.equals("Repeat once")) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(loadedPodcast.getEpisodes()
                                    .indexOf(loadedEpisode) + 1);
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        == loadedPodcast.getEpisodes().size() - 1
                        && !playlist && repeat.equals("Repeat infinite")) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(loadedPodcast.getEpisodes()
                                    .indexOf(loadedEpisode));
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        != loadedPodcast.getEpisodes().size() - 1
                        && !playlist && repeat.equals("Repeat infinite")) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(loadedPodcast.getEpisodes()
                                    .indexOf(loadedEpisode) + 1);
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                }
            } else if (lastType == LastType.PLAYLIST && loadedSong != null && !shuffleFlag) {
                if (repeat.equals("Repeat Once") || repeat.equals("Repeat Infinite")) {
                    repeat = "No Repeat";
                }
                loadedSong.setDuration(loadedSong.getDuration()
                        - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0
                        && repeat.equals("No Repeat") && loadedPlaylist.getSongs().
                        indexOf(loadedSong) == loadedPlaylist.getSongs().size() - 1) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    if (loadedSong.getDuration() + duration > 0) {
                        loadedSong.setDuration(loadedSong.getDuration() + duration);
                    } else {
                        while (duration < 0) {
                            duration += loadedSong.getDuration();
                            if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                    < loadedPlaylist.getSongs().size() - 1) {
                                loadedSong =
                                        loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                                .indexOf(loadedSong) + 1);
                            } else if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                    == loadedPlaylist.getSongs().size() - 1) {
                                finished = true;
                                paused = true;
                            }
                        }
                        if (duration + loadedSong.getDuration() > 0) {
                            loadedSong.setDuration(loadedSong.getDuration() + duration);
                        } else {
                            finished = true;
                            paused = true;
                        }
                    }
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        == loadedPlaylist.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        == loadedPlaylist.getSongs().size() - 1 && repeat.equals("Repeat All")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    loadedSong =
                            loadedPlaylist.getSongs().get(0);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                        if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                < loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                            .indexOf(loadedSong) + 1);
                        } else if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                == loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(0);
                        }
                    }
                    loadedSong.setDuration(originalDuration + duration);
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1 && repeat.equals("Repeat All")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                        if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                < loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                            .indexOf(loadedSong) + 1);
                        } else if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                == loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(0);
                        }
                    }
                    loadedSong.setDuration(originalDuration - (originalDuration - duration));
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && repeat.equals("Repeat Current Song")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                    }
                    loadedSong.setDuration(originalDuration - (originalDuration - duration));
                }
            } else if (lastType == LastType.PLAYLIST && loadedSong == null && !shuffleFlag) {
                finished = true;
                paused = true;
                repeat = "No Repeat";
            } else if (lastType == LastType.PLAYLIST && loadedSong != null && shuffleFlag) {
                if (repeat.equals("Repeat Once") || repeat.equals("Repeat Infinite")) {
                    repeat = "No Repeat";
                }
                loadedSong.setDuration(loadedSong.getDuration()
                        - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0
                        && repeat.equals("No Repeat") && loadedPlaylistShuffle.getSongs().
                        indexOf(loadedSong) == loadedPlaylistShuffle.getSongs().size() - 1) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                        < loadedPlaylistShuffle.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylistShuffle.getSongs().get(loadedPlaylistShuffle.getSongs()
                                    .indexOf(loadedSong) + 1);
                    if (loadedSong.getDuration() + duration > 0) {
                        loadedSong.setDuration(loadedSong.getDuration() + duration);
                    } else {
                        while (duration < 0) {
                            duration += loadedSong.getDuration();
                            if (duration < 0 && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                                    < loadedPlaylistShuffle.getSongs().size() - 1) {
                                loadedSong =
                                        loadedPlaylistShuffle.getSongs().get(loadedPlaylistShuffle.getSongs()
                                                .indexOf(loadedSong) + 1);
                            } else if (duration < 0 && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                                    == loadedPlaylistShuffle.getSongs().size() - 1) {
                                finished = true;
                                paused = true;
                            }
                        }
                        if (duration + loadedSong.getDuration() > 0) {
                            loadedSong.setDuration(loadedSong.getDuration() + duration);
                        } else {
                            finished = true;
                            paused = true;
                        }
                    }
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                        == loadedPlaylistShuffle.getSongs().size() - 1 && repeat.equals("No Repeat")) {
                    finished = true;
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                        == loadedPlaylistShuffle.getSongs().size() - 1 && repeat.equals("Repeat All")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    loadedSong =
                            loadedPlaylist.getSongs().get(0);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                        if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                < loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                            .indexOf(loadedSong) + 1);
                        } else if (duration < 0 && loadedPlaylist.getSongs().indexOf(loadedSong)
                                == loadedPlaylist.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylist.getSongs().get(0);
                        }
                    }
                    loadedSong.setDuration(originalDuration + duration);
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                        < loadedPlaylistShuffle.getSongs().size() - 1 && repeat.equals("Repeat All")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    loadedSong =
                            loadedPlaylistShuffle.getSongs().get(loadedPlaylistShuffle.getSongs()
                                    .indexOf(loadedSong) + 1);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                        if (duration < 0 && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                                < loadedPlaylistShuffle.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylistShuffle.getSongs().get(loadedPlaylistShuffle.getSongs()
                                            .indexOf(loadedSong) + 1);
                        } else if (duration < 0 && loadedPlaylistShuffle.getSongs().indexOf(loadedSong)
                                == loadedPlaylistShuffle.getSongs().size() - 1) {
                            loadedSong =
                                    loadedPlaylistShuffle.getSongs().get(0);
                        }
                    }
                    loadedSong.setDuration(originalDuration - (originalDuration - duration));
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && repeat.equals("Repeat Current Song")) {
                    int duration = loadedSong.getDuration();
                    loadedSong.setDuration(originalDuration);
                    while (duration < 0) {
                        duration += loadedSong.getDuration();
                    }
                    loadedSong.setDuration(originalDuration - (originalDuration - duration));
                }
            } else if (lastType == LastType.PLAYLIST && loadedSong == null && shuffleFlag) {
                finished = true;
                paused = true;
                repeat = "No Repeat";
            }
        }
        currentTimestamp = timestamp;
        switch (command) {
            case "search" -> {
                Search search = new Search(username, timestamp, type, filters);
                search.executeSearch(outputs);
            }
            case "select" -> {
                Select select = new Select(username, timestamp, itemNumber);
                select.execute(outputs);
            }
            case "load" -> {
                Load load = new Load(username, timestamp);
                load.execute(outputs);
            }
            case "status" -> {
                Status status = new Status(username, timestamp);
                status.execute(outputs);
            }
            case "playPause" -> {
                PlayPause playPause = new PlayPause(username, timestamp);
                playPause.execute(outputs);
            }
            case "createPlaylist" -> {
                int userPlaylists = 1;
                for (Playlist playlist : allPlaylists) {
                    if (playlist.getOwner().equals(username)) {
                        userPlaylists++;
                    }
                }
                CreatePlaylist createPlaylist = new CreatePlaylist(username, timestamp, playlistName, userPlaylists);
                createPlaylist.execute(outputs);
            }
            case "addRemoveInPlaylist" -> {
                if (allPlaylists.isEmpty()) {
                    ObjectNode playlistObj = new ObjectMapper().createObjectNode();
                    playlistObj.put("command", "addRemoveInPlaylist");
                    playlistObj.put("user", username);
                    playlistObj.put("timestamp", timestamp);
                    playlistObj.put("message", "The specified playlist does not exist.");
                    outputs.add(playlistObj);
                } else {
                    for (Playlist playlist : allPlaylists) {
                        if (playlist.getOwner().equals(username) && playlist.getId() == playlistId) {
                            AddRemoveInPlaylist addRemoveInPlaylist = new AddRemoveInPlaylist(username, timestamp, playlistName, playlistId);
                            addRemoveInPlaylist.execute(outputs);
                        }
                    }
                }
            }
            case "like" -> {
                USER_LIKED userLikeIt = null;
                for (USER_LIKED user : USER_LIKED) {
                    if (user.getUsername().equals(username)) {
                        userLikeIt = user;
                        break;
                    }
                }
                if (userLikeIt == null) {
                    USER_LIKED.add(new USER_LIKED(username));
                }
                Like like = new Like(username, loadedSong, timestamp);
                like.execute(outputs);
            }
            case "showPlaylists" -> {
                ShowPlaylist showPlaylist = new ShowPlaylist(username, timestamp);
                showPlaylist.execute(outputs);
            }
            case "showPreferredSongs" -> {
                ShowPrefferedSongs showPrefferedSongs = new ShowPrefferedSongs(username, timestamp);
                showPrefferedSongs.execute(outputs);
            }
            case "repeat" -> {
                Repeat repeat = new Repeat(username, timestamp);
                repeat.execute(outputs);
            }
            case "shuffle" -> {
                Shuffle shuffle = new Shuffle(username, timestamp);
                shuffle.execute(outputs);
            }
            case "follow" -> {
                Follow follow = new Follow(username, timestamp, selectedPlaylist);
                follow.execute(outputs);
            }
            case "switchVisibility" -> {
                SwitchVisibility switchVisibility = new SwitchVisibility(username, timestamp, playlistName, playlistId);
                switchVisibility.execute(outputs);
            }
        }
        currentTimestamp = timestamp;
        lastCommand = command;
    }
}

class Search extends Command {
    private final String username;
    private final int timestamp;
    private final String type;//
    private final HashMap<String, Object> filters;

    public Search(String username, int timestamp, String type, HashMap<String, Object> filters) {
        this.username = username;
        this.timestamp = timestamp;
        this.type = type;
        this.filters = filters;
    }
    public void executeSearch(ArrayNode outputs) {
        ObjectNode searchObj = new ObjectMapper().createObjectNode();
        searchObj.put("command", "search");
        searchObj.put("user", username);
        searchObj.put("timestamp", timestamp);
        if (loadedSong != null) {
            loadedSong.setDuration(originalDuration);
        }
        loadedSong = null;
        loadedEpisode = null;
        searchNull = true;
        switch (type) {
            case "song" -> {
                ArrayList<SongInput> songs = new ArrayList<>(library.getSongs());
                if (filters.isEmpty()) {
                    outputs.add("No filter");
                } else {
                    if (filters.containsKey("name")) {
                        songs.removeIf(song -> !song.getName().startsWith((String) filters.get("name")));
                    }
                    if (filters.containsKey("album")) {
                        songs.removeIf(song -> !song.getAlbum().equals(filters.get("album")));
                    }
                    if (filters.containsKey("genre")) {
                        songs.removeIf(song -> !song.getGenre().equalsIgnoreCase((String) filters.get("genre")));
                    }
                    if (filters.containsKey("tags")) {
                        ArrayList<String> tags = (ArrayList<String>) filters.get("tags");
                        songs.removeIf(song -> !song.getTags().containsAll(tags));
                    }
                    if (filters.containsKey("lyrics")) {
                        String lyrics = ((String) filters.get("lyrics")).toLowerCase();
                        songs.removeIf(song -> !song.getLyrics().toLowerCase().contains(lyrics));
                    }
                    if (filters.containsKey("releaseYear")) {
                        char operator = ((String) filters.get("releaseYear")).charAt(0);
                        int year = Integer.parseInt(((String) filters.get("releaseYear")).substring(1));
                        switch (operator) {
                            case '=' -> songs.removeIf(song -> song.getReleaseYear() != year);
                            case '<' -> songs.removeIf(song -> song.getReleaseYear() >= year);
                            case '>' -> songs.removeIf(song -> song.getReleaseYear() <= year);
                        }
                    }
                    if (filters.containsKey("artist")) {
                        songs.removeIf(song -> !song.getArtist().equals(filters.get("artist")));
                    }
                    if (songs.isEmpty()) {
                        searchObj.put("message", "no results found");
                    } else {
                        if (songs.size() > 5) {
                            songs = new ArrayList<>(songs.subList(0, 5));
                        }
                        searchObj.put("message", "Search returned " + songs.size() + " results");
                        ArrayNode songArray = searchObj.putArray("results");
                        for (SongInput song : songs) {
                            songArray.add(song.getName());
                        }
                        outputs.add(searchObj);
                    }
                    SEARCH_RES_SONG.clear();
                    SEARCH_RES_SONG.addAll(songs);
                    Command.lastType = LastType.SONG;
                }
            }
            case "playlist" -> {
                ArrayList<Playlist> playlists = new ArrayList<>(allPlaylists);
                if (filters.containsKey("name")) {
                    playlists.removeIf(playlist -> !playlist.getName().startsWith((String) filters.get("name")));
                }
                if (filters.containsKey("owner")) {
                    playlists.removeIf(playlist -> !playlist.getOwner().equals(filters.get("owner")));
                }
                playlists.removeIf(playlist -> !playlist.getOwner().equals(username) && playlist.getVisibility() == Playlist.Visibility.PRIVATE);
                if (playlists.isEmpty()) {
                    searchObj.put("message", "Search returned 0 results");
                    ArrayNode playlistArray = searchObj.putArray("results");
                    outputs.add(searchObj);
                } else {
                    if (playlists.size() > 5) {
                        playlists = new ArrayList<>(playlists.subList(0, 5));
                    }
                    searchObj.put("message", "Search returned " + playlists.size() + " results");
                    ArrayNode playlistArray = searchObj.putArray("results");
                    for (Playlist playlist : playlists) {
                        playlistArray.add(playlist.getName());
                    }
                    outputs.add(searchObj);
                }
                SEARCHED_PLAYLIST.clear();
                SEARCHED_PLAYLIST.addAll(playlists);
                Command.lastType = LastType.PLAYLIST;
            }
            case "podcast" -> {
                ArrayList<PodcastInput> podcasts = new ArrayList<>(library.getPodcasts());
                if (filters.containsKey("name")) {
                    podcasts.removeIf(song -> !song.getName().startsWith((String) filters.get("name")));
                }
                if (filters.containsKey("owner")) {
                    podcasts.removeIf(song -> !song.getOwner().equals(filters.get("owner")));
                }
                if (podcasts.isEmpty()) {
                    searchObj.put("message", "no results found");
                } else {
                    if (podcasts.size() > 5) {
                        podcasts = new ArrayList<>(podcasts.subList(0, 5));
                    }
                    searchObj.put("message", "Search returned " + podcasts.size() + " results");
                    ArrayNode podcastArray = searchObj.putArray("results");
                    for (PodcastInput podcast : podcasts) {
                        podcastArray.add(podcast.getName());
                    }
                    outputs.add(searchObj);
                }
                SEARCH_RES_PODCAST.clear();
                SEARCH_RES_PODCAST.addAll(podcasts);
                Command.lastType = LastType.PODCAST;
            }
        }
    }
}

class Select extends Command {
    private final String username;
    private final int timestamp;
    private final int itemNumber;
    public Select(String username, int timestamp, int itemNumber) {
        this.username = username;
        this.timestamp = timestamp;
        this.itemNumber = itemNumber;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode selectObj = new ObjectMapper().createObjectNode();
        selectObj.put("command", "select");
        selectObj.put("user", username);
        selectObj.put("timestamp", timestamp);
        if (lastType == LastType.SONG) {
            if (itemNumber - 1 >= SEARCH_RES_SONG.size() && !SEARCH_RES_SONG.isEmpty()) {
                selectObj.put("message", "The selected ID is too high.");
                selectedSong = null;
            } else if (SEARCH_RES_SONG.isEmpty()) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                if (loadedSong != null) {
                    loadedSong.setDuration(originalDuration);
                    //outputs.add("name: " + loadedSong.getName() + " original duration: " + loadedSong.getDuration());
                }
                selectObj.put("message", "Successfully selected " + SEARCH_RES_SONG.get(itemNumber - 1).getName() + ".");
                selectedSong = SEARCH_RES_SONG.get(itemNumber - 1);
                originalDuration = selectedSong.getDuration();
                SEARCH_RES_SONG.clear();
            }
            outputs.add(selectObj);
        } else if (lastType == LastType.PODCAST) {
            if (itemNumber - 1 >= SEARCH_RES_PODCAST.size() && !SEARCH_RES_PODCAST.isEmpty()) {
                selectObj.put("message", "The selected ID is too high.");
                selectedPodcast = null;
            } else if (selectedPodcast == null) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                if (loadedEpisode != null) {
                    loadedEpisode.setDuration(originalDuration);
                }
                selectObj.put("message", "Successfully selected " + SEARCH_RES_PODCAST.get(itemNumber - 1).getName() + ".");
                selectedPodcast = SEARCH_RES_PODCAST.get(itemNumber - 1);
                originalDuration = selectedPodcast.getEpisodes().get(0).getDuration();
                SEARCH_RES_PODCAST.clear();
            }
            outputs.add(selectObj);
        } else if (lastType == LastType.PLAYLIST) {
            if (itemNumber - 1 >= SEARCHED_PLAYLIST.size()) {
                selectObj.put("message", "The selected ID is too high.");
                selectedPlaylist = null;
            } else if (SEARCHED_PLAYLIST.isEmpty()) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                if (loadedSong != null) {
                    loadedSong.setDuration(originalDuration);
                    //outputs.add("name: " + loadedSong.getName() + " original duration: " + loadedSong.getDuration());
                }
                selectObj.put("message", "Successfully selected " + SEARCHED_PLAYLIST.get(itemNumber - 1).getName() + ".");
                selectedPlaylist = SEARCHED_PLAYLIST.get(itemNumber - 1);
                originalDuration = selectedPlaylist.getSongs().get(0).getDuration();
                SEARCHED_PLAYLIST.clear();
            }
            outputs.add(selectObj);
        } else {
            selectObj.put("message", "No results found");
        }
    }
}

class Load extends Command {
    private final String username;
    private final int timestamp;
    public Load(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode loadObj = new ObjectMapper().createObjectNode();
        loadObj.put("command", "load");
        loadObj.put("user", username);
        loadObj.put("timestamp", timestamp);
        if (!lastCommand.equals("select")) {
            loadObj.put("message", "Please select a source before attempting to load.");
            outputs.add(loadObj);
            return;
        }
        if (lastType == LastType.SONG) {
            if (selectedSong == null) {
                loadObj.put("message", "Please select a source before attempting to load.");
            } else {
//                if (loadedSong != null) {
//                    loadedSong.setDuration(originalDuration);
//                }
                //originalDuration = selectedSong.getDuration();
                loadObj.put("message", "Playback loaded successfully.");
                loadedSong = selectedSong;
                loadedSong.setDuration(originalDuration);
                selectedSong = null;
                //originalDuration = selectedSong.getDuration();
                paused = false;
                finished = false;
                //currentTimestamp = timestamp;
                playlist = false;
            }
        } else if (lastType == LastType.PODCAST) {
            if (selectedPodcast == null) {
                loadObj.put("message", "Please select a source before attempting to load.");
            } else {
                //originalDuration = selectedPodcast.getEpisodes().get(0).getDuration();
//                if (loadedEpisode != null) {
//                    loadedEpisode.setDuration(originalDuration);
//                }
                loadObj.put("message", "Playback loaded successfully.");
                loadedPodcast = selectedPodcast;
                loadedEpisode = loadedPodcast.getEpisodes().get(0);
                loadedEpisode.setDuration(originalDuration);
                //originalDuration = selectedPodcast.getEpisodes().get(0).getDuration();
                paused = false;
                finished = false;
                //currentTimestamp = timestamp;
                playlist = false;
            }
        } else if (lastType == LastType.PLAYLIST) {
            if (selectedPlaylist == null) {
                loadObj.put("message", "Please select a source before attempting to load.");
            } else if (!selectedPlaylist.getSongs().isEmpty()) {
//                if (loadedSong != null) {
//                    loadedSong.setDuration(originalDuration);
//                }
                //outputs.add("name: " + selectedPlaylist.getSongs().get(0).getName() + " original duration: " + selectedPlaylist.getSongs().get(0).getDuration());
                //originalDuration = selectedPlaylist.getSongs().get(0).getDuration();
                loadObj.put("message", "Playback loaded successfully.");
                loadedSong = selectedPlaylist.getSongs().get(0);
                //System.out.println("load: " + loadedSong.getName() + "  " + loadedSong.getDuration());
                loadedSong.setDuration(originalDuration);
                //originalDuration = selectedPlaylist.getSongs().get(0).getDuration();
                paused = false;
                finished = false;
                //currentTimestamp = timestamp;
                playlist = true;
                loadedPlaylist = selectedPlaylist;
            } else {
                loadObj.put("message", "Playlist is empty.");
            }
        } else {
            loadObj.put("message", "No results found");
        }
        outputs.add(loadObj);
    }
}

class Status extends Command {
    private final String username;
    private final int timestamp;
    public Status(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode statusObj = new ObjectMapper().createObjectNode();
        statusObj.put("command", "status");
        statusObj.put("user", username);
        statusObj.put("timestamp", timestamp);
        if (lastType == LastType.SONG) {
            if (loadedSong == null && !paused && !searchNull) {
                statusObj.put("message", "No song is currently playing.");
            } else if (loadedSong == null || finished) {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", "");
                stats.put("remainedTime", 0);
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffle);
                stats.put("paused", paused);
            } else {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", loadedSong.getName());
                stats.put("remainedTime", loadedSong.getDuration());
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffle);
                stats.put("paused", paused);
            }
        } else if (lastType == LastType.PODCAST) {
            if (loadedPodcast == null) {
                statusObj.put("message", "No podcast is currently playing.");
            } else {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", loadedEpisode.getName());
                stats.put("remainedTime", loadedEpisode.getDuration());
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffle);
                stats.put("paused", paused);
            }
        } else if (lastType == LastType.PLAYLIST) {
            if (loadedSong == null && !paused && !searchNull) {
                statusObj.put("message", "No song is currently playing.");
            } else if (loadedSong == null || finished) {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", "");
                stats.put("remainedTime", 0);
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffleFlag);
                stats.put("paused", paused);
            } else {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", loadedSong.getName());
                stats.put("remainedTime", loadedSong.getDuration());
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffleFlag);
                stats.put("paused", paused);
            }
        } else {
            statusObj.put("message", "No results found");
        }
        outputs.add(statusObj);
    }
}

class PlayPause extends Command {
    private final String username;
    private final int timestamp;
    public PlayPause(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode playPauseObj = new ObjectMapper().createObjectNode();
        playPauseObj.put("command", "playPause");
        playPauseObj.put("user", username);
        playPauseObj.put("timestamp", timestamp);
        if (lastType == LastType.SONG) {
            if (loadedSong == null) {
                playPauseObj.put("message", "Please load a source before attempting to pause or resume playback.");
            } else {
                if (paused) {
                    playPauseObj.put("message", "Playback resumed successfully.");
                    paused = false;
                } else {
                    playPauseObj.put("message", "Playback paused successfully.");
                    paused = true;
                }
            }
        } else if (lastType == LastType.PODCAST) {
            if (loadedPodcast == null) {
                playPauseObj.put("message", "Please load a source before attempting to pause or resume playback.");
            } else {
                if (paused) {
                    playPauseObj.put("message", "Playback resumed successfully.");
                    paused = false;
                } else {
                    playPauseObj.put("message", "Playback paused successfully.");
                    paused = true;
                }
            }
        } else {
            playPauseObj.put("message", "No results found");
        }
        outputs.add(playPauseObj);
    }
}

class Playlist {
    private String name;
    private int id;
    private String owner;
    private ArrayList<SongInput> songs;
    private int userId;
    private int followers;
    private ArrayList<String> followersList;

    public ArrayList<String> getFollowersList() {
        return followersList;
    }
    public void addFollower(String follower) {
        followersList.add(follower);
        followers++;
    }
    public void removeFollower(String follower) {
        followersList.remove(follower);
        followers--;
    }
    public void setFollowersList(ArrayList<String> followersList) {
        this.followersList = followersList;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public Playlist() {
        songs = new ArrayList<>();
        followersList = new ArrayList<>();
    }

    public enum Visibility {
        PUBLIC, PRIVATE
    }
    private Visibility visibility;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<SongInput> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<SongInput> songs) {
        this.songs = songs;
    }
    public void addSong(SongInput song) {
        songs.add(song);
    }
    public void removeSong(SongInput song) {
        songs.remove(song);
    }
}

class AddRemoveInPlaylist extends Command {
    private final String username;
    private final int timestamp;
    private final String playlistName;
    private int playlistId;
    public AddRemoveInPlaylist(String username, int timestamp, String playlistName, int playlistId) {
        this.username = username;
        this.timestamp = timestamp;
        this.playlistName = playlistName;
        this.playlistId = playlistId;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode playlistObj = new ObjectMapper().createObjectNode();
        playlistObj.put("command", "addRemoveInPlaylist");
        playlistObj.put("user", username);
        playlistObj.put("timestamp", timestamp);
        for (Playlist playlist : allPlaylists) {
            if (playlist.getOwner().equals(username) && playlist.getId() == (playlistId)) {
                if (Command.lastType == LastType.SONG) {
                    if (loadedSong == null) {
                        playlistObj.put("message", "Please load a source before adding to or removing from the playlist.");
                    } else {
                        if (playlist.getSongs().contains(loadedSong)) {
                            loadedSong.setDuration(originalDuration);
                            playlist.removeSong(loadedSong);
                            playlistObj.put("message", "Successfully removed from playlist.");
                        } else {
                            //loadedSong.setDuration(originalDuration);
                            playlist.addSong(loadedSong);
                            playlistObj.put("message", "Successfully added to playlist.");
                        }
                    }
                } else {
                    playlistObj.put("message", "The loaded source is not a song.");
                }
            }
        }
        outputs.add(playlistObj);
    }
}

class CreatePlaylist extends Command {
    private final String username;
    private final int timestamp;
    private final String playlistName;
    private int playlistId;
    public CreatePlaylist(String username, int timestamp, String playlistName, int playlistId) {
        this.username = username;
        this.timestamp = timestamp;
        this.playlistName = playlistName;
        this.playlistId = playlistId;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode playlistObj = new ObjectMapper().createObjectNode();
        playlistObj.put("command", "createPlaylist");
        playlistObj.put("user", username);
        playlistObj.put("timestamp", timestamp);
        if (playlistName == null || playlistName.isEmpty()) {
            playlistObj.put("message", "Please specify a playlist name.");
        } else {
            for (Playlist playlist : allPlaylists) {
                if (playlist.getOwner().equals(username) && playlist.getName().equals(playlistName)) {
                    playlistObj.put("message", "A playlist with the same name already exists.");
                    outputs.add(playlistObj);
                    return;
                }
            }
            playlistObj.put("message", "Playlist created successfully.");
            Playlist playlist = new Playlist();
            playlist.setName(playlistName);
            playlist.setId(playlistId);
            playlist.setOwner(username);
            playlist.setVisibility(Playlist.Visibility.PUBLIC);
            playlist.setSongs(new ArrayList<>());
            playlist.setFollowers(0);
            allPlaylists.add(playlist);
        }
        outputs.add(playlistObj);
    }

}
class USER_LIKED {
    private String username;
    private ArrayList<SongInput> songs;
    private ArrayList<PodcastInput> podcasts;
    public USER_LIKED() {
        songs = new ArrayList<>();
        podcasts = new ArrayList<>();
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public USER_LIKED(String username) {
        this.username = username;
        this.songs = new ArrayList<>();
        this.podcasts = new ArrayList<>();
    }
    public void addSong(SongInput song) {
        songs.add(song);
    }
    public void addPodcast(PodcastInput podcast) {
        podcasts.add(podcast);
    }
    public void removeSong(SongInput song) {
        songs.remove(song);
    }
    public void removePodcast(PodcastInput podcast) {
        podcasts.remove(podcast);
    }
    public ArrayList<SongInput> getSongs() {
        return songs;
    }
    public ArrayList<PodcastInput> getPodcasts() {
        return podcasts;
    }
}

class Like extends Command {
    private final String username;
    private final SongInput songToLike;
    private final int timestamp;
    public Like(String username, SongInput songToLike, int timestamp) {
        this.username = username;
        this.songToLike = songToLike;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode likeObj = new ObjectMapper().createObjectNode();
        likeObj.put("command", "like");
        likeObj.put("user", username);
        likeObj.put("timestamp", timestamp);
        if (songToLike == null) {
            likeObj.put("message", "Please load a source before liking or unliking.");
        } else if (lastType == LastType.SONG || loadedSong != null) {
            USER_LIKED userLikeIt = null;
            for (USER_LIKED user : USER_LIKED) {
                if (user.getUsername().equals(username)) {
                    userLikeIt = user;
                    break;
                }
            }
            if (userLikeIt == null) {
                USER_LIKED.add(new USER_LIKED(username));
            }
            if (userLikeIt.getSongs().contains(songToLike)) {
                likeObj.put("message", "Unlike registered successfully.");
                userLikeIt.removeSong(songToLike);
            } else {
                userLikeIt.addSong(songToLike);
                likeObj.put("message", "Like registered successfully.");
            }
        } else {
            likeObj.put("message", "Loaded source is not a song.");
        }
        outputs.add(likeObj);
    }
}

class ShowPlaylist extends Command {
    private final String username;
    private final int timestamp;
    public ShowPlaylist(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode showPlaylistObj = new ObjectMapper().createObjectNode();
        showPlaylistObj.put("command", "showPlaylists");
        showPlaylistObj.put("user", username);
        showPlaylistObj.put("timestamp", timestamp);
        ArrayNode playlistArray = showPlaylistObj.putArray("result");
        for (Playlist playlist : allPlaylists) {
            if (playlist.getOwner().equals(username) && playlist.getSongs() != null) {
                ObjectNode playlistObj = playlistArray.addObject();
                playlistObj.put("name", playlist.getName());
                ArrayNode songsArray = playlistObj.putArray("songs");
                for (SongInput song : playlist.getSongs()) {
                    songsArray.add(song.getName());
                }
                playlistObj.put("visibility", playlist.getVisibility().toString().toLowerCase());
                playlistObj.put("followers", playlist.getFollowers());
            }
        }
        outputs.add(showPlaylistObj);
    }
}

class ShowPrefferedSongs extends Command {
    private final String username;
    private final int timestamp;
    public ShowPrefferedSongs(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode showPrefferedSongsObj = new ObjectMapper().createObjectNode();
        showPrefferedSongsObj.put("command", "showPreferredSongs");
        showPrefferedSongsObj.put("user", username);
        showPrefferedSongsObj.put("timestamp", timestamp);
        ArrayNode songArray = showPrefferedSongsObj.putArray("result");
        for (USER_LIKED user : USER_LIKED) {
            if (user.getUsername().equals(username)) {
                for (SongInput song : user.getSongs()) {
                    songArray.add(song.getName());
                }
            }
        }
        outputs.add(showPrefferedSongsObj);
    }
}

class Repeat extends Command {
    private final String username;
    private final int timestamp;
    public Repeat(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode repeatObj = new ObjectMapper().createObjectNode();
        repeatObj.put("command", "repeat");
        repeatObj.put("user", username);
        repeatObj.put("timestamp", timestamp);
        if (loadedEpisode != null || loadedSong != null) {
            if (Command.lastType == LastType.SONG || Command.lastType == LastType.PODCAST) {
                if (Command.repeat.equals("No Repeat")) {
                    Command.repeat = "Repeat Once";
                    repeatObj.put("message", "Repeat mode changed to repeat once.");
                } else if (Command.repeat.equals("Repeat Once")) {
                    Command.repeat = "Repeat Infinite";
                    repeatObj.put("message", "Repeat mode changed to repeat infinite.");
                } else if (Command.repeat.equals("Repeat Infinite")) {
                    Command.repeat = "No Repeat";
                    repeatObj.put("message", "Repeat mode changed to no repeat.");
                } else if (Command.repeat.equals("Repeat All")) {
                    Command.repeat = "Repeat Infinite";
                    repeatObj.put("message", "Repeat mode changed to repeat infinite.");
                } else if (Command.repeat.equals("Repeat Current Song")) {
                    Command.repeat = "No Repeat";
                    repeatObj.put("message", "Repeat mode changed to no repeat.");
                }
            } else {
                if (Command.repeat.equals("No Repeat")) {
                    Command.repeat = "Repeat All";
                    repeatObj.put("message", "Repeat mode changed to repeat all.");
                } else if (Command.repeat.equals("Repeat All")) {
                    Command.repeat = "Repeat Current Song";
                    repeatObj.put("message", "Repeat mode changed to repeat current song.");
                } else if (Command.repeat.equals("Repeat Current Song")) {
                    Command.repeat = "No Repeat";
                    repeatObj.put("message", "Repeat mode changed to no repeat.");
                } else if (Command.repeat.equals("Repeat Once")) {
                    Command.repeat = "Repeat Current Song";
                    repeatObj.put("message", "Repeat mode changed to repeat current song.");
                } else if (Command.repeat.equals("Repeat Infinite")) {
                    Command.repeat = "No Repeat";
                    repeatObj.put("message", "Repeat mode changed to no repeat.");
                }
            }
        } else {
            repeatObj.put("message", "Please load a source before setting the repeat status.");
        }
        outputs.add(repeatObj);
    }
}

class Shuffle extends Command {
    private final String username;
    private final int timestamp;
    public Shuffle(String username, int timestamp) {
        this.username = username;
        this.timestamp = timestamp;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode shuffleObj = new ObjectMapper().createObjectNode();
        shuffleObj.put("command", "shuffle");
        shuffleObj.put("user", username);
        shuffleObj.put("timestamp", timestamp);
        if (lastType == LastType.PLAYLIST) {
            if (loadedPlaylist == null) {
                shuffleObj.put("message", "Please load a source before using the shuffle function.");
            } else {
                if (shuffleFlag) {
                    shuffleFlag = false;
                    shuffleObj.put("message", "Shuffle function deactivated successfully.");
                } else {
                    shuffleFlag = true;
                    shuffleObj.put("message", "Shuffle function activated successfully.");
                }
            }
        } else {
            shuffleObj.put("message", "The loaded source is not a playlist.");
        }
        outputs.add(shuffleObj);
    }
}

class Follow extends Command {
    private final String username;
    private final int timestamp;
    private final Playlist selectedPlaylist;
    private int playlistId;
    public Follow(String username, int timestamp, Playlist playlistName) {
        this.username = username;
        this.timestamp = timestamp;
        this.selectedPlaylist = playlistName;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode followObj = new ObjectMapper().createObjectNode();
        followObj.put("command", "follow");
        followObj.put("user", username);
        followObj.put("timestamp", timestamp);
        if (lastType != LastType.PLAYLIST) {
            followObj.put("message", "The selected source is not a playlist.");
            outputs.add(followObj);
            return;
        }
        if (selectedPlaylist == null || !lastCommand.equals("select")) {
            followObj.put("message", "Please select a source before following or unfollowing.");
            outputs.add(followObj);
            return;
        }
        if (selectedPlaylist.getOwner().equals(username)) {
            followObj.put("message", "You cannot follow or unfollow your own playlist.");
            outputs.add(followObj);
            return;
        }
        for (Playlist playlist : allPlaylists) {
            if (playlist.getId() == selectedPlaylist.getId() && playlist.getName().equals(selectedPlaylist.getName())) {
                if (playlist.getFollowersList().contains(username)) {
                    playlist.setFollowers(playlist.getFollowers() - 1);
                    playlist.getFollowersList().remove(username);
                    followObj.put("message", "Playlist unfollowed successfully.");
                } else {
                    playlist.setFollowers(playlist.getFollowers() + 1);
                    playlist.getFollowersList().add(username);
                    followObj.put("message", "Playlist followed successfully.");
                }
            }
        }
        outputs.add(followObj);
    }
}

class SwitchVisibility extends Command {
private final String username;
    private final int timestamp;
    private final String playlistName;
    private int playlistId;
    public SwitchVisibility(String username, int timestamp, String playlistName, int playlistId) {
        this.username = username;
        this.timestamp = timestamp;
        this.playlistName = playlistName;
        this.playlistId = playlistId;
    }
    public void execute(ArrayNode outputs) {
        ObjectNode playlistObj = new ObjectMapper().createObjectNode();
        playlistObj.put("command", "switchVisibility");
        playlistObj.put("user", username);
        playlistObj.put("timestamp", timestamp);
        for (Playlist playlist : allPlaylists) {
            if (playlist.getOwner().equals(username) && playlist.getId() == (playlistId)) {
                if (playlist.getVisibility().equals(Playlist.Visibility.PUBLIC)) {
                    playlist.setVisibility(Playlist.Visibility.PRIVATE);
                    playlistObj.put("message", "Visibility status updated successfully to private.");
                } else {
                    playlist.setVisibility(Playlist.Visibility.PUBLIC);
                    playlistObj.put("message", "Visibility status updated successfully to public.");
                }
            }
        }
        outputs.add(playlistObj);
    }
}