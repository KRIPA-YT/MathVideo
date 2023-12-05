package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.awt.*;


@EqualsAndHashCode
@ToString
public abstract class Renderable {
    public abstract void render(Graphics2D g);

    public void draw() {
        MathVideo.getRenderer().registerRenderable(this);
    }
}
