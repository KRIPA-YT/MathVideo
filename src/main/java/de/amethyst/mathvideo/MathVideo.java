package de.amethyst.mathvideo;

import de.amethyst.mathvideo.engine.HeightCodedGraph;
import de.amethyst.mathvideo.engine.Renderer;
import lombok.Getter;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.*;

public class MathVideo extends JPanel {
    public static final Color BACK_GRAY = new Color(0x0E1A24);
    public static final Color GRAY = new Color(0x9EA2AA);
    public static final Color WHITE = new Color(0xFCFEFF);
    public static final Color RED = new Color(0xDF0E49);
    public static final Color YELLOW = new Color(0xEFD159);
    public static final Color GREEN = new Color(0x0ECE8D);
    public static final Color BLUE = new Color(0x2C9FD5);
    public static final double FRAMERATE = 60.0;
    public static final double RESOLUTION = 1;

    private static MathVideo self;
    @Getter
    private static Renderer renderer;

    public MathVideo() {
        super();
        self = this;

        // Render thread
        renderer = new Renderer();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, (int) (1000 / FRAMERATE));
    }

    @Override
    public void paint(Graphics graphics) {
        // Init + Background
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(BACK_GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        getRenderer().render(g);
    }

    public static MathVideo getInstance() {
        return self;
    }

    public static void main(String[] args) throws InterruptedException {
        // Create Window
        MathVideo mathVideo = new MathVideo();
        JFrame mathVideoFrame = new JFrame("Math Video");
        mathVideoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make visible
        mathVideoFrame.setContentPane(mathVideo);
        mathVideoFrame.setSize(1920, 1080);
        mathVideoFrame.setUndecorated(true);
        mathVideoFrame.setVisible(true);

        // Draw graphs
        Graph begin = new MonoColorGraph(x -> 0.0, RED, 100);
        begin.draw();
        Thread.sleep(5000);
        begin.delete();

        Graph intro = new MonoColorGraph(x -> x/sin(x), RED, 3, 25);
        intro.animateWait(Duration.ofMillis(2500));
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> sin(x)/x*5, YELLOW, 3, 100));
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(Math::tan, GREEN, 3, 100));
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> tan(x)/x, BLUE, 3, 100));
        intro.morph(Duration.ofMillis(2500), new MonoColorGraph(x -> x/2.0, RED, 3, 100));
        Thread.sleep(1250);

        Graph parabola = new MonoColorGraph(x -> pow(x/2, 2), GREEN, 3, 100);
        Graph hyperbola = new MonoColorGraph(x -> -pow(x/3, 3), BLUE, 3, 100);
        Text title = new Text(new Point2D.Double(0, 350), "Konvergenz zu +/- Unendlich", WHITE);
        Text subtitle = new Text(new Point2D.Double(0, 300), "bei ganzrationalen Funktionen", WHITE, 25);
        Text credits = new Text(new Point2D.Double(0, -350), "von Amy, Raphael, Ray und Sebastian", GRAY, 25);
        parabola.animate(Duration.ofMillis(2500));
        hyperbola.animateWait(Duration.ofMillis(2500));
        title.animateWait(Duration.ofMillis(1000));
        subtitle.animate(Duration.ofMillis(750));
        credits.animateWait(Duration.ofMillis(750));
        Thread.sleep(2500);

        intro.animateDelete(Duration.ofMillis(1000));
        parabola.animateDelete(Duration.ofMillis(1000));
        hyperbola.morph(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> pow(x/3, 3)));
        title.animateDeleteWait(Duration.ofMillis(500));
        subtitle.animateDelete(Duration.ofMillis(500));
        credits.animateDeleteWait(Duration.ofMillis(500));

        hyperbola.morphWait(Duration.ofMillis(1000), new HeightCodedGraph(x -> pow(x/3, 3), Map.ofEntries(
                Map.entry(-1, BLUE),
                Map.entry(1, RED)
        ), hyperbola.getWidth(), hyperbola.getScale()));
        hyperbola.morphWait(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> -(pow(x/3, 4) + 3 * pow(x/3, 3) + pow(x/3, 2) + x/3 + 3)));
        hyperbola.morphWait(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> (pow(x/2, 5) + 3 * pow(x/2, 4) - 11 * pow(x/2, 3) - 27 * pow(x/2, 2) + 10 * x/2 + 32) / 20.0));
    }
}
