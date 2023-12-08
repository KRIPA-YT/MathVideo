package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;
import lombok.*;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import static de.amethyst.mathvideo.engine.RenderMath.*;
import static de.amethyst.mathvideo.MathVideo.*;

@EqualsAndHashCode(callSuper = false)
@ToString
@Accessors(chain = true)
public class Text implements AnimatableDeletable, Cloneable {
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

    @Setter
    @Getter
    private boolean smoothInterpolate;

    @Setter
    @Getter
    private boolean smoothDelete = true;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Duration animationDuration;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private double animationPercentage = 1;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Duration deletionDuration;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private double deletionPercentage = 0;

    static {
        try {
            renogare = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Text.class.getResourceAsStream("/Renogare-Regular.otf")));
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Text(Point2D position, String content, Color color) {
        this(position, content, color, 100, true, true);
    }

    public Text(Point2D position, String content, Color color, int size) {
        this(position, content, color, size, true, true);
    }

    public Text(Point2D position, String content, Color color, int size, boolean centered) {
        this(position, content, color, size, centered, true);
    }

    public Text(Point2D position, String content, Color color, int size, boolean centered, boolean smoothInterpolate) {
        this.setPosition(position);
        this.setContent(content);
        this.setColor(color);
        this.setSize(size);
        this.setCentered(centered);
        this.setSmoothInterpolate(smoothInterpolate);
    }

    @Override
    public void animate(Duration duration) {
        this.setAnimationPercentage(0);
        this.setAnimationDuration(duration);
        MathVideo.getRenderer().registerRenderable(this);
    }

    @Override
    public void render(Graphics2D g) {
        Font font = renogare.deriveFont((float) this.getSize());
        g.setFont(font);
        g.setColor(this.getColor());
        Point2D renderCoords = Renderer.coordinateSpaceToUserSpace(this.getPosition());
        FontMetrics metrics = g.getFontMetrics(font);
        if (this.isCentered()) {
            AffineTransform centerShift = AffineTransform.getTranslateInstance(
                    metrics.stringWidth(this.getContent()) / -2.0,
                    (metrics.getHeight() - metrics.getAscent() - metrics.getDescent()) / 2.0);
            renderCoords = centerShift.transform(renderCoords, null);
        }
        updateAnimation(g, renderCoords.getX(), metrics.stringWidth(this.getContent()));
        updateDeletion(g, renderCoords.getX(), metrics.stringWidth(this.getContent()));
        g.drawString(this.getContent(), (float) renderCoords.getX(), (float) renderCoords.getY());
    }

    @Override
    public void animateDelete(Duration duration) {
        this.setDeletionDuration(duration);
        this.setDeletionPercentage(0);
    }

    private void updateAnimation(Graphics2D g, double x, double width) {
        if (this.getAnimationPercentage() >= 1) {
            if (this.getAnimationPercentage() > 1) {
                this.setAnimationPercentage(1);
            }
            return;
        }
        g.setPaint(new GradientPaint((float) interpolate(this.getAnimationPercentage(), x, x + width), 0, this.getColor(), (float) interpolate(this.getAnimationPercentage(), x, x + width) + this.getSize(), 0, new Color(0, 0, 0, 0)));

        this.setAnimationPercentage(this.getAnimationPercentage() + 1 / (FRAMERATE * this.getAnimationDuration().toMillis() / 1000));
    }

    private void updateDeletion(Graphics2D g, double x, double width) {
        if (this.getDeletionDuration() == null) { // Deletion hasn't started
            return;
        }

        if (this.isDeletionFinished()) {
            g.setColor(new Color(0, 0, 0, 0));
            return;
        }

        if (this.isSmoothDelete()) {
            g.setPaint(new GradientPaint((float) interpolate(this.getDeletionPercentage(), x, x + width), 0, new Color(0, 0, 0, 0), (float) interpolate(this.getDeletionPercentage(), x, x + width) + this.getSize(), 0, this.getColor()));
        } else {
            g.setColor(new Color(
                    this.getColor().getRed(), this.getColor().getBlue(), this.getColor().getGreen(),
                    (int) interpolate(this.getDeletionPercentage(), this.getColor().getAlpha(), 0)));
        }

        this.setDeletionPercentage(this.getDeletionPercentage() + 1 / (FRAMERATE * this.getDeletionDuration().toMillis() / 1000));
    }

    private boolean isDeletionFinished() {
        if (this.getDeletionPercentage() >= 1) {
            if (this.getDeletionPercentage() > 1) {
                this.setDeletionPercentage(1);
            }
            MathVideo.getRenderer().deleteRenderable(this);
            return true;
        }

        if (this.getDeletionDuration() == null) {
            return true;
        }

        if (this.getDeletionDuration().isZero()) {
            this.setDeletionPercentage(1);
            return true;
        }

        return false;
    }

    private double interpolate(double distance, double start, double stop) {
        return this.isSmoothInterpolate() ? cerp(distance, start, stop) : lerp(distance, start, stop);
    }

    @Override
    public Text clone() {
        try {
            return (Text) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
