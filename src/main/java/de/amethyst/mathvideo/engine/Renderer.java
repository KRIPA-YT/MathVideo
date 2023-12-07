package de.amethyst.mathvideo.engine;

import de.amethyst.mathvideo.MathVideo;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

@ToString
@EqualsAndHashCode
public class Renderer {

    private final List<Renderable> renderables = new ArrayList<>();
    private final List<Runnable> renderModificationQueue = new ArrayList<>();

    public boolean drawCoordinates = true;

    public void registerRenderable(Renderable renderable) {
        if (renderables.contains(renderable)) {
            return;
        }
        renderModificationQueue.add(() -> renderables.add(renderable));
    }

    public void deleteRenderable(Renderable renderable) {
        renderModificationQueue.add(() -> renderables.remove(renderable));
    }

    public void render(Graphics2D g) {
        if (drawCoordinates) {
            drawLine(g, MathVideo.GRAY, 0.5, new Point2D.Double(MathVideo.getInstance().getWidth() * -0.5, 0), new Point2D.Double(MathVideo.getInstance().getWidth() * 0.5,  0));
            drawLine(g, MathVideo.GRAY, 0.5, new Point2D.Double(0, MathVideo.getInstance().getHeight() * -0.5), new Point2D.Double(0,  MathVideo.getInstance().getHeight() * 0.5));
        }
        for (Iterator<Runnable> iterator = renderModificationQueue.iterator(); iterator.hasNext();) {
            iterator.next().run();
            iterator.remove();
        }
        for (Renderable renderable : renderables) {
            renderable.render(g);
        }
    }

    public static void drawLine(Graphics2D graphics, Color color, double width, Point2D start, Point2D end) {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke((float) width));
        Point2D transformedStart = coordinateSpaceToUserSpace(start);
        Point2D transformedEnd = coordinateSpaceToUserSpace(end);
        graphics.draw(new Line2D.Double(
                transformedStart.getX(), transformedStart.getY(),
                transformedEnd.getX(), transformedEnd.getY()));
    }

    public static Point2D coordinateSpaceToUserSpace(Point2D coordSpace) {
        return coordinateSpaceToUserSpace(coordSpace, 1);
    }

    public static Point2D coordinateSpaceToUserSpace(Point2D coordSpace, int scale) {
        AffineTransform shifter = AffineTransform.getTranslateInstance(MathVideo.getInstance().getWidth() / 2.0, MathVideo.getInstance().getHeight() / 2.0);
        AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
        return shifter.transform(scaler.transform(flipper.transform(coordSpace, null), null), null);
    }
}
