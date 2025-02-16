package audio.lists;

import audio.item.AudioItem;
import audioenum.AudioEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioList extends AudioItem {

    private final String name;

    public AudioList(final String name, final AudioEnum type) {
        super(type);
        this.name = name;
    }

}
