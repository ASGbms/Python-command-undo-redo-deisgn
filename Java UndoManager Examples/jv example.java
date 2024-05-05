// https://stackoverflow.com/questions/22960849/undo-in-jtable-in-swing

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.TabExpander;
import javax.swing.undo.*;


public class UndoTable
{
    public static void main(String[] args)
    {
        Object data[][] = {
            {"AMZN", "Amazon", 41.28, "BUY"},
            {"EBAY", "eBay", 41.57, "BUY"},
            {"GOOG", "Google", 388.33, "SELL"},
            {"MSFT", "Microsoft", 26.56, "SELL"},
            {"NOK", "Nokia Corp", 17.13, "BUY"},
            {"ORCL", "Oracle Corp.", 12.52, "BUY"},
            {"SUNW", "Sun Microsystems", 3.86, "BUY"},
            {"TWX",  "Time Warner", 17.66, "SELL"},
            {"VOD",  "Vodafone Group", 26.02, "SELL"},
            {"YHOO", "Yahoo!", 37.69, "BUY"}
        };
        String columns[] = {"Symbol", "Name", "Price", "Guidance"};

        final JvUndoableTableModel tableModel = new JvUndoableTableModel(data, columns);
        final JTable table = new JTable(tableModel);
        JScrollPane pane = new JScrollPane(table);

        JvUndoManager undoManager = new JvUndoManager();
        tableModel.addUndoableEditListener(undoManager);

        JMenu editMenu = new JMenu("Edit");

        Action addrowaction = new AbstractAction("Add Row") {
            private static final long serialVersionUID = 1433684360133156145L;


            public void actionPerformed(ActionEvent e) {
                tableModel.insertRow(table.getRowCount(), new Object[]{"YHOO", "Yahoo!", 37.69, "BUY"});


            }
        };
        editMenu.add(undoManager.getUndoAction());
        //editMenu.add(undoManager.getRedoAction());

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(editMenu);
        editMenu.add(addrowaction);


        JFrame frame = new JFrame("Undoable JTable");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        frame.add(pane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setLocation(200, 300);
        frame.setVisible(true);
    }
}


class JvUndoableTableModel extends DefaultTableModel
{
    public JvUndoableTableModel(Object[][] data, Object[] columnNames)
    {
        super(data, columnNames);
    }


    public Class getColumnClass(int column)
    {
        if (column >= 0 && column < getColumnCount())
            return getValueAt(0, column).getClass();

        return Object.class;
    }



    @Override
    public void setValueAt(Object value, int row, int column)
    {
        setValueAt(value, row, column, true);
    }


    public void setValueAt(Object value, int row, int column, boolean undoable)
    {
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null)
        {
            super.setValueAt(value, row, column);
            return;
        }


        Object oldValue = getValueAt(row, column);
        super.setValueAt(value, row, column);

        JvCellEdit cellEdit = new JvCellEdit(this, oldValue, value, row, column);
        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
        for (UndoableEditListener listener : listeners)
            listener.undoableEditHappened(editEvent);

    }

    //adding new cell to the table
    public void insertRow(int row, Object[] rowData){
        insertRow(row, rowData, true);
    }

    public void insertRow(int row,
            Object[] rowData,boolean undoable){
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null)
        {
            super.insertRow(row, rowData);
            return;
        }

        super.insertRow(row, rowData);
        JvCellNew cellNew = new JvCellNew(this, rowData, row);

        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellNew);
        for (UndoableEditListener listener : listeners)
            listener.undoableEditHappened(editEvent);

    }


    //removing row from the table
    public void removeRow(int row){
        removeRow(row, true);
    }
    public void removeRow(int row, boolean undoable){
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null)
        {
            super.removeRow(row);
            return;
        }
        super.removeRow(row);
        JvCellNew cellNew = new JvCellNew(this, row);
        UndoableEditEvent editEvent = new UndoableEditEvent(this, cellNew);
        for (UndoableEditListener listener : listeners)
            listener.undoableEditHappened(editEvent);

    }


    public void addUndoableEditListener(UndoableEditListener listener)
    {
        listenerList.add(UndoableEditListener.class, listener);
    }
}


class JvCellEdit extends AbstractUndoableEdit
{
    protected JvUndoableTableModel tableModel;
    protected Object oldValue;
    protected Object newValue;
    protected int row;
    protected int column;


    public JvCellEdit(JvUndoableTableModel tableModel, Object oldValue, Object newValue, int row, int column)
    {
        this.tableModel = tableModel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.row = row;
        this.column = column;
    }


    @Override
    public String getPresentationName()
    {
        return "Cell Edit";
    }


    @Override
    public void undo() throws CannotUndoException
    {
        super.undo();

        tableModel.setValueAt(oldValue, row, column, false);
    }
}
class JvCellNew extends AbstractUndoableEdit
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected JvUndoableTableModel tableModel;
    protected Object[] rowData;
    protected int row;

    public JvCellNew(JvUndoableTableModel tableModel, Object[] rowData, int row)
    {
        this.tableModel = tableModel;        
        this.rowData = rowData;
        this.row = row;

    }
    public JvCellNew(JvUndoableTableModel tableModel, int row)
    {
        this.tableModel = tableModel;        
        this.row = row;

    }
    @Override
    public String getPresentationName()
    {
        return "Cell New"+row;
    }
    // Provide a useful toString implementation
    @Override
    public String toString()
    {
        return getPresentationName();
    }
    public void undo() throws CannotUndoException
    {
        super.undo();
        tableModel.removeRow(row,false);

    }
}


class JvUndoManager extends UndoManager
{
    protected Action undoAction;
   // protected Action redoAction;


    public JvUndoManager()
    {
        this.undoAction = new JvUndoAction(this);
        synchronizeActions();           // to set initial names
    }


    public Action getUndoAction()
    {
        return undoAction;
    }



    @Override
    public boolean addEdit(UndoableEdit anEdit)
    {
        try
        {
            boolean b = super.addEdit(anEdit);

            // Print the current state of this manager 
            System.out.println("After adding "+anEdit);
            for (UndoableEdit e : this.edits)
            {
                System.out.println(e);
            }
            return b;

        }
        finally
        {
            synchronizeActions();
        }
    }


    @Override
    protected void undoTo(UndoableEdit edit) throws CannotUndoException
    {
        try
        {
            super.undoTo(edit);

            // Print the current state of this manager 
            System.out.println("After undo to "+edit);
            for (UndoableEdit e : this.edits)
            {
                System.out.println(e);
            }

        }
        finally
        {
            synchronizeActions();
        }
    }


    protected void synchronizeActions()
    {
        undoAction.setEnabled(canUndo());
        undoAction.putValue(Action.NAME, getUndoPresentationName());
    }
}


class JvUndoAction extends AbstractAction
{
    protected final UndoManager manager;


    public JvUndoAction(UndoManager manager)
    {
        this.manager = manager;
    }


    public void actionPerformed(ActionEvent e)
    {
        try
        {
            manager.undo();
        }
        catch (CannotUndoException ex)
        {
            ex.printStackTrace();
        }
    }
}