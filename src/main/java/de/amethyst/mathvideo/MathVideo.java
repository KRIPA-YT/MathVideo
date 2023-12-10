package de.amethyst.mathvideo;

import de.amethyst.mathvideo.engine.*;
import lombok.Getter;
import org.scilab.forge.jlatexmath.TeXFormulaParser;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.util.Collections;
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
    public static final double RESOLUTION = 1.0;

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

        LaTeX func = new LaTeX("f(x)=\\frac{x}{\\sin(x)}", GRAY, new Point2D.Double(mathVideo.getWidth() / 2.0 - 50, mathVideo.getHeight() / -2.0 + 250), 40, LaTeX.Alignment.RIGHT, true);

        intro(func);
        examples(func);
        fourCases(mathVideo);
        economy(mathVideo);
        end();
    }

    private static void intro(LaTeX func) throws InterruptedException {
        Graph intro = new MonoColorGraph(x -> x/sin(x), RED, 3, 25);
        func.animate(Duration.ofMillis(250));
        intro.animateWait(Duration.ofMillis(2500));
        Thread.sleep(2000);
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{\\sin(x)}{5x}");
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> sin(x)/x*5, YELLOW, 3, 100));
        Thread.sleep(2000);
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{tan(x)}{2}");
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(Math::tan, GREEN, 3, 100));
        Thread.sleep(2000);
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{\\tan(x)}{x}");
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> tan(x)/x, BLUE, 3, 100));
        Thread.sleep(2000);
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{2^x}{10}");
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> pow(2, x) / 10, YELLOW, 3, 100));
        Thread.sleep(2000);
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{1}{x}");
        intro.morphWait(Duration.ofMillis(2500), new MonoColorGraph(x -> 1/x, GREEN, 3, 100));
        Thread.sleep(2000);

        func.morph(Duration.ofMillis(500), "f(x)=\\frac{1}{2}x");
        intro.morph(Duration.ofMillis(2500), new MonoColorGraph(x -> x/2.0, RED, 3, 100));
        Thread.sleep(1250);
        func.animateDelete(Duration.ofMillis(250));
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
        Thread.sleep(5000);
        /*Tauchen wir ein in die Welt der mathematischen Unendlichkeit, während wir die Geheimnisse hinter dem Verhalten von ganzrationalen Funktionen entschlüsseln.
        Von ganzrationalen Funktionen, die sich dem positiven Unendlich annähern, bis hin zu jenen, die sich in den Abgründen des negativen Unendlich verlieren –
        wir werden die mathematischen Kurven erkunden, die unsere Vorstellungskraft herausfordern. Bereit für eine Reise durch die unendlichen Weiten der Mathematik?
        Dann lassen Sie uns gemeinsam in diese faszinierende Analyse eintauchen.*/

        intro.animateDelete(Duration.ofMillis(1000));
        parabola.animateDelete(Duration.ofMillis(1000));
        hyperbola.morph(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> pow(x/3, 3)));
        func.setLaTeX("f(x)=\\frac{1}{27}x^3");
        func.animate(Duration.ofMillis(250));

        title.animateDeleteWait(Duration.ofMillis(500));
        subtitle.animateDelete(Duration.ofMillis(500));
        credits.animateDeleteWait(Duration.ofMillis(500));
        Thread.sleep(9000);
        /*Schauen wir uns zuerst das Verhalten an, wenn x gegen Plus Unendlich geht. In diesem Fall nähert sich die Funktion immer mehr*/
        hyperbola.delete();
    }

    private static void examples(LaTeX func) throws InterruptedException {
        Graph hyperbola = new HeightCodedGraph(x -> pow(x/3, 3), Collections.singletonMap(0, BLUE), 3, 100);
        hyperbola.morphWait(Duration.ofMillis(1000), new HeightCodedGraph(x -> pow(x/3, 3), Map.ofEntries(
                Map.entry(-1, BLUE),
                Map.entry(1, RED)
        ), hyperbola.getWidth(), hyperbola.getScale()));
        Thread.sleep(9000);
        /*positiver Unendlichkeit. Und jetzt, wenn x gegen Minus Unendlich geht, nähern sich die die Funktionswerte negativer Unendlichkeit.*/
        func.morph(Duration.ofMillis(500), "f(x)={\\frac{1}{640}x^5+\\frac{3}{320}x^4-\\frac{11}{160}x^3-\\frac{27}{80}x^2+\\frac{1}{4}x+\\frac{4}{5}}");
        hyperbola.morphWait(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> (pow(x/2, 5) + 3 * pow(x/2, 4) - 11 * pow(x/2, 3) - 27 * pow(x/2, 2) + 10 * x/2 + 32) / 20.0));
        Thread.sleep(4000);
        /* Mathematisch ausgedrückt bedeutet das, wenn wir das Verhalten der Funktion f(x) für x-Werte gegen*/
        LaTeX limit = new LaTeX("\\begin{gather}x\\to+\\infty\\\\ f(x)\\to+\\infty\\end{gather}", RED, new Point2D.Double(0, 400), 75, LaTeX.Alignment.CENTER, true);
        limit.animate(Duration.ofMillis(1000));
        Thread.sleep(10000);
        /*+∞ betrachten, geht f(x) ebenfalls gegen Plus Unendlich.*/
        limit.animateDelete(Duration.ofMillis(1000));
        hyperbola.morphWait(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> (5 * pow(x, 4) - 2 * pow(x, 2) + 5) / 10));
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{5x^4-2x^2+5}{10}");
        Thread.sleep(6000 + 3000);
        /*Betrachten wir ein Beispiel mit einer ganzrationalen Funktion, zum Beispiel f(x) = 5x^4-2x^2+5. Wenn x gegen Plus Unendlich geht, schauen Sie sich an, wie sich die Funktion verhält*/
        hyperbola.morphWait(Duration.ofMillis(5000), hyperbola.clone().setFunction(x -> (5 * pow(x, 4) - 2 * pow(x, 2) + 5) / 10).setScale(1));
        Thread.sleep(5000);
        /*Der Wert von f(x) nähert sich positiver Unendlichkeit.*/
        hyperbola.morphWait(Duration.ofMillis(1000), hyperbola.clone().setFunction(x -> (5 * pow(x, 4) - 2 * pow(x, 2) + 5) / -10).setScale(100));
        func.morph(Duration.ofMillis(500), "f(x)=\\frac{-5x^4+2x^2-5}{10}");
        Thread.sleep(2000);
        /*Und wenn x gegen Minus Unendlich geht, sehen Sie, dass sich f(x) negativer Unendlichkeit nähert.*/
        func.animateDelete(Duration.ofMillis(250));
        hyperbola.animateDeleteWait(Duration.ofMillis(1000));
        Thread.sleep(1000);
    }

    private static void fourCases(MathVideo mathVideo) throws InterruptedException {
        AnimatableDeletable[] cases = drawFourCases(mathVideo);
        Thread.sleep(8000);
        deleteFourCases(cases);
        Thread.sleep(100);
        /*
        Für die 4 Fälle, die wir uns gleich anschauen, ist nur a_n*x^n relevant, da für die Ermittlung des Aussehens des Graphen nur der Grad und der Koeffizient des x'es beim Grad relevant ist.
         */
        LaTeX proof1 = new LaTeX("""
                \\begin{align}
                f(x)&=a_n \\cdot x^n + a_{n-1} \\cdot x^{n-1} + \\ldots + a_1 \\cdot x + a_0\\\\
                    &=x^n \\cdot (a_n + \\frac{a_{n-1}}{x} + \\ldots + \\frac{a_1}{x^{n-1}} + \\frac{a_0}{x^n})
                \\end{align}
                """, WHITE, new Point2D.Double(mathVideo.getWidth() / -2.0, mathVideo.getHeight() / 2.0), 50);
        proof1.animateWait(Duration.ofMillis(1000));
        Thread.sleep(7000);
        /*
        Wenn wir allgemein eine ganzrationale Funktion haben, können wir sie so umschreiben, sodass alles ein Faktor von x^n ist. Dies können wir ausnutzen,
         */
        LaTeX proof2 = new LaTeX("""
                \\begin{align}
                x \\to &\\pm\\infty\\\\
                &\\Rightarrow (a_n + \\frac{a_{n-1}}{x} + \\ldots + \\frac{a_1}{x^{n-1}} + \\frac{a_0}{x^n}) \\to 0\\\\
                &\\Rightarrow f(x) \\approx a_n \\cdot x^n
                \\end{align}
                """, WHITE, new Point2D.Double(mathVideo.getWidth() / -2.0, mathVideo.getHeight() / 2.0 - 190), 50);
        proof2.animateWait(Duration.ofMillis(1000));
        Thread.sleep(8500);
        /*
        um klarzumachen, dass bei kleinen x der Term gegen 0 geht und dieser daher keinen Effekt hat. Das heißt, dass dann f(x) ungefähr a_n*x^n ist.
         */
        proof1.animateDelete(Duration.ofMillis(1000));
        proof2.animateDeleteWait(Duration.ofMillis(1000));
        double xShift = mathVideo.getWidth() / 4.0;
        LaTeX pparabolaLimit = new LaTeX("""
                \\begin{gather}
                a > 0\\\\
                x \\to +\\infty \\quad f(x) \\to +\\infty\\\\
                x \\to -\\infty \\quad f(x) \\to +\\infty
                \\end{gather}
                """, WHITE, new Point2D.Double(-xShift, 100), 40, LaTeX.Alignment.CENTER);
        LaTeX nparabolaLimit = new LaTeX("""
                \\begin{gather}
                a < 0\\\\
                x \\to +\\infty \\quad f(x) \\to -\\infty\\\\
                x \\to -\\infty \\quad f(x) \\to -\\infty
                \\end{gather}
                """, WHITE, new Point2D.Double(xShift, 100), 40, LaTeX.Alignment.CENTER);
        LaTeX phyperbolaLimit = new LaTeX("""
                \\begin{gather}
                a > 0\\\\
                x \\to +\\infty \\quad f(x) \\to +\\infty\\\\
                x \\to -\\infty \\quad f(x) \\to -\\infty
                \\end{gather}
                """, WHITE, new Point2D.Double(-xShift, -100), 40, LaTeX.Alignment.CENTER);
        LaTeX nhyperbolaLimit = new LaTeX("""
                \\begin{gather}
                a < 0\\\\
                x \\to +\\infty \\quad f(x) \\to -\\infty\\\\
                x \\to -\\infty \\quad f(x) \\to +\\infty
                \\end{gather}
                """, WHITE, new Point2D.Double(xShift, -100), 40, LaTeX.Alignment.CENTER);
        cases = drawFourCases(mathVideo);
        pparabolaLimit.animate(Duration.ofMillis(250));
        nparabolaLimit.animateWait(Duration.ofMillis(250));
        Thread.sleep(4000);
        /*
        Ist der Grad gerade, so ähnelt die Funktion einer Parabel,
        gilt nur bei großen und kleinen Werten, andernfalls ist der Unterschied um den Ursprung herum zu groß:
        für a > 0 ist die Funktion nach oben geöffnet
        für a < 0 ist die Funktion nach unten geöffnet
         */
        phyperbolaLimit.animate(Duration.ofMillis(2500));
        nhyperbolaLimit.animateWait(Duration.ofMillis(2500));
        Thread.sleep(4000);
        /*
        Ist der Grad ungerade, so ähnelt die Funktion einer kubischen Parabel,
        gilt nur bei großen und kleinen Werten, andernfalls ist der Unterschied um den Ursprung herum zu groß:
        für a > 0 gilt für x -> +∞ gilt f(x) -> +∞, für x -> -∞ gilt f(x) -> -∞
        für a < 0 gilt für x -> +∞ gilt f(x) -> -∞, für x -> -∞ gilt f(x) -> +∞
         */
        deleteFourCases(cases);
        pparabolaLimit.animateDelete(Duration.ofMillis(1000));
        nparabolaLimit.animateDelete(Duration.ofMillis(1000));
        phyperbolaLimit.animateDelete(Duration.ofMillis(1000));
        nhyperbolaLimit.animateDeleteWait(Duration.ofMillis(1000));
    }

    private static AnimatableDeletable[] drawFourCases(MathVideo mathVideo) throws InterruptedException {
        double xShift = mathVideo.getWidth() / 4.0;
        double yShift = mathVideo.getHeight() / 4.0;
        Graph pparabola = new MonoColorGraph(x -> (pow((x + xShift) / 15, 2) + yShift), GREEN, 3);
        pparabola.setMaxX(0);
        pparabola.setMinY(0);
        Graph nparabola = new MonoColorGraph(x -> (-pow((x + xShift) / 15, 2) - yShift), RED, 3);
        nparabola.setMaxX(0);
        nparabola.setMaxY(0);
        Graph phyperbola = new MonoColorGraph(x -> (pow((x - xShift) / 40, 3) + yShift), GREEN, 3);
        phyperbola.setMinX(0);
        phyperbola.setMinY(0);
        Graph nhyperbola = new MonoColorGraph(x -> (-pow((x - xShift) / 40, 3) - yShift), RED, 3);
        LaTeX pparabolaLabel = new LaTeX("f(x)=x^2", WHITE, new Point2D.Double(-xShift, 2*yShift - 40), 40, LaTeX.Alignment.CENTER);
        LaTeX nparabolaLabel = new LaTeX("f(x)=-x^2", WHITE, new Point2D.Double(-xShift, -2*yShift + 40), 40, LaTeX.Alignment.CENTER);
        LaTeX phyperbolaLabel = new LaTeX("f(x)=x^3", WHITE, new Point2D.Double(xShift, 2*yShift - 40), 40, LaTeX.Alignment.CENTER);
        LaTeX nhyperbolaLabel = new LaTeX("f(x)=-x^3", WHITE, new Point2D.Double(xShift, -2*yShift + 40), 40, LaTeX.Alignment.CENTER);
        nhyperbola.setMinX(0);
        nhyperbola.setMaxY(0);
        phyperbola.animate(Duration.ofMillis(1000));
        nhyperbola.animate(Duration.ofMillis(1000));
        pparabola.animate(Duration.ofMillis(1000));
        nparabola.animate(Duration.ofMillis(1000));
        Thread.sleep(750);
        pparabolaLabel.animate(Duration.ofMillis(250));
        nparabolaLabel.animate(Duration.ofMillis(250));
        phyperbolaLabel.animate(Duration.ofMillis(250));
        nhyperbolaLabel.animate(Duration.ofMillis(250));
        Thread.sleep(250);
        return new AnimatableDeletable[]{pparabola, nparabola, phyperbola, nhyperbola, pparabolaLabel, nparabolaLabel, phyperbolaLabel, nhyperbolaLabel};
    }

    private static void deleteFourCases(AnimatableDeletable[] cases) throws InterruptedException {
        cases[0].animateDelete(Duration.ofMillis(1000));
        cases[1].animateDelete(Duration.ofMillis(1000));
        cases[2].animateDelete(Duration.ofMillis(1000));
        cases[3].animateDelete(Duration.ofMillis(1000));
        Thread.sleep(750);
        cases[4].animateDelete(Duration.ofMillis(250));
        cases[5].animateDelete(Duration.ofMillis(250));
        cases[6].animateDelete(Duration.ofMillis(250));
        cases[7].animateDelete(Duration.ofMillis(250));
        Thread.sleep(250);
    }

    private static void economy(MathVideo mathVideo) throws InterruptedException {
        LaTeX problem = new LaTeX("""
                \\textrm{Als einziger Anbieter eines bestimmten Produktes ist eine Firma ein Angebotsmonopolist.\\\\
                Bei der Herstellung ergibt sich durch Regression der folgende funktionale Zusammenhang\\\\
                zwischen Mengeneinheiten $x$ und Kosten $K(x)$, n\\ddot{a}mlich die Gesamtkostenfunktion:\\\\
                $K(x)=x^3-7x^2+135x-1150$}
                """, WHITE, new Point2D.Double(0, mathVideo.getHeight() / 2.0 - 150), 40, LaTeX.Alignment.CENTER);
        Graph k = new MonoColorGraph(x -> (pow(x, 3) - 7 * pow(x, 2) + 135 * x - 1150) / 250, RED, 3);
        k.setMinX(-100);
        k.setMaxX(100);
        LaTeX solution = new LaTeX("""
                \\begin{align}
                K(x)&=x^3-7x^2+135x-1150\\\\
                \\Rightarrow n&=3\\\\
                \\Rightarrow x &\\to +\\infty\\\\
                             K(x) &\\to +\\infty\\\\
                \\end{align}
                """, WHITE, new Point2D.Double(0, mathVideo.getHeight() / -2.0 + 150), 40, LaTeX.Alignment.CENTER);
        problem.animateWait(Duration.ofMillis(1000));
        solution.animateWait(Duration.ofMillis(1000));
        k.animateWait(Duration.ofMillis(1000));
        Thread.sleep(16000);
        /*
        Schließlich betrachten wir die praktische Anwendung dieser Konzepte. In wirtschaftlichen Modellen können Funktionen das Verhalten von Ressourcen oder Gewinnen beschreiben.
        Das Verständnis, wie diese Funktionen in extremen Situationen reagieren, kann uns helfen, langfristige Trends und Stabilität zu analysieren.
         */
        problem.animateDelete(Duration.ofMillis(1000));
        solution.animateDelete(Duration.ofMillis(1000));
        k.animateDeleteWait(Duration.ofMillis(1000));
    }

    private static void end() throws InterruptedException {
        Graph intro = new MonoColorGraph(x -> x/2.0, RED, 3, 100);
        Graph parabola = new MonoColorGraph(x -> pow(x/2, 2), GREEN, 3, 100);
        Graph hyperbola = new MonoColorGraph(x -> -pow(x/3, 3), BLUE, 3, 100);
        Text title = new Text(new Point2D.Double(0, 350), "Konvergenz zu +/- Unendlich", WHITE);
        Text subtitle = new Text(new Point2D.Double(0, 300), "bei ganzrationalen Funktionen", WHITE, 25);
        Text credits = new Text(new Point2D.Double(0, -350), "von Amy, Raphael, Ray und Sebastian", GRAY, 25);
        intro.animate(Duration.ofMillis(2500));
        parabola.animate(Duration.ofMillis(2500));
        hyperbola.animateWait(Duration.ofMillis(2500));
        title.animateWait(Duration.ofMillis(1000));
        subtitle.animate(Duration.ofMillis(750));
        credits.animateWait(Duration.ofMillis(750));
        /*Zusammenfassend kann man sagen, dass der grobe Verlauf des Graphen mit den oben genannten Fällen ermittelt werden kann.*/
    }
}
