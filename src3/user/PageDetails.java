package user;

import lombok.Getter;
import lombok.Setter;
import enums.Page;

@Getter
@Setter
public class PageDetails {

    private Page type;
    private String owner;

    public PageDetails(final Page type, final String owner) {
        this.type = type;
        this.owner = owner;
    }

}
