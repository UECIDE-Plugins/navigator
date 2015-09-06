package org.uecide.plugin;

import org.uecide.*;
import org.uecide.debug.*;
import org.uecide.editors.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.zip.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

import say.swing.*;

import org.uecide.Console;

import java.util.Timer;


public class Navigator extends Plugin implements MouseListener {
    public static HashMap<String, String> pluginInfo = null;
    public static void setInfo(HashMap<String, String>info) { pluginInfo = info; }
    public static String getInfo(String item) { return pluginInfo.get(item); }

    Context ctx;

    public Navigator(Editor e) { editor = e; }
    public Navigator(EditorBase e) { editorTab = e; }


    public void shootConsole()
    {
        shootImage(editor.getConsole());
    }

    public void shootImage(Component c) {
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        c.paint(im.getGraphics());
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        fc.setFileFilter(filter);
        int n = fc.showSaveDialog(editor);
        if (n == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(im, "PNG", fc.getSelectedFile());
            } catch (Exception e) {
            }
        }
    }

    public void shootEditor() {
        EditorBase sel = editor.getSelectedEditor();
        if (sel == null) {
            return;
        }
        shootImage(sel.getContentPane());
    }

    public void addToolbarButtons(JToolBar toolbar, int flags) {
    }

    public static PropertyFile getPreferencesTree() {
        return null;
    }

    public void populateMenu(JMenu menu, int flags) {
    }

    public void populateMenu(JPopupMenu menu, int flags) {
        if (flags == (Plugin.MENU_POPUP_CONSOLE | Plugin.MENU_MID)) {
            JMenuItem item = new JMenuItem("Screenshot");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    shootConsole();
                }
            });
            menu.add(item);
        }

        if (flags == (Plugin.MENU_POPUP_EDITOR | Plugin.MENU_MID)) {
            JMenuItem item = new JMenuItem("Screenshot");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    shootEditor();
                }
            });
            menu.add(item);
        }
    }

    public static String getPreferencesTitle() {
        return null;
    }

    public void populateContextMenu(JPopupMenu menu, int flags, DefaultMutableTreeNode node) {
    }

    public ImageIcon getFileIconOverlay(File f) { return null; }

    JPanel previewPanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {

            if (editor == null) {
                super.paintComponent(g);
                return;
            }

            EditorBase sel = editor.getSelectedEditor();
            if (sel == null) {
                super.paintComponent(g);
                return;
            }

            Component c = sel.getContentPane();

            if (c == null) {
                super.paintComponent(g);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = c.getWidth();
            int h = c.getHeight();

            if (w == 0) { // Split closed
                super.paintComponent(g);
                return;
            }

            Rectangle bounds = sel.getViewRect();

            BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            c.paint(im.getGraphics());

            Graphics2D workBuffer = (Graphics2D)im.getGraphics();
            workBuffer.setPaint(new Color(0, 0, 0, 0.1f));
            workBuffer.fillRect(0, 0, im.getWidth(), bounds.y);
            workBuffer.fillRect(0, bounds.y + bounds.height, im.getWidth(), im.getHeight() - (bounds.y + bounds.height));

            workBuffer.setPaint(new Color(1, 1, 1, 0.2f));
            workBuffer.fillRect(0, bounds.y, im.getWidth(), bounds.height);
            

            g2d.drawImage(im, 0, 0, getWidth(), getHeight(), null);
        }
    };

    Timer trigger = new Timer();

    public void addPanelsToTabs(JTabbedPane tabs, int flags) {
        if (flags == Plugin.TABS_SIDEBAR) {
            tabs.add(previewPanel, "Navigator");
            trigger.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    previewPanel.repaint();
                }
            }, 100, 100);

            previewPanel.addMouseListener(this);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        EditorBase sel = editor.getSelectedEditor();
        if (sel == null) {
            return;
        }
        String text = sel.getText();
        if (text == null || text.equals("")) {
            return;
        }


        Component c = sel.getContentPane();
        int mainHeight = c.getHeight();
        Rectangle viewRect = sel.getViewRect();

        float pct = (float)e.getY() / (float)previewPanel.getHeight();

        int clickPos = (int)((float)mainHeight * pct);

        int showPos = clickPos - (viewRect.height / 2);
        if (showPos < 0) {
            showPos = 0;
        }

        if (showPos + viewRect.height > mainHeight) {
            showPos = mainHeight - viewRect.height;
        }

        sel.setViewPosition(new Point(0, showPos));
    }
}

