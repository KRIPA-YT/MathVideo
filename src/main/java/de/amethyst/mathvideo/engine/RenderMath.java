package de.amethyst.mathvideo.engine;

import static java.lang.Math.*;

public class RenderMath {
    public static double lerp(double distance, double start, double stop) {
        return start + distance * (stop - start);
    }
    public static double cerp(double distance, double start, double stop) {
        double distance2 = (1-cos(distance*PI))/2;
        return(start*(1-distance2)+stop*distance2);
    }

    public static double invLerp(double value, double start, double stop) {
        return (value - start) / (stop - start);
    }

    public static float[] doubleToFloat(double[] array) {
        float[] farr = new float[array.length];

        for (int i = 0; i < array.length; i++) {
            farr[i] = (float) array[i];
        }
        return farr;
    }
}
