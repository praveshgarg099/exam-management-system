package exam_management_syatem.ui.shell;

public interface NavigationController {
    /**
     * Navigates to the specified view ID.
     */
    void navigateTo(String viewId);
    
    /**
     * Returns the currently active view ID.
     */
    String getCurrentViewId();
    
    /**
     * Checks if a view ID exists and can be navigated to.
     */
    boolean canNavigateTo(String viewId);
    
    /**
     * Requests the application to handle a logout sequence safely.
     */
    void handleLogout();
}
