/*
 */

package twitterbot.sim;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


/**
 * @author Anna Kruglaia
 */

public class ColornameCreator {
 
    private static class ReadyMade {
        public String firstGram;
        public String secondGram;
        public String firstRgb;
        public String secondRgb;
        public int webFrequency; 
        
        ReadyMade (String[] gram, int freq) {
            firstGram = gram[0];
            secondGram = gram[1];            
            webFrequency = freq;
        }   
        ReadyMade (String gram1, String gram2, String rgb1, String rgb2) {
            firstGram = gram1;
            secondGram = gram2;
            firstRgb = rgb1;
            secondRgb = rgb2;
        } 
    }
    private static class ColorGram {
        public String stereotype;
        public String color;
        public String category;
        public int liking;        
        
        ColorGram (String[] gram) {
            stereotype = gram[0];
            color = gram[1];
            liking = Integer.parseInt(gram[2]);
            category = gram[3];         
        }           
    }
    private static class ColorName {
        String firstGram;
        String secondGram;
        String colorCode; 
        
        String colorType;
        String firstMeaning;
        String secondMeaning;        
        double mixPercent;
        int matching;
        int liking;
        
        ColorName(String a, String b, String code, double mix) {
            firstGram = a;
            secondGram = b;
           
            mixPercent = mix;
            colorCode = code;
            
            colorType = colorSpectrum(colorCode);                       
        }
        
        //linguistic rules
        public String getName () {
            if (mixPercent < 0.25) 
                return firstGram + "-shaded " + secondGram;
            if (mixPercent >= 0.25 && mixPercent < 0.45)
                return secondGram + " " + firstGram;
            if (mixPercent >= 0.45 && mixPercent < 0.55)
                return firstGram + " and " + secondGram;
            if (mixPercent >= 0.55 && mixPercent < 0.75)
                return firstGram + " " + secondGram;
            else //if (mixPercent >= 0.75)
                return secondGram + " with a tinge of " + firstGram;
        }
    }
    
    public static HashMap<String, ColorGram> colorMap;
    public static ArrayList<ReadyMade> readyMades;    
    public static HashMap<ReadyMade, String> readyMadeRgb50;    
    public static HashMap<String, String> rgb2Url;
    
    public static void loadUrlMap () throws FileNotFoundException {
        
        rgb2Url = new HashMap<>(1000);
        Scanner sc = new Scanner(new FileReader("urls.txt"));
        
        while (sc.hasNextLine()) {
            if (!sc.hasNext()) break; 
            String colorCode = sc.next();
            colorCode = "#" + colorCode.substring(2);
            String url = sc.next();
            rgb2Url.put(colorCode, url);
        }
//       TEST
//        int i = 0;        
//        for (String key : rgb2Url.keySet()) {
//            System.out.println(++i + " " + key + " " + rgb2Url.get(key));
//            if (i==20) break;
//        }        
    } 
    public static void loadBigrams () throws FileNotFoundException {
        
        readyMades = new ArrayList<>(500);         
        Scanner sc = new Scanner(new FileReader("bigrams.txt"));
        
        
        while (sc.hasNextLine()){
            if (!sc.hasNext()) break; 
            String[] gram = new String[2];
            int freq;
            
            gram[0] = sc.next().toLowerCase();
            gram[1] = sc.next().toLowerCase();
            freq = Integer.parseInt(sc.next());
            ReadyMade wordPair = new ReadyMade(gram, freq);
            readyMades.add(wordPair); 
        }
        
        
//       TEST
//        int i = 0;        
//        for (ColorGram key : readyMades) {
//            System.out.println(++i + " " + key.toString());
//            if (i==20) break;
//        }
    }    
    public static void loadColorMap () throws FileNotFoundException {
        
        colorMap = new HashMap<>(500);         
        Scanner sc = new Scanner(new FileReader("colorMap_extended.txt"));
                
        while (sc.hasNextLine()){            
            if (!sc.hasNext()) break;
            
            String[] s = new String[4];
            s[0] = sc.next();
            s[1] = sc.next();
            String colorCode = sc.next();
            s[2] = sc.next();
            s[3] = sc.next();
            
            ColorGram gram = new ColorGram(s);
            colorMap.put(colorCode, gram);              
            //System.out.println(gram[0] + " " + gram[1]);
        }
        
    }   
    
    //is there a color stereotype, mapped with this word?
    public static boolean findStereotype(String name) {
        for (String key : colorMap.keySet()) {
            if (name.equals(colorMap.get(key).stereotype)) {
//                System.out.println("found!! key: " + key + " value: " + colorMap.get(key)[0] 
//                        + " " + colorMap.get(key)[1]);
                return true;
            }  
        }
        return false;
    }
    
    //find all color stereotypes mapped with this word.
    //return a list of pairs RGB:generalColorName
    public static ArrayList getStereotype(String name) {
        ArrayList<String[]> colors = new ArrayList<>();
        
        for (String colorCode : colorMap.keySet()) {
            if (name.equals(colorMap.get(colorCode).stereotype)) {
                //pairs color code and the general name (red, orange, etc.)
                String[] s = new String[2];
                s[0] = colorCode;
                s[1] = colorMap.get(colorCode).color;
                colors.add(s);                        
            }  
        }
        return colors;
    }
    
    //function to choose one color code if there are many different colors
    //associated with the same color stereotype name (apple green, apple red)
    public static String[] pairColors (ArrayList<String[]> st1, ArrayList<String[]> st2) {
        String[] result = new String[2];
        
        //single color names, nothing to do
        if (st1.size() == 1 && st2.size() == 1) {
            result[0] = st1.get(0)[0];
            result[1] = st2.get(0)[0];     
            return result;
        }
        
        //if the second name has multiple color options
        if (st1.size() == 1) {
            result[0] = st1.get(0)[0];
            for (String[] st21 : st2) {
                if (st1.get(0)[1].equals(st21[1])) {
                    result[1] = st21[0];
                    return result;
                }                
            }
         //otherwise let's mix the least different colors
        int k = 0, diff, minD = 200;
        for (int i = 0; i < st2.size(); i++) { 
            diff = ColorBlender.matchColor(st1.get(0)[0], st2.get(i)[0]);
            if (diff < minD) { 
                minD = diff;
                k = i;
            }                
        }            
        result[1] = st2.get(k)[0];
        return result;
        }
        
        //riverse situation: if the first name has multiple color options
        if (st2.size() == 1) {
            result[1] = st2.get(0)[0];
            for (String[] st21 : st1) {
                if (st2.get(0)[1].equals(st21[1])) {
                    result[0] = st21[0];
                    return result;
                }                
            }
            //otherwise let's mix the least different colors
            int k = 0, diff, minD = 200;
            for (int i = 0; i < st1.size(); i++) { 
                diff = ColorBlender.matchColor(st2.get(0)[0], st1.get(i)[0]);
                if (diff < minD) { 
                    minD = diff;
                    k = i;
                }                
            }            
            result[0] = st1.get(k)[0];
            return result;
        }
        
        //both names have multiple options, same logic as before
        for (String[] str1 : st1) {
            for (String[] str2 : st2)
                if (str1[1].equals(str2[1])) {
                    result[0] = str1[0];
                    result[1] = str2[0];
                    return result;
                }                
        }
        int k = 0, t = 0, diff, minD = 200;
        for (int i = 0; i < st1.size(); i++) { 
            for (int j = 0; j < st2.size(); j++) { 
                diff = ColorBlender.matchColor(st1.get(i)[0], st2.get(j)[0]);
                if (diff < minD) { 
                    minD = diff;
                    k = i;
                    t = j;
                }                
            }
        }
        result[0] = st1.get(k)[0];
        result[1] = st2.get(t)[0];
        return result;
    }    
    
    //mapping the readymades with RGB (I use 50:50 mix as a starting reference point)
    public static void createColorbase () {        
        
        readyMadeRgb50 = new HashMap<>(500);
        
        //go through all readymades, check if we can use them
        for (ReadyMade key : readyMades) {
            
            if (findStereotype(key.firstGram) && findStereotype(key.secondGram)
                    && key.webFrequency < 500 && !key.firstGram.equals(key.secondGram)) 
            {
                String[] pair = pairColors(getStereotype(key.firstGram),
                                            getStereotype(key.secondGram));
               
                //make a new object with mix 50:50 and add it to the map RM:RGB
                String rgb = ColorBlender.mixAddPlus(pair[0], pair[1], 0.5);
                ReadyMade aColor = new ReadyMade(key.firstGram, key.secondGram,
                                                    pair[0], pair[1]);        
                
                readyMadeRgb50.put(aColor, rgb);            
            }
        }
    }

    //find all possible names for a RGB
    public static ArrayList<ColorName> possibleNames (String colorCode) {
        ArrayList<ColorName> possibleNames = new ArrayList<>();
        ColorName aColor;
        
        for (ReadyMade key : readyMadeRgb50.keySet()) {
            String mapCode = readyMadeRgb50.get(key);
            
            int diff = ColorBlender.matchColor(mapCode, colorCode);  
            
            
            if (diff <= 15) { //close enough to what we want, so try to variate the mix     
                int bestMatch = diff;
                String bestMix = mapCode;
                double bestRatio = 0.5;                
                String newMix;
                for (double i = 0.20; i <= 0.80; i += 0.10) {
                    newMix = ColorBlender.mixAddPlus(key.firstRgb, key.secondRgb, i);
                    int match = ColorBlender.matchColor(colorCode, newMix);
                    if (match < bestMatch) {
                        bestMatch = match;
                        bestRatio = i;
                        bestMix = newMix;
                    }
                } 
                if (bestMatch <= 5) {  //good enough match!
                    aColor = new ColorName(key.firstGram, key.secondGram, bestMix, bestRatio);
                    aColor.colorType = colorSpectrum(bestMix);
                    aColor.firstMeaning = colorMap.get(key.firstRgb).category;
                    aColor.secondMeaning = colorMap.get(key.secondRgb).category;
                    aColor.matching = bestMatch;
                    aColor.liking = evaluateName(key.firstGram, key.secondGram);
                    possibleNames.add(aColor);
                }                            
            }               
        }
        return possibleNames;        
    }
    
    //define how much this bot likes the name pair according to the 
    //initial colorMap liking parameter and category compatibility
    public static int evaluateName (String a, String b) {
        int like1 = 0, like2 = 0;
        String cat1 = "", cat2 = "";
        
        //iterate through the colorMap and look up the parameters
        for (Map.Entry<String, ColorGram> entry : colorMap.entrySet()) {            
            if (a.equals(entry.getValue().stereotype)) {
                like1 = entry.getValue().liking;
                cat1 = entry.getValue().category;
            }
            else if (b.equals(entry.getValue().stereotype)) {
                like2 = entry.getValue().liking;
                cat2 = entry.getValue().category;
            }
        }     
        //check category compatibility, 0 or 1
        int cat = matchCategories(cat1, cat2);
        
        //like! Like values are from 0 (absolutely not!) to 5 (awesome!)
        //resulting in maximum of 25 (super awesome!)
        int like = like1 * like2;
        
        //resulting in maximum 30
        return like + cat*5;
    }
    
    //checking category compatibility 
    public static int matchCategories (String cat1, String cat2) {
        //magic table
        int[][] table = {{0, 0, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                         {0, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1},
                         {1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1},
                         {1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1},
                         {1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1},
                         {1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1},
                         {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                         {1, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1},
                         {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
                         {0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                         {1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0}};
        String[] categoryList = {"alco", "animals", "body", "chem", "color",
                                 "food", "material", "nature", "tech", 
                                 "things", "unusial"};
        int in1 = 0, in2 = 0;
        for (int i = 0; i < categoryList.length; i++) {
            if (cat1.equals(categoryList[i])) in1 = i;
            if (cat2.equals(categoryList[i])) in2 = i;
        }
        //yes, in2 goes first, it just happened so when I made the compatibility table
        return table[in2][in1];       
        
    }
    
    //how much the bot likes a color (it likes bright saturated colors,
    //and a little more - the red part of the spectrum)
    public static int evaluateColor (String colorCode) {
        int liking = 0;
        
        String spec = colorSpectrum(colorCode);
        if (spec.equals("red") || spec.equals("orange")) {
            liking += 3;
        }
        else if (spec.equals("white") || spec.equals("black") || spec.equals("grey")) {
            liking -= 5;
        }
        
        Color color = ColorBlender.hex2Rgb(colorCode);
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
        hsv[0] *= 360; 
        hsv[1] *= 100; 
        hsv[2] *= 100; 
        
        //the brighter the better.
        if (hsv[2] >= 70) {
            liking += hsv[2] - 60;
        }
        //saturation is great, we like it!
        if (hsv[1] > 70) liking += (hsv[1] - 70) * 2;
        

        return liking;
    }
    
    //searching for the best name, if we can find it
    public static ColorName findBestName (String colorCode) {
        
        ArrayList<ColorName> possibleNames = possibleNames(colorCode);
        if (possibleNames.isEmpty()) return null; 
        
        //now we should have a list of names, which match the given color.
        //we know how much the bot likes the name itself, how good is the 
        //color match and what are the semantic categories of both word compounds.
                
        //return the best option
        int bestValue = 0;
        int i = 0;
        for (ColorName name : possibleNames) {
            double mix = name.mixPercent;
            int mixPoints = 0;
            //best mixes are like this, from my observations
            if ((mix > 0.25 && mix < 0.45) ||
                    (mix > 0.55 && mix < 0.75)) mixPoints = 2;
            if (mix >= 0.45 && mix <= 0.55) mixPoints = 1;
            int value = (5 - name.matching)*2 + name.liking + mixPoints*4;
            if (bestValue > value) { 
                bestValue = value;
                i = possibleNames.indexOf(name);
            }
            
        }
        return possibleNames.get(i);
    }
     //color categorization
    public static String colorSpectrum (String colorStr) {
        Color c = ColorBlender.hex2Rgb(colorStr);
        //convering RGB to HSB 
        float[] hsv = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);
        hsv[0] = hsv[0]*360; 
        hsv[1] = hsv[1]*100; 
        hsv[2] = hsv[2]*100; 
        
        if (hsv[2] <= 5) return "black";
        if ((hsv[1] <= 12) && (hsv[2] <= 50) ||
                (hsv[1] <= 6) && (hsv[2] <= 85)) return "grey";
        if ((hsv[2] >= 85) && (hsv[1] <= 7)) return "white";
            
        if (hsv[0] <= 10 || hsv[0] >= 347) return "red";
        if (hsv[0] >= 246 && hsv[0] <= 346) return "violet";
        if (hsv[0] >= 215 && hsv[0] <= 245) return "blue";
        if (hsv[0] >= 152 && hsv[0] <= 214) return "lightblue";
        if (hsv[0] >= 79 && hsv[0] <= 151) return "green";
        if (hsv[0] >= 53 && hsv[0] <= 78) return "yellow";
        else return "orange";    
                
    }
 
    public static String makeStatus (String colorTwit) {
        
        String colorCode = "#" + colorTwit.substring(2, 8);
        String url = colorTwit.substring(9);
        
        ColorName name = findBestName(colorCode);       
        
        if (name != null) {
            int colorLiking = evaluateColor(colorCode);
            
            //not a good option, don't post
            if (colorLiking < 40 && name.liking < 15) return null;   
            
            String framing;
            String extraFraming;
            
            //first framing
            if (name.mixPercent == 0.5) framing = "Perfect mix of "; 
            else if (name.secondMeaning.equals("alco")) framing = "This smells like ";
            else if (name.firstMeaning.equals("food") && name.secondMeaning.equals("food")
                    && name.liking >= 20) framing = "As delicious as ";
            else if (name.matching == 0) framing = "This looks just like ";
            else if (name.matching == 1) framing = "I would call this ";
            else if (name.matching == 3) framing = "This is almost like ";
            else if (name.matching == 4) framing = "It reminds me of ";
            else framing = "";
            
            //additional framing
            if (name.secondMeaning.equals("alco")) {
                if (name.firstMeaning.equals("food") ||
                    name.firstMeaning.equals("alco")) extraFraming = "I think I will have a fun evening. ";
                else extraFraming = "I am not sure about this, but why not? "; }
            else if (name.firstMeaning.equals("food") && name.secondMeaning.equals("food")) {
                if (colorLiking > 70) extraFraming = "Omnomnom! ^___^ ";
                else if (colorLiking > 60) extraFraming = "Who is hungry? I am starving. ";
                else if (colorLiking > 40) extraFraming = "Food time for me. ";
                else if (name.liking > 20) extraFraming = "It doesn't look very appealing, but I would eat it anyway. ";
                else extraFraming = "Food is good. "; }
            else if (colorLiking > 60) {
                if (colorLiking > 80 && name.liking >= 25) extraFraming = "Awww, isn't it absolutely awesome?! ";
                else if (colorLiking > 70 && name.liking > 25) extraFraming = "Totally awesome! ";
                else if (name.liking >= 20) extraFraming = "A nice name for a nice color. ";
                else if (name.liking > 12) extraFraming = "Awesome color, but I am not sure about the name. ";
                else if (colorLiking > 80) return "This color is so awesome, that I have no words. ";
                else if (colorLiking > 70) return "I like the color, but cannot think of a good name.. ";
                else return "It looks nice. But what should I call it?.. ";
            }    
            else if (name.firstMeaning.equals("nature") && name.secondMeaning.equals("nature")) {
                if (name.liking > 26) extraFraming = "I feel inspired! ";
                else if (name.liking > 22) extraFraming = "I want a vacation. ";
                else if (name.liking > 15) extraFraming = "I am going outside. ";
                else extraFraming = "I need some fresh air. ";
            }
            else if (colorLiking >= 40) {
                if (name.liking > 20) extraFraming = "I like it! ";
                else if (name.liking > 16) extraFraming = "It's quite nice. ";
                else if (name.liking > 12) extraFraming = "Not that bad. ";
                else return null;
            }
            else {
                if (colorLiking <=20 && name.liking > 25) 
                    return "I don't really like how it looks, but it's " +
                            name.getName() + " and it's awesome";
                else if (name.liking > 24) extraFraming = "It is nice enough after all. ";
                else return null;
            } 
            
            return framing + name.getName() + ". " + extraFraming;
        }
        
        return null;               
        
    }
    
    
//
//    public static void main (String[] args) throws FileNotFoundException {
//        loadColorMap();
//        loadBigrams();
//        createColorbase();        
//        loadUrlMap();
//
////========TEST CODE=============================================================
////        
//        
////        ColorBlender.matchColor("#fe47e3", "#fb6767");
////        findBestName("#03ae53");
////        evaluateColor("#365f25");
////        
////        int i = 0;        
////        for (String key : rgb2Url.keySet()) {
////            ColorName colorName = findBestName(key);
////       
////            if (colorName != null) {
////                if (i>80){
////                System.out.println("I think this color is worth " + evaluateColor(key));
////                
////                System.out.println(colorName.getName() + " - " + rgb2Url.get(key));
////                System.out.println("color code: "+ colorName.colorCode);
////                System.out.println("color meaning: "+ colorName.firstMeaning + " and " + colorName.secondMeaning);
////                System.out.println("liking: "+ colorName.liking);
////                System.out.println("matching: "+ colorName.matching);
////                System.out.println("mix percent: "+ colorName.mixPercent);
////                System.out.println("color spectrum: " + colorName.colorType);
////                }
////                i++;
////                if (i == 100) break;
////                
////            }
////        
////        }
//        
//        
////========TEST CODE=============================================================
////        
////        String test1 = "#f9871a";
////        String test2 = "#f95a1a";
////        
////        String[] mix = new String[9];
////        for (int i = 0; i < 9; i++) {
////            mix[i] = ColorBlender.mixAddPlus(test1, test2, 0.1*(i+1));            
////        }
////        for (int i = 0; i < 9; i++) {
////            System.out.println("mix ratio " + 0.1*(i+1) + " - " + mix[i]);
////            if (i != 0 && i != 8)
////            System.out.println("Color distance: " + 
////                    ColorBlender.matchColor(test1, mix[i]) + " and " +
////                    ColorBlender.matchColor(test2, mix[i]) +
////                    "; with prev: " + ColorBlender.matchColor(mix[i], mix[i-1]) + 
////                    " with next: " + ColorBlender.matchColor(mix[i], mix[i+1]));
////            
////            
////        } 
////        
////        System.out.println("Colors used: ");
////        System.out.println(test1 + " and " + test2);
////        System.out.println("Color distance: " + ColorBlender.matchColor(test1, test2));
//
////==============================================================================
//        
////        findStereotype("apple");
////        
////        Color testColor1 = hex2Rgb ("#ff0000");
////        Color testColor2 = hex2Rgb ("#00ba00");
////        
////        Color testColor = mixAddPlus(testColor1, testColor2, 0.25);
////        
////        System.out.println(matchColor(testColor1, testColor2));
////        
////        int r = testColor.getRed();
////        int g = testColor.getGreen();
////        int b = testColor.getBlue();
////        
////        //transforming int color values into hex color code
////        String hex = String.format("#%02x%02x%02x", r, g, b);
////       
////        System.out.println (hex);        
//    }
//
}
