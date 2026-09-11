package exam_management_syatem.ui.shell;

import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class ViewRegistry {
    private final Map<String, JPanel> views = new HashMap<>();
    private final Map<String, NavigationItem> navItems = new HashMap<>();
    
    public void registerView(NavigationItem item, JPanel viewPanel) {
        navItems.put(item.getId(), item);
        views.put(item.getId(), viewPanel);
    }
    
    public JPanel getView(String id) {
        return views.get(id);
    }
    
    public NavigationItem getNavItem(String id) {
        return navItems.get(id);
    }
    
    public boolean hasView(String id) {
        return views.containsKey(id);
    }
    
    public Iterable<NavigationItem> getAllNavItems() {
        return navItems.values();
    }
}
