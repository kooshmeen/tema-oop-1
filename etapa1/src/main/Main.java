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
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Locale;
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
        ArrayList<Command> commands = objectMapper.readValue(file, new TypeReference<>() { });
        Command.setAllPlaylists(new ArrayList<>());
//        Command.setSEARCHED_PLAYLIST(new ArrayList<>());
//        Command.setSEARCH_RES_PODCAST(new ArrayList<>());
//        Command.setSearchResSong(new ArrayList<>());
//        Command.setUSER_LIKED(new ArrayList<>());
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
    protected static boolean searchNull = false;
    protected static int originalDuration = 0;
    protected static boolean paused = true;
    protected static boolean shuffle = false;
    protected static int currentTimestamp = 0;
    protected static String repeat = "No Repeat";
    protected static boolean playlist = false;
    protected static String lastCommand = "";
    protected static ArrayList<Playlist> allPlaylists = new ArrayList<>();

//    public static void setSearchResSong(ArrayList<SongInput> searchResSong) {
//        Command.searchResSong = searchResSong;
//    }

//    public static void setSEARCHED_PLAYLIST(ArrayList<Playlist> SEARCHED_PLAYLIST) {
//        Command.SEARCHED_PLAYLIST = SEARCHED_PLAYLIST;
//    }

//    public static void setSEARCH_RES_PODCAST(ArrayList<PodcastInput> SEARCH_RES_PODCAST) {
//        Command.SEARCH_RES_PODCAST = SEARCH_RES_PODCAST;
//    }

//    public static void setUSER_LIKED(ArrayList<USER_LIKED> USER_LIKED) {
//        Command.USER_LIKED = USER_LIKED;
//    }

    public static void setAllPlaylists(ArrayList<Playlist> allPlaylists) {
        Command.allPlaylists = allPlaylists;
    }

    private final int seed = 0;
    protected static final ArrayList<USER_LIKED> USER_LIKED = new ArrayList<>();

    public int seed() {
        return seed;
    }
//
//    public void setSeed(int seed) {
//        this.seed = seed;
//    }

    public String getPlaylistName() {
        return playlistName;
    }

//    public void setPlaylistName(String playlistName) {
//        this.playlistName = playlistName;
//    }

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
            if (lastType == LastType.SONG && loadedSong != null) {
                loadedSong.setDuration(loadedSong.getDuration() - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0 && !playlist && repeat.equals("No Repeat")) {
                    loadedSong.setName("");
                    loadedSong.setDuration(0);
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1) {
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
                    loadedEpisode.setName("");
                    loadedEpisode.setDuration(0);
                    paused = true;
                } else if (loadedEpisode.getDuration() <= 0 && loadedPodcast.getEpisodes()
                        .indexOf(loadedEpisode)
                        != loadedPodcast.getEpisodes().size() - 1) {
                    int duration = loadedEpisode.getDuration();
                    loadedEpisode =
                            loadedPodcast.getEpisodes().get(loadedPodcast.getEpisodes()
                                    .indexOf(loadedEpisode) + 1);
                    loadedEpisode.setDuration(loadedEpisode.getDuration() + duration);
                }
            } else if (lastType == LastType.PLAYLIST && loadedSong != null) {
                loadedSong.setDuration(loadedSong.getDuration()
                        - (timestamp - currentTimestamp));
                if (loadedSong.getDuration() <= 0
                        && repeat.equals("No Repeat") && loadedPlaylist.getSongs().
                        indexOf(loadedSong) == loadedPlaylist.getSongs().size() - 1) {
                    loadedSong.setName("");
                    loadedSong.setDuration(0);
                    paused = true;
                } else if (loadedSong.getDuration() <= 0 && playlist
                        && loadedPlaylist.getSongs().indexOf(loadedSong)
                        < loadedPlaylist.getSongs().size() - 1) {
                    int duration = loadedSong.getDuration();
                    loadedSong =
                            loadedPlaylist.getSongs().get(loadedPlaylist.getSongs()
                                    .indexOf(loadedSong) + 1);
                    loadedSong.setDuration(loadedSong.getDuration() + duration);
                }
            }
        }
        currentTimestamp = timestamp;
        if (command.equals("search")) {
            Search search = new Search(username, timestamp, type, filters);
            search.executeSearch(outputs);
        }
        if (command.equals("select")) {
            Select select = new Select(username, timestamp, itemNumber);
            select.execute(outputs);
        }
        if (command.equals("load")) {
//            if (lastType == LastType.SONG || lastType == LastType.PLAYLIST) {
//                if (loadedSong != null) {
//                    loadedSong.setDuration(originalDuration);
//                }
//            } else if (lastType == LastType.PODCAST) {
//                if (loadedEpisode != null) {
//                    loadedEpisode.setDuration(originalDuration);
//                }
//            }
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
            int userPlaylists = 1;
            for (Playlist playlist : allPlaylists) {
                if (playlist.getOwner().equals(username)) {
                    userPlaylists++;
                }
            }
            CreatePlaylist createPlaylist = new CreatePlaylist(username, timestamp, playlistName, userPlaylists);
            createPlaylist.execute(outputs);
            //playlists.add(new Playlist(playlistName, playlistId, username, timestamp, outputs));
        }
        if (command.equals("addRemoveInPlaylist")) {
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
        if (command.equals("like")) {
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
        if (command.equals("showPlaylists")) {
            ShowPlaylist showPlaylist = new ShowPlaylist(username, timestamp);
            showPlaylist.execute(outputs);
        }
        if (command.equals("showPreferredSongs")) {
            ShowPrefferedSongs showPrefferedSongs = new ShowPrefferedSongs(username, timestamp);
            showPrefferedSongs.execute(outputs);
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
//        if (paused) {
            loadedSong = null;
//        }
//        if (loadedEpisode != null) {
//            loadedEpisode.setDuration(originalDuration);
//        }
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
                if (playlists.isEmpty()) {
                    searchObj.put("message", "no results found");
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
            if (itemNumber - 1 >= SEARCH_RES_SONG.size()) {
                selectObj.put("message", "The selected ID is too high.");
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
            }
            outputs.add(selectObj);
        } else if (lastType == LastType.PODCAST) {
            if (itemNumber - 1 >= SEARCH_RES_PODCAST.size()) {
                selectObj.put("message", "The selected ID is too high.");
            } else if (selectedPodcast == null) {
                selectObj.put("message", "Please conduct a search before making a selection.");
            } else {
                if (loadedEpisode != null) {
                    loadedEpisode.setDuration(originalDuration);
                }
                selectObj.put("message", "Successfully selected " + SEARCH_RES_PODCAST.get(itemNumber - 1).getName() + ".");
                selectedPodcast = SEARCH_RES_PODCAST.get(itemNumber - 1);
                originalDuration = selectedPodcast.getEpisodes().get(0).getDuration();
            }
            outputs.add(selectObj);
        } else if (lastType == LastType.PLAYLIST) {
            if (itemNumber - 1 >= SEARCHED_PLAYLIST.size()) {
                selectObj.put("message", "The selected ID is too high.");
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
                //originalDuration = selectedSong.getDuration();
                paused = false;
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
            } else if (loadedSong == null) {
                ObjectNode stats = statusObj.putObject("stats");
                stats.put("name", "");
                stats.put("remainedTime", 0);
                stats.put("repeat", repeat);
                stats.put("shuffle", shuffle);
                stats.put("paused", !paused);
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

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public Playlist() {
        songs = new ArrayList<>();
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
