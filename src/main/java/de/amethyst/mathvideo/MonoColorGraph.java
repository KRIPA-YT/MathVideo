package de.amethyst.mathvideo;

import de.amethyst.mathvideo.engine.HeightCodedGraph;

import java.awt.*;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class MonoColorGraph extends HeightCodedGraph {
    public MonoColorGraph(Function<Double, Double> function, Color paint, double width) {
        this(function, paint, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), 1, true);
    }

    public MonoColorGraph(Function<Double, Double> function, Color paint, double width, int scale) {
        this(function, paint, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), scale, true);
    }

    public MonoColorGraph(Function<Double, Double> function, Color paint, double width, int start, int stop) {
        this(function, paint, width, start, stop, 1, true);
    }

    public MonoColorGraph(Function<Double, Double> function, Color paint, double width, int start, int stop, int scale) {
        this(function, paint, width, start, stop, scale, true);
    }

    public MonoColorGraph(Function<Double, Double> function, Color color, double width, int start, int stop, int scale, boolean smoothInterpolate) {
        super(function, Collections.singletonMap(0, color), width, start, stop, scale, smoothInterpolate);
    }

    @Deprecated
    public MonoColorGraph setColorCodes(Map<Integer, Color> colorCodes) {
        return this;
    }

    public MonoColorGraph setColor(Color color) {
        this.setPaint(color);
        return this;
    }

    public Color getColor() {
        return (Color) this.getPaint();
    }

    @Override
    public MonoColorGraph clone() {
        return (MonoColorGraph) super.clone();
    }
}
