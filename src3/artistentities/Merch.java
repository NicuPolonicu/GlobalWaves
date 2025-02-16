package artistentities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Merch {

    private String name;
    private String description;
    private double price;

    public Merch(final String name, final String description, final double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
