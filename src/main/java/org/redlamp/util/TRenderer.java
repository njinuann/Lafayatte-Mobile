package org.redlamp.util;

import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Pecherk
 */
public class TRenderer extends DefaultTreeCellRenderer
{
    Icon starIcon = new ImageIcon(getClass().getResource("/images/star.png"));
    Icon listIcon = new ImageIcon(getClass().getResource("/images/list.png"));
    Icon baloonIcon = new ImageIcon(getClass().getResource("/images/baloon.png"));
    Icon crossIcon = new ImageIcon(getClass().getResource("/images/cross.png"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        setFont(new Font(getFont().getName(), (node.isLeaf() ? Font.PLAIN : Font.BOLD), getFont().getSize()));
        setIcon(node.isRoot() ? starIcon  : ((node.isLeaf() ? crossIcon : listIcon)));
        return this;
    }
}
