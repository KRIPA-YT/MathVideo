package de.amethyst.mathvideo.engine;

import com.sun.source.tree.Tree;
import de.amethyst.mathvideo.Graph;
import de.amethyst.mathvideo.MathVideo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.amethyst.mathvideo.engine.RenderMath.*;

public class HeightCodedGraph extends Graph {
    public HeightCodedGraph(Function<Double, Double> function, Map<Integer, Color> colorCodes, double width) {
        this(function, colorCodes, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), 1, true);
    }

    public HeightCodedGraph(Function<Double, Double> function, Map<Integer, Color> colorCodes, double width, int scale) {
        this(function, colorCodes, width, (int) (MathVideo.getInstance().getWidth() * -0.5), (int) (MathVideo.getInstance().getWidth() * 0.5), scale, true);
    }

    public HeightCodedGraph(Function<Double, Double> function, Map<Integer, Color> colorCodes, double width, int start, int stop) {
        this(function, colorCodes, width, start, stop, 1, true);
    }

    public HeightCodedGraph(Function<Double, Double> function, Map<Integer, Color> colorCodes, double width, int start, int stop, int scale) {
        this(function, colorCodes, width, start, stop, scale, true);
    }

    public HeightCodedGraph(Function<Double, Double> function, Map<Integer, Color> colorCodes, double width, int start, int stop, int scale, boolean smoothInterpolate) {
        super(function, generatePaint(colorCodes, scale), width, start, stop, scale, smoothInterpolate);
    }

    public HeightCodedGraph setColorCodes(Map<Integer, Color> colorCodes) {
        this.setPaint(generatePaint(colorCodes, this.getScale()));
        return this;
    }

    private static Paint generatePaint(Map<Integer, Color> colorCodes, int scale) throws IllegalArgumentException {
        if (colorCodes.isEmpty()) {
            throw new IllegalArgumentException("colorCodes must have at least 1 element!");
        }
        if (colorCodes.size() == 1) {
            return colorCodes.entrySet().stream().toList().get(0).getValue();
        }
        // Extract min, max + other data from colorCodes
        Point2D top = new Point2D.Double(0, Collections.max(colorCodes.entrySet(), Map.Entry.comparingByKey()).getKey());
        Point2D bottom = new Point2D.Double(0, Collections.min(colorCodes.entrySet(), Map.Entry.comparingByKey()).getKey());
        float[] fractions = doubleToFloat(
                colorCodes.keySet().stream()
                .map(height -> invLerp(height, top.getY(), bottom.getY()))
                .mapToDouble(f -> f).toArray());
        Color[] colors = colorCodes.values().toArray(Color[]::new);
        // Sort colorCodes
        Map<Float, Color> colorMap = new TreeMap<>();
        for (int i = 0; i < fractions.length; i++) {
            colorMap.put(fractions[i], colors[i]);
        }
        List<Map.Entry<Float, Color>> entrySet = colorMap.entrySet().stream().toList();
        for (int i = 0; i < colorMap.size(); i++) {
            fractions[i] = entrySet.get(i).getKey();
            colors[i] = entrySet.get(i).getValue();
        }
        return new LinearGradientPaint(Renderer.coordinateSpaceToUserSpace(top, scale), Renderer.coordinateSpaceToUserSpace(bottom, scale), fractions, colors);
    }
}
