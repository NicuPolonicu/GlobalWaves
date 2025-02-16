package commands;

import audio.files.Filters;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Command {

    private String command;
    private String username;
    private Integer timestamp;

    private String type;
    private Filters filters;
    private Integer itemNumber;
    private Integer seed;
    private Integer playlistId;
    private String playlistName;
    public Command() {
        // Default constructor needed for JSON deserialization
    }
}
