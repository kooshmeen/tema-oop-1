package main;

import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.LibraryInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
        ArrayList<Command> commands = objectMapper.readValue(file, new TypeReference<>(){});
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
    private String playlistId;
    protected enum LastType {
        SONG, PODCAST
    }
    protected static LastType lastType;
    protected static ArrayList<SongInput> searchResSong = new ArrayList<>();
    protected static ArrayList<PodcastInput> searchResPodcast = new ArrayList<>();
    protected static SongInput selectedSong = new SongInput();
    protected static PodcastInput selectedPodcast = new PodcastInput();
    protected static SongInput loadedSong = new SongInput();
    protected static PodcastInput loadedPodcast = new PodcastInput();
    protected static boolean paused = true;
    protected static boolean shuffle = false;
    protected static int currentTimestamp = 0;
    protected static String repeat = "No Repeat";
    protected static boolean playlist = false;
    protected static ArrayList<Playlist> playlists = new ArrayList<>();
    private int seed;

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
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
        if (command == null || command.isEmpty()) {
            outputs.add("Invalid command");
        }
        if (!paused) {
            if (lastType == LastType.SONG && loadedSong != null) {
                loadedSong.setDuration(loadedSong.getDuration() - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0) {
                    loadedSong.setName("");
                    loadedSong.setDuration(0);
                    paused = true;
                }
            }
        }
        if (command.equals("search")) {
            Search search = new Search(username, timestamp, type, filters);
            search.executeSearch(outputs);
        }
        if (command.equals("select")) {
            Select select = new Select(username, timestamp, itemNumber);
            select.execute(outputs);
        }
        if (command.equals("load")) {
            Load load = new Load(username, timestamp);
            load.execute(outputs);
        }
        if (command.equals("status")) {
            Status status = new Status(username, timestamp);
            status.execute(outputs);
        }
        if (command.equals("playPause")) {
            PlayPause playPause = new PlayPause(username, timestamp);
            playPause.execute(outputs);
        }
        if (command.equals("createPlaylist")) {
            playlists.add(new Playlist(playlistName, playlistId, username, timestamp, outputs));
        }
        if (command.equals("addRemoveInPlaylist")) {
            playlists.get(playlists.size()).addRemoveInPlaylist(playlistName, playlistId, username, timestamp, outputs);
        }
        currentTimestamp = timestamp;
    }
}

class Search extends Command {
    private final String username;
    private final int timestamp;
    private final String type;
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
                        songs.removeIf(song -> !song.getLyrics().contains((CharSequence) filters.get("lyrics")));
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
                    searchResSong.clear();
                    searchResSong.addAll(songs);
                    Command.lastType = LastType.SONG;
                }
            }
            case "playlist" -> {
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
                searchResPodcast.clear();
                searchResPodcast.addAll(podcasts);
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
            if (itemNumber - 1 >= searchResSong.size()) {
                selectObj.put("message", "The selected ID is too high.");
            } else if (searchResSong.isEmpty()) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                selectObj.put("message", "Successfully selected " + searchResSong.get(itemNumber - 1).getName() + ".");
                selectedSong = searchResSong.get(itemNumber - 1);
            }
            outputs.add(selectObj);
        } else if (lastType == LastType.PODCAST) {
            if (itemNumber - 1 > searchResPodcast.size()) {
                selectObj.put("message", "The selected ID is too high.");
            } else if (selectedPodcast == null) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                selectObj.put("message", "Successfully selected " + searchResPodcast.get(itemNumber - 1) + ".");
                selectedPodcast = searchResPodcast.get(itemNumber - 1);
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
        if (lastType == LastType.SONG) {
            if (selectedSong == null) {
                loadObj.put("message", "Please select a source before attempting to load.");
            } else {
                loadObj.put("message", "Playback loaded successfully.");
                loadedSong = selectedSong;
                paused = false;
                currentTimestamp = timestamp;
            }
        } else if (lastType == LastType.PODCAST) {
            if (selectedPodcast == null) {
                loadObj.put("message", "Please select a source before attempting to load.");
            } else {
                loadObj.put("message", "Playback loaded successfully.");
                loadedPodcast = selectedPodcast;
                paused = false;
                currentTimestamp = timestamp;
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
            if (loadedSong == null) {
                statusObj.put("message", "No song is currently playing.");
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
                statusObj.put("message", "Podcast is currently playing.");
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
    private String id;
    private String owner;
    private ArrayList<SongInput> songs;
    private enum Visibility {
        PUBLIC, PRIVATE
    }
    private Visibility visibility;

    public Playlist(String name, String id, String owner, int timestamp, ArrayNode outputs) {
        this.name = name;
        this.id = id;
        this.owner = owner;
        this.visibility = Visibility.PUBLIC;
        ObjectNode playlistObj = new ObjectMapper().createObjectNode();
        playlistObj.put("command", "createPlaylist");
        playlistObj.put("user", owner);
        playlistObj.put("timestamp", timestamp);
        playlistObj.put("message", "Playlist created successfully.");
        outputs.add(playlistObj);
    }
    public void addRemoveInPlaylist(String name, String id, String owner, int timestamp, ArrayNode outputs) {
        boolean exists = false;
        if (songs == null) {
            songs = new ArrayList<>();
        }
        for (SongInput song : songs) {
            if (song.getName().equals(Command.loadedSong.getName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            songs.add(Command.loadedSong);
            ObjectNode playlistObj = new ObjectMapper().createObjectNode();
            playlistObj.put("command", "addRemoveInPlaylist");
            playlistObj.put("user", owner);
            playlistObj.put("timestamp", Command.currentTimestamp);
            playlistObj.put("message", "Successfully added to playlist.");
            outputs.add(playlistObj);
        } else {
            songs.remove(Command.loadedSong);
            ObjectNode playlistObj = new ObjectMapper().createObjectNode();
            playlistObj.put("command", "addRemoveInPlaylist");
            playlistObj.put("user", owner);
            playlistObj.put("timestamp", Command.currentTimestamp);
            playlistObj.put("message", "Successfully removed from playlist.");
            outputs.add(playlistObj);
        }
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
