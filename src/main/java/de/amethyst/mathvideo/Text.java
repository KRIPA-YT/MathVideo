package de.amethyst.mathvideo;

import de.amethyst.mathvideo.engine.Animatable;
import de.amethyst.mathvideo.engine.Renderable;
import lombok.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import static de.amethyst.mathvideo.engine.RenderMath.*;
import static de.amethyst.mathvideo.MathVideo.*;

@EqualsAndHashCode(callSuper = true)
@ToString
public class Text extends Renderable implements Animatable {
    @Setter
    @Getter
    private Point2D position;

    @Setter
    @Getter
    private String content;

    @Setter
    @Getter
    private Color color;

    @Setter
    @Getter
    private int size;

    private static final Font renogare;

    @Setter
    @Getter
    private boolean centered;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Duration animationDuration;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private double animationT = 1;

    static {
        try {
            renogare = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Text.class.getResourceAsStream("/Renogare-Regular.otf")));
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Text(Point2D position, String content, Color color) {
        this(position, content, color, 100, true);
    }

    public Text(Point2D position, String content, Color color, int size) {
        this(position, content, color, size, true);
    }

    public Text(Point2D position, String content, Color color, int size, boolean centered) {
        this.setPosition(position);
        this.setContent(content);
        this.setColor(color);
        this.setSize(size);
        this.setCentered(centered);
    }

    @Override
    public void animate(Duration duration) {
        this.setAnimationT(0);
        this.setAnimationDuration(duration);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void animateWait(Duration duration) throws InterruptedException {
        this.animate(duration);
        Thread.sleep(duration.toMillis());
    }

    @Override
    public void render(Graphics2D g) {
        Font font = renogare.deriveFont((float) this.getSize());
        g.setFont(font);
        g.setColor(this.getColor());
        double renderX = MathVideo.getInstance().getWidth() / 2.0 + this.getPosition().getX();
        double renderY = MathVideo.getInstance().getHeight() / 2.0 - this.getPosition().getY();
        FontMetrics metrics = g.getFontMetrics(font);
        if (this.isCentered()) {
            renderX -= metrics.stringWidth(this.getContent()) / 2.0;
            renderY += (metrics.getHeight() - metrics.getAscent() - metrics.getDescent()) / 2.0;
        }
        updateAnimation(g, renderX, metrics.stringWidth(this.getContent()));
        g.drawString(this.getContent(), (float) renderX, (float) renderY);
    }

    private void updateAnimation(Graphics2D g, double x, double width) {
        if (this.getAnimationT() >= 1) {
            if (this.getAnimationT() > 1) {
                this.setAnimationT(1);
            }
            return;
        }
        g.setPaint(new GradientPaint((float) cerp(this.getAnimationT(), x, x + width), 0, this.getColor(), (float) cerp(this.getAnimationT(), x, x + width) + this.getSize(), 0, new Color(0, 0, 0, 0)));

        this.setAnimationT(this.getAnimationT() + 1 / (FRAMERATE * this.getAnimationDuration().toMillis() / 1000));
    }
}
