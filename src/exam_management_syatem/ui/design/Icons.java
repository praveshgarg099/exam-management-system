package exam_management_syatem.ui.design;

import exam_management_syatem.util.ImageLoader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Component;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

public final class Icons {
    private Icons() {}
    
    private static final Map<String, ImageIcon> iconCache = new HashMap<>();

    /**
     * Loads and optionally scales an icon. Uses internal caching to avoid reloading.
     */
    public static ImageIcon getIcon(String path, int size) {
        String key = path + "_" + size;
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        ImageIcon original = ImageLoader.loadImage(path);
        if (original != null) {
            Image img = original.getImage();
            Image scaledImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImg);
            iconCache.put(key, scaledIcon);
            return scaledIcon;
        }

        // Fallback transparent icon of requested size if not found
        ImageIcon empty = new ImageIcon(new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB));
        iconCache.put(key, empty);
        return empty;
    }

    /**
     * Standard centralized icons.
     */
    public static ImageIcon getDashboardIcon() { return getIcon("index admin.png", Dimensions.ICON_SIZE); }
    public static ImageIcon getStudentIcon() { return getIcon("index student.png", Dimensions.ICON_SIZE); }
    public static ImageIcon getCloseIcon() { return getIcon("Close.png", Dimensions.ICON_SIZE); }
}
