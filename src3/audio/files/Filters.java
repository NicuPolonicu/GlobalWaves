package audio.files;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Filters {

    private String name;
    private String album;
    private ArrayList<String> tags;
    private String lyrics;
    private String genre;
    private String releaseYear;
    private String artist;
    private String owner;
    private String description;
}
