package de.amethyst.mathvideo;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class Renderer {

    private final List<Graph> graphs = new ArrayList<>();

    public boolean drawCoordinates = true;

    public void registerGraph(Graph graph) {
        if (graphs.contains(graph)) {
            return;
        }
        graphs.add(graph);
    }

    public void deleteGraph(Graph graph) {
        graphs.remove(graph);
    }

    public void render(Graphics2D g) {
        if (drawCoordinates) {
            drawLine(g, MathVideo.GRAY, 0.5, MathVideo.getInstance().getWidth() * -0.5, 0, MathVideo.getInstance().getWidth() * 0.5,  0);
            drawLine(g, MathVideo.GRAY, 0.5, 0, MathVideo.getInstance().getHeight() * -0.5, 0,  MathVideo.getInstance().getHeight() * 0.5);
        }
        for (Graph graph : graphs) {
            graph.render(g);
        }
    }

    public static void drawLine(Graphics2D graphics, Color color, double width, double x1, double y1, double x2, double y2) {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke((float) width));
        double renderX1 = MathVideo.getInstance().getWidth() / 2.0 + x1;
        double renderY1 = MathVideo.getInstance().getHeight() / 2.0 - y1;
        double renderX2 = MathVideo.getInstance().getWidth() / 2.0 + x2;
        double renderY2 = MathVideo.getInstance().getHeight() / 2.0 - y2;
        graphics.draw(new Line2D.Double(renderX1, renderY1, renderX2, renderY2));
    }
}
