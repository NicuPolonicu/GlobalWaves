package artistentities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {

    private String name;
    private String date;
    private String description;

    public Event(final String name, final String date, final String description) {
        this.name = name;
        this.date = date;
        this.description = description;
    }
}
