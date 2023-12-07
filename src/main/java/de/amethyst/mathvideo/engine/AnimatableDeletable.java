package de.amethyst.mathvideo.engine;

import java.time.Duration;

public interface AnimatableDeletable extends Animatable {
    void animateDelete(Duration duration);

    default void animateDeleteWait(Duration duration) throws InterruptedException {
        this.animateDelete(duration);
        Thread.sleep(duration.toMillis());
    }
}
