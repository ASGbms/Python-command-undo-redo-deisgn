// https://stackoverflow.com/questions/33728104/how-do-i-make-java-jtables-cell-editors-and-undo-work-together-w-o-creating-ext

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class FixedField {

    public static void main(String[] args) {
        new FixedField();
    }

    public FixedField() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public static class UndoableEditHandler implements UndoableEditListener {

        private static final int MASK
                = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        private UndoManager undoManager = new UndoManager();

        private boolean canUndo = true;

        public UndoableEditHandler(JTextField field) {
            Document doc = field.getDocument();
            doc.addUndoableEditListener(this);
            field.getActionMap().put("Undo", new AbstractAction("Undo") {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undoManager.canUndo()) {
                            undoManager.undo();
                        }
                    } catch (CannotUndoException e) {
                        System.out.println(e);
                    }
                }
            });
            field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK), "Undo");
            field.getActionMap().put("Redo", new AbstractAction("Redo") {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (undoManager.canRedo()) {
                            undoManager.redo();
                        }
                    } catch (CannotRedoException e) {
                        System.out.println(e);
                    }
                }
            });
            field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, MASK), "Redo");
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            if (canUndo()) {
                undoManager.addEdit(e.getEdit());
            }
        }

        public void setCanUndo(boolean canUndo) {
            this.canUndo = canUndo;
        }

        public boolean canUndo() {
            return canUndo;
        }

    }

    public class TestPane extends JPanel {

        public TestPane() {
            JTextField field = new JTextField(10);
            UndoableEditHandler handler = new UndoableEditHandler(field);

            handler.setCanUndo(false);
            field.setText("Help");
            handler.setCanUndo(true);
            add(field);
        }

    }

}

public static class MyCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextField editor;
    private UndoableEditHandler undoableEditHandler;

    public MyCellEditor(JTextField editor) {
        this.editor = editor;
        undoableEditHandler = new UndoableEditHandler(editor);
        editor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        undoableEditHandler.setCanUndo(false);
        editor.setText(value == null ? null : value.toString());
        undoableEditHandler.setCanUndo(true);
        return editor;
    }

}