/*
 * Functions for color mixing and comparing.
 *
 *
 * @author Anna Kruglaia
 */

package twitterbot.sim;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class ColorBlender {
    
      
    //making a Color object out of hex code
    public static Color hex2Rgb(String colorStr) {
        return new Color(
            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
    
    
    //mixing two colors, color addition, custom ratio
    //ratio 0.25 means 25% of the first color, 75% of the second
    //ratio 0.90 means 90% of the first color, 10% of the second
    //the result is the same as mixing color layers in photoshop
    public static Color mixAddPlus (Color c1, Color c2, double ratio) {
        if (ratio < 0 || ratio > 1) ratio = 0.5;
        
        double r, g, b;
        r = (c1.getRed() * (1.0 - ratio) + c2.getRed() * ratio);
        g = (c1.getGreen() * (1.0 - ratio) + c2.getGreen() * ratio);
        b = (c1.getBlue() * (1.0 - ratio) + c2.getBlue() * ratio);
        
        return new Color ( 
            (int) r,
            (int) g,
            (int) b
            );
    }
    public static String mixAddPlus (String c1, String c2, double ratio) {
        Color color1 = hex2Rgb(c1);
        Color color2 = hex2Rgb(c2);
        Color colorMix = mixAddPlus(color1, color2, ratio);
        
        int r = colorMix.getRed();
        int g = colorMix.getGreen();
        int b = colorMix.getBlue();
       
        return String.format("#%02x%02x%02x", r, g, b);
        
    }
    
    //this function compares HSB values of two colors and returns a numeric value,
    //with current coefficients if the result <= 5, two colors are similar enough
    //for human perception to call them the same name
    public static int matchColor (Color c1, Color c2) {
        
        //convering RGB to HSB 
        float[] hsv1 = new float[3];
        float[] hsv2 = new float[3];
        Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), hsv1);
        Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsv2);
        
        double H1, H2, S1, S2, V1, V2;
        H1 = hsv1[0]; S1 = hsv1[1]; V1 = hsv1[2];
        H2 = hsv2[0]; S2 = hsv2[1]; V2 = hsv2[2];
        
        double dH, dS, dV, hueDiff;
        hueDiff = Math.min(Math.abs(H1 - H2), Math.abs(1.0 - Math.max(H1, H2) + Math.min(H1, H2)));
        dH = Math.abs(hueDiff);
        dS = Math.abs(S1 - S2);
        dV = Math.abs(V1 - V2);  
        
        double diff = (0.75*dH + 0.25*dS + 0.75*dV);   
        
        return (int) (diff * 100);
    }
    
    public static int matchColor (String c1, String c2) {
        Color color1 = hex2Rgb(c1);
        Color color2 = hex2Rgb(c2);
        return matchColor(color1, color2);
    }
}
