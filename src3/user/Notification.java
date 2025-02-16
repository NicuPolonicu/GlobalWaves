package user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Notification {
    private String description;
    private String name;

    public Notification(final String description, final String name) {
        this.description = description;
        this.name = name;
    }

}
