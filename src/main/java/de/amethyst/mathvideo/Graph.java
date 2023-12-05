package de.amethyst.mathvideo;

import de.amethyst.mathvideo.engine.Animatable;
import de.amethyst.mathvideo.engine.Renderable;
import de.amethyst.mathvideo.engine.Renderer;
import lombok.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.function.Function;

import static de.amethyst.mathvideo.engine.RenderMath.*;
import static de.amethyst.mathvideo.MathVideo.*;


@EqualsAndHashCode(callSuper = true)
@ToString
public class Graph extends Renderable implements Animatable {
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private Function<Double, Double> function;

    @Setter
    @Getter
    private Color color;

    @Setter
    @Getter
    private int scale;

    @Setter
    @Getter
    private double width;

    @Setter
    @Getter
    private int start;

    @Setter
    @Getter
    private int stop;

    @Setter
    @Getter
    private boolean smoothInterpolate;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Duration animationDuration;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private double animationT = 1;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Duration morphDuration;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private double morphT = 1;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Graph morphTo = this;

    public Graph(Function<Double, Double> function, Color color, double width) {
        this(function, color, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), 1, true);
    }

    public Graph(Function<Double, Double> function, Color color, double width, int scale) {
        this(function, color, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), scale, true);
    }

    public Graph(Function<Double, Double> function, Color color, double width, int start, int stop) {
        this(function, color, width, start, stop, 1, true);
    }

    public Graph(Function<Double, Double> function, Color color, double width, int start, int stop, int scale) {
        this(function, color, width, start, stop, 1, true);
    }

    public Graph(Function<Double, Double> function, Color color, double width, int start, int stop, int scale, boolean smoothInterpolate) {
        this.setFunction(function);
        this.setColor(color);
        this.setWidth(width);
        this.setStart(start);
        this.setStop(stop);
        this.setScale(scale);
    }

    public double evaluate(double x) {
        return this.getFunction().apply(x / this.getScale()) * this.getScale();
    }

    @Override
    public void animate(Duration duration) {
        this.setAnimationDuration(duration);
        this.setAnimationT(0);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void animateWait(Duration duration) throws InterruptedException {
        this.animate(duration);
        Thread.sleep(duration.toMillis());
    }

    public void morph(Duration duration, Graph morphTo) {
        this.setMorphDuration(duration);
        this.setMorphT(0);
        this.setMorphTo(morphTo);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void morphWait(Duration duration, Graph morphTo) throws InterruptedException {
        this.morph(duration, morphTo);
        Thread.sleep(duration.toMillis());
    }

    public void delete() {
        MathVideo.getRenderer().deleteRenderable(this);
    }

    @Override
    public void render(Graphics2D g) {
        for (double i = this.getStart(); i < cerp(this.getAnimationT(), this.getStart(), this.getStop()); i += RESOLUTION) {
            double y1 = cerp(this.getMorphT(), this.evaluate(i), this.getMorphTo().evaluate(i));
            double y2 = cerp(this.getMorphT(), this.evaluate(i + RESOLUTION), this.getMorphTo().evaluate(i + RESOLUTION));
            float[] startHSB = Color.RGBtoHSB(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), null);
            float[] targetHSB = Color.RGBtoHSB(this.getMorphTo().getColor().getRed(), this.getMorphTo().getColor().getGreen(), this.getMorphTo().getColor().getBlue(), null);
            // Adjust for Residual class of Hue Part 1
            if (Math.abs(startHSB[0] - targetHSB[0]) > 0.5) {
                if (startHSB[0] > targetHSB[0]) {
                    startHSB[0] -= 1;
                } else {
                    targetHSB[0] -= 1;
                }
            }
            double[] interHSB = {cerp(this.getMorphT(), startHSB[0], targetHSB[0]), cerp(this.getMorphT(), startHSB[1], targetHSB[1]), cerp(this.getMorphT(), startHSB[2], targetHSB[2])};
            // Adjust for Residual class of Hue Part 2
            if (interHSB[0] < 0) {
                interHSB[0] += 1;
            }
            Renderer.drawLine(g, new Color(Color.HSBtoRGB((float) interHSB[0], (float) interHSB[1], (float) interHSB[2])), this.getWidth(), i, y1, i + RESOLUTION, y2);
        }
        updateAnimation();
        updateMorph();
    }

    private void updateAnimation() {
        if (this.isAnimationFinished()) {
            return;
        }
        this.setAnimationT(this.getAnimationT() + 1 / (FRAMERATE * this.getAnimationDuration().toMillis() / 1000));
    }

    private void updateMorph() {
        if (this.isMorphFinished()) {
            return;
        }
        this.setMorphT(this.getMorphT() + 1 / (FRAMERATE * this.getMorphDuration().toMillis() / 1000));
    }

    private boolean isAnimationFinished() {
        if (this.getAnimationT() >= 1) {
            if (this.getAnimationT() > 1) {
                this.setAnimationT(1);
            }
            return true;
        }

        if (this.getAnimationDuration().isZero()) {
            this.setAnimationT(1);
            return true;
        }

        return false;
    }

    private boolean isMorphFinished() {
        if (this.getMorphT() >= 1) {
            if (this.getMorphT() > 1) {
                this.setMorphT(1);
            }
            if (this.getMorphTo() != this) {
                copySelf();
            }
            return true;
        }

        if (this.getMorphDuration().isZero()) {
            this.setMorphT(1);
            return true;
        }

        return false;
    }

    private void copySelf() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(this, field.get(this.getMorphTo()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.setMorphTo(this);
    }
}
