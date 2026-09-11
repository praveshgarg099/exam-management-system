package exam_management_syatem.ui.components;

import exam_management_syatem.ui.design.Colors;
import exam_management_syatem.ui.design.Dimensions;
import exam_management_syatem.ui.design.Spacing;
import exam_management_syatem.ui.design.Typography;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

public class AppTable extends JTable {

    public AppTable(TableModel dm) {
        super(dm);
        initComponent();
    }

    private void initComponent() {
        setRowHeight(Dimensions.TABLE_ROW_HEIGHT);
        setFont(Typography.BODY);
        setForeground(Colors.TEXT_PRIMARY);
        setBackground(Colors.SURFACE);
        setGridColor(Colors.BORDER);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(Colors.SURFACE_ELEVATED);
        setSelectionForeground(Colors.TEXT_PRIMARY);
        setFillsViewportHeight(true);

        JTableHeader header = getTableHeader();
        header.setFont(Typography.LABEL);
        header.setBackground(Colors.SURFACE_ELEVATED);
        header.setForeground(Colors.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(100, 40));
        header.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER));
        
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(javax.swing.JLabel.LEFT);
        
        // Custom cell renderer to add horizontal padding
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(javax.swing.BorderFactory.createEmptyBorder(0, Spacing.MD, 0, Spacing.MD));
                return c;
            }
        });
    }
}
