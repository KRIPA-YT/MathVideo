package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;
import de.amethyst.mathvideo.ReflectionUtil;
import lombok.*;
import lombok.experimental.Accessors;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.function.Function;

import static de.amethyst.mathvideo.engine.RenderMath.*;
import static de.amethyst.mathvideo.MathVideo.*;


@EqualsAndHashCode(callSuper = false)
@ToString

@Accessors(chain = true)
public class Graph implements AnimatableDeletable, Cloneable {
    @Setter
    @Getter
    private Function<Double, Double> function;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Paint paint;

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

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Duration animationDuration;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private double animationPercentage = 1;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Duration morphDuration;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private double morphPercentage = 1;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Graph morphTarget = this;

    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private Duration deletionDuration;
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private double deletionPercentage = 0;

    public Graph(Function<Double, Double> function, Paint paint, double width, int start, int stop, int scale, boolean smoothInterpolate) {
        this.setFunction(function);
        this.setPaint(paint);
        this.setWidth(width);
        this.setStart(start);
        this.setStop(stop);
        this.setScale(scale);
        this.setSmoothInterpolate(smoothInterpolate);
    }

    public double evaluate(double x) {
        return this.getFunction().apply(x / this.getScale()) * this.getScale();
    }

    @Override
    public void animate(Duration duration) {
        this.setAnimationDuration(duration);
        this.setAnimationPercentage(0);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void morph(Duration duration, Graph morphTarget) {
        this.setMorphDuration(duration);
        this.setMorphPercentage(0);
        this.setMorphTarget(morphTarget);
        MathVideo.getRenderer().registerRenderable(this);
    }

    public void morphWait(Duration duration, Graph morphTarget) throws InterruptedException {
        this.morph(duration, morphTarget);
        Thread.sleep(duration.toMillis());
    }

    @Override
    public void render(Graphics2D g) {
        double start = interpolate(this.getDeletionPercentage(), this.getStart(), this.getStop());
        double stop = interpolate(this.getAnimationPercentage(), this.getStart(), this.getStop());
        for (double i = start; i < stop; i += RESOLUTION) {
            double y1 = interpolate(this.getMorphPercentage(), this.evaluate(i), this.getMorphTarget().evaluate(i));
            double y2 = interpolate(this.getMorphPercentage(), this.evaluate(i + RESOLUTION), this.getMorphTarget().evaluate(i + RESOLUTION));
            int[] color = this.getColorAtPos(g, this.getPaint(), new Point2D.Double(i, y1));
            int[] targetColor = this.getColorAtPos(g, this.getMorphTarget().getPaint(), new Point2D.Double(i, y1));
            float[] startHSB = Color.RGBtoHSB(color[0], color[1], color[2], null);
            float[] targetHSB = Color.RGBtoHSB(targetColor[0], targetColor[1], targetColor[2],null);
            // Adjust for Residual class of Hue Part 1
            if (Math.abs(startHSB[0] - targetHSB[0]) > 0.5) {
                if (startHSB[0] > targetHSB[0]) {
                    startHSB[0] -= 1;
                } else {
                    targetHSB[0] -= 1;
                }
            }
            double[] interHSB = {
                    interpolate(this.getMorphPercentage(), startHSB[0], targetHSB[0]),
                    interpolate(this.getMorphPercentage(), startHSB[1], targetHSB[1]),
                    interpolate(this.getMorphPercentage(), startHSB[2], targetHSB[2])};
            // Adjust for Residual class of Hue Part 2
            if (interHSB[0] < 0) {
                interHSB[0] += 1;
            }
            Renderer.drawLine(g, new Color(Color.HSBtoRGB((float) interHSB[0], (float) interHSB[1], (float) interHSB[2])), this.getWidth(), new Point2D.Double(i, y1), new Point2D.Double(i + RESOLUTION, y2));
        }
        updateAnimation();
        updateMorph();
        updateDeletion();
    }

    @Override
    public void animateDelete(Duration duration) {
        this.setDeletionDuration(duration);
        this.setDeletionPercentage(0);
    }

    private void updateAnimation() {
        if (this.isAnimationFinished()) {
            return;
        }
        this.setAnimationPercentage(this.getAnimationPercentage() + 1 / (FRAMERATE * this.getAnimationDuration().toMillis() / 1000));
    }

    private void updateMorph() {
        if (this.isMorphFinished()) {
            return;
        }
        this.setMorphPercentage(this.getMorphPercentage() + 1 / (FRAMERATE * this.getMorphDuration().toMillis() / 1000));
    }

    private void updateDeletion() {
        if (this.isDeletionFinished()) {
            return;
        }
        this.setDeletionPercentage(this.getDeletionPercentage() + 1 / (FRAMERATE * this.getDeletionDuration().toMillis() / 1000));
    }

    private boolean isAnimationFinished() {
        if (this.getAnimationPercentage() >= 1) {
            if (this.getAnimationPercentage() > 1) {
                this.setAnimationPercentage(1);
            }
            return true;
        }

        if (this.getAnimationDuration().isZero()) {
            this.setAnimationPercentage(1);
            return true;
        }

        return false;
    }

    private boolean isMorphFinished() {
        if (this.getMorphPercentage() >= 1) {
            if (this.getMorphPercentage() > 1) {
                this.setMorphPercentage(1);
            }
            if (this.getMorphTarget() != this) {
                copySelf();
            }
            return true;
        }

        if (this.getMorphDuration().isZero()) {
            this.setMorphPercentage(1);
            return true;
        }

        return false;
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

    private int[] getColorAtPos(Graphics2D g, Paint paint, Point2D pos) {
        AffineTransform userSpaceToDeviceSpace = g.getTransform();
        PaintContext paintContext = paint.createContext(
                ColorModel.getRGBdefault(),
                g.getDeviceConfiguration().getBounds(),
                MathVideo.getInstance().getBounds(),
                g.getTransform(),
                g.getRenderingHints());
        Point2D userSpace = Renderer.coordinateSpaceToUserSpace(pos);
        Point2D deviceSpace = userSpaceToDeviceSpace.transform(userSpace, null);
        return paintContext.getRaster((int) deviceSpace.getX(), (int) deviceSpace.getY(), (int) RESOLUTION, (int) RESOLUTION).getPixel(0, 0, (int[]) null);
    }

    private void copySelf() {
        Iterable<Field> fields = ReflectionUtil.getFieldsUpTo(this.getClass(), Object.class);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(this, field.get(this.getMorphTarget()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.setFunction(this.getMorphTarget().getFunction());
        this.setMorphTarget(this);
    }

    @Override
    public Graph clone() {
        try {
            return (Graph) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
