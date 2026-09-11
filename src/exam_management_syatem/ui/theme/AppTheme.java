package exam_management_syatem.ui.theme;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Typography;
import javax.swing.UIManager;

public final class AppTheme {
    private AppTheme() {}

    /**
     * Applies standard global theme overrides for standard Swing components.
     * Note: Custom components like AppButton, AppCard handle their own styling,
     * but this ensures standard OptionPanes and basic components don't look completely disjointed.
     */
    public static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Global overrides for JOptionPane, standard Panels, etc.
            UIManager.put("Panel.background", Colors.BACKGROUND);
            UIManager.put("OptionPane.background", Colors.BACKGROUND);
            UIManager.put("OptionPane.messageForeground", Colors.TEXT_PRIMARY);
            UIManager.put("OptionPane.messageFont", Typography.BODY);
            UIManager.put("Label.font", Typography.BODY);
            UIManager.put("Label.foreground", Colors.TEXT_PRIMARY);
            
            UIManager.put("Button.font", Typography.BUTTON);
            UIManager.put("TextField.font", Typography.BODY);
            
            // Note: In an ideal full theme, we would replace BasicButtonUI, BasicTextFieldUI, etc.
            // But since we created custom components, we will rely on those for the primary layout.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
