package exam_management_syatem.ui.design;

import java.awt.Color;

public final class Colors {
    private Colors() {} // Prevent instantiation

    // Primary Brand Colors
    public static final Color PRIMARY = Color.decode("#2563EB");
    public static final Color PRIMARY_HOVER = Color.decode("#1D4ED8");
    public static final Color PRIMARY_PRESSED = Color.decode("#1E40AF");
    public static final Color PRIMARY_DISABLED = Color.decode("#93C5FD");

    // Surface and Backgrounds
    public static final Color BACKGROUND = Color.decode("#F8FAFC");
    public static final Color SURFACE = Color.decode("#FFFFFF");
    public static final Color SURFACE_ELEVATED = Color.decode("#F1F5F9");

    // Typography Colors
    public static final Color TEXT_PRIMARY = Color.decode("#111827");
    public static final Color TEXT_SECONDARY = Color.decode("#6B7280");
    public static final Color TEXT_MUTED = Color.decode("#9CA3AF");
    public static final Color TEXT_ON_PRIMARY = Color.decode("#FFFFFF");

    // Borders
    public static final Color BORDER = Color.decode("#E5E7EB");
    public static final Color BORDER_FOCUS = Color.decode("#3B82F6");

    // Semantic Status Colors
    public static final Color STATUS_SUCCESS = Color.decode("#10B981");
    public static final Color STATUS_WARNING = Color.decode("#F59E0B");
    public static final Color STATUS_ERROR = Color.decode("#EF4444");
    public static final Color STATUS_INFO = Color.decode("#3B82F6");
    
    // Status Backgrounds (light tints for badges)
    public static final Color STATUS_SUCCESS_BG = Color.decode("#D1FAE5");
    public static final Color STATUS_WARNING_BG = Color.decode("#FEF3C7");
    public static final Color STATUS_ERROR_BG = Color.decode("#FEE2E2");
    public static final Color STATUS_INFO_BG = Color.decode("#DBEAFE");
    
    // Status Text (darker shades for badges)
    public static final Color STATUS_SUCCESS_TEXT = Color.decode("#065F46");
    public static final Color STATUS_WARNING_TEXT = Color.decode("#92400E");
    public static final Color STATUS_ERROR_TEXT = Color.decode("#991B1B");
    public static final Color STATUS_INFO_TEXT = Color.decode("#1E40AF");
}
