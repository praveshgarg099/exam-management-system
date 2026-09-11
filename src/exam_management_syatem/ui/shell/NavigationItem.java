package exam_management_syatem.ui.shell;

import javax.swing.ImageIcon;

public class NavigationItem {
    private final String id;
    private final String label;
    private final ImageIcon icon;

    public NavigationItem(String id, String label, ImageIcon icon) {
        this.id = id;
        this.label = label;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}
