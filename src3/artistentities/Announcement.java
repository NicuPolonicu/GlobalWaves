package artistentities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Announcement {

    private String name;
    private String description;

    public Announcement(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
}
