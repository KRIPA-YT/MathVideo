package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;

import java.awt.*;


public interface Renderable {
    void render(Graphics2D g);

    default void draw() {
        MathVideo.getRenderer().registerRenderable(this);
    }

    default void delete() {
        MathVideo.getRenderer().deleteRenderable(this);
    }
}
