package audio.item;

import audioenum.AudioEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioItem {

    private AudioEnum type;

    public AudioItem(final AudioEnum type) {
        this.type = type;
    }
}
