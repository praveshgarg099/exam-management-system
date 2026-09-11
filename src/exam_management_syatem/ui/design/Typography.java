package exam_management_syatem.ui.design;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

public final class Typography {
    private Typography() {}

    private static final String FONT_FAMILY = getBestAvailableFont();

    // Typography Definitions
    public static final Font DISPLAY = new Font(FONT_FAMILY, Font.BOLD, 36);
    public static final Font H1 = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font H2 = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font H3 = new Font(FONT_FAMILY, Font.BOLD, 16);
    
    public static final Font BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BODY_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 13);
    
    public static final Font LABEL = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font BUTTON = new Font(FONT_FAMILY, Font.BOLD, 14);

    /**
     * Determines the best available safe font on the platform.
     */
    private static String getBestAvailableFont() {
        String[] preferredFonts = {"Segoe UI", "San Francisco", "Helvetica Neue", "Arial"};
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        
        for (String preferred : preferredFonts) {
            for (String available : availableFonts) {
                if (preferred.equalsIgnoreCase(available)) {
                    return available;
                }
            }
        }
        return "SansSerif"; // Ultimate fallback
    }
}
