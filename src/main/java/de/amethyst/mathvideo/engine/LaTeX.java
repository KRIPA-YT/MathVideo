package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import static de.amethyst.mathvideo.MathVideo.FRAMERATE;
import static de.amethyst.mathvideo.engine.RenderMath.*;
import static java.lang.Math.*;

@Accessors(chain = true)
public class LaTeX implements AnimatableDeletable {
    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
    @Setter
    @Getter
    private String laTeX = "";

    @Setter
    @Getter
    private Color color;

    @Setter
    @Getter
    private Point2D position;

    @Setter
    @Getter
    private int scale;

    @Setter
    @Getter
    private Alignment alignment;

    @Setter
    @Getter
    private boolean smoothInterpolate;

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

    public LaTeX(String laTeX, Color color, Point2D position) {
        this(laTeX, color, position, 50, Alignment.LEFT, true);
    }

    public LaTeX(String laTeX, Color color, Point2D position, int scale) {
        this(laTeX, color, position, scale, Alignment.LEFT, true);
    }

    public LaTeX(String laTeX, Color color, Point2D position, int scale, Alignment alignment) {
        this(laTeX, color, position, scale, alignment, true);
    }

    public LaTeX(String laTeX, Color color, Point2D position, int scale, Alignment alignment, boolean smoothInterpolate) {
        this.setLaTeX(laTeX);
        this.setColor(color);
        this.setPosition(position);
        this.setScale(scale);
        this.setAlignment(alignment);
        this.setSmoothInterpolate(smoothInterpolate);
    }

    @Override
    public void render(Graphics2D g) {
        TeXFormula formula = new TeXFormula(this.getLaTeX());
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, this.getScale());
        Color renderColor = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(),
                (int) interpolate(max(0, this.getAnimationPercentage() - this.getDeletionPercentage()), 0, this.getColor().getAlpha()));
        icon.setForeground(renderColor);

        Point2D renderPosition = Renderer.coordinateSpaceToUserSpace(this.getPosition());
        AffineTransform aligner;
        switch (this.getAlignment()) {
            case CENTER -> {
                aligner = AffineTransform.getTranslateInstance(icon.getIconWidth() / -2.0, icon.getIconHeight() / 2.0);
                renderPosition = aligner.transform(renderPosition, null);
            }
            case RIGHT -> {
                aligner = AffineTransform.getTranslateInstance(-icon.getIconWidth(), -icon.getIconHeight());
                renderPosition = aligner.transform(renderPosition, null);
            }
        }
        icon.paintIcon(null, g, (int) renderPosition.getX(), (int) renderPosition.getY());
        updateAnimation();
        updateDeletion(g);
    }

    private void updateAnimation() {
        if (this.getAnimationPercentage() >= 1) {
            if (this.getAnimationPercentage() > 1) {
                this.setAnimationPercentage(1);
            }
            return;
        }

        this.setAnimationPercentage(this.getAnimationPercentage() + 1 / (FRAMERATE * this.getAnimationDuration().toMillis() / 1000));
    }

    private void updateDeletion(Graphics2D g) {
        if (this.getDeletionDuration() == null) { // Deletion hasn't started
            return;
        }

        if (this.isDeletionFinished()) {
            g.setColor(new Color(0, 0, 0, 0));
            return;
        }

        this.setDeletionPercentage(this.getDeletionPercentage() + 1 / (FRAMERATE * this.getDeletionDuration().toMillis() / 1000));
    }

    private boolean isDeletionFinished() {
        if (this.getDeletionPercentage() >= 1) {
            if (this.getDeletionPercentage() > 1) {
                this.setDeletionPercentage(1);
            }
            MathVideo.getRenderer().deleteRenderable(this);
            this.setDeletionDuration(null);
            this.setDeletionPercentage(0);
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

    @Override
    public void animate(Duration duration) {
        this.setAnimationDuration(duration);
        this.setAnimationPercentage(0);
        MathVideo.getRenderer().registerRenderable(this);
    }

    @Override
    public void animateDelete(Duration duration) {
        this.setDeletionDuration(duration);
        this.setDeletionPercentage(0);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void morph(Duration duration, String targetLaTeX) {
        this.animateDelete(duration.dividedBy(2));
        LaTeX self = this;
        Timer deletionTimer = new Timer();
        deletionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) (2000 / FRAMERATE)); // Wait two frames
                } catch (InterruptedException ignored) {}
                setLaTeX(targetLaTeX);
                animate(duration.dividedBy(2));
            }
        }, duration.dividedBy(2).toMillis());
    }

    public void morphWait(Duration duration, String targetLatTeX) throws InterruptedException {
        morph(duration, targetLatTeX);
        Thread.sleep(duration.dividedBy(2).toMillis());
    }

    private double interpolate(double distance, double start, double stop) {
        return this.isSmoothInterpolate() ? cerp(distance, start, stop) : lerp(distance, start, stop);
    }
}
