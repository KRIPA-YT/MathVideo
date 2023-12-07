package de.amethyst.mathvideo.engine;

import java.time.Duration;

public interface Animatable extends Renderable {
    void animate(Duration duration);
    default void animateWait(Duration duration) throws InterruptedException {
        this.animate(duration);
        Thread.sleep(duration.toMillis());
    }
}
