package de.amethyst.mathvideo;

public class RenderMath {
    public static double lerp(double distance, double start, double stop) {
        return start + distance * (stop - start);
    }
}
