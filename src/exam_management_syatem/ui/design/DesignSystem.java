package exam_management_syatem.ui.design;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Insets;

public final class DesignSystem {
    private DesignSystem() {}

    /**
     * Creates an empty border with standard padding.
     */
    public static Border createPadding(int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }
    
    public static Border createPadding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /**
     * Creates a standard subtle border for cards and panels.
     */
    public static Border createStandardBorder() {
        return BorderFactory.createLineBorder(Colors.BORDER, 1);
    }

    /**
     * Applies standard surface styling to a component.
     */
    public static void applySurfaceStyle(JComponent comp) {
        comp.setBackground(Colors.SURFACE);
        comp.setOpaque(true);
    }
    
    /**
     * Applies elevated surface styling.
     */
    public static void applyElevatedSurfaceStyle(JComponent comp) {
        comp.setBackground(Colors.SURFACE_ELEVATED);
        comp.setOpaque(true);
    }
}
