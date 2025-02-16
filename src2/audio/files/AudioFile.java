package audio.files;

import audio.item.AudioItem;
import enums.AudioEnum;
import lombok.Getter;

@Getter
public abstract class AudioFile extends AudioItem {

    private final String name;
    private final Integer duration;

    public AudioFile(final String name, final Integer duration, final AudioEnum type) {
        super(type);
        this.name = name;
        this.duration = duration;
    }
}
