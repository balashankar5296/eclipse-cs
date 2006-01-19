//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars K�dderitzsch, Fabrice Bellingard
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.util.table;

import java.text.Collator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * This subclass of <code>TableViewer</code> adds easier sorting support and
 * support for storing table settings (column width, sorter state).
 * 
 * @author Lars K�dderitzsch
 */
public class EnhancedTableViewer extends TableViewer
{
    //
    // constants
    //

    /** Key for the column index in the persistence store. */
    private static final String TAG_COLUMN_INDEX = "sortColumn"; //$NON-NLS-1$

    /** Key for the sort direction in the persistence store. */
    private static final String TAG_SORT_DIRECTION = "sortDirection"; //$NON-NLS-1$

    /** Key for the widths in the persistence store. */
    private static final String TAG_COLUMN_WIDTH = "colWidth"; //$NON-NLS-1$

    /** Key for the selection index in the persistence store. */
    private static final String TAG_CURRENT_SELECTION = "selectedRow"; //$NON-NLS-1$

    /** Integer constant for the forward sort direction value. */
    private static final int DIRECTION_FORWARD = 1;

    /** Integer constant for the reverse sort direction value. */
    private static final int DIRECTION_REVERSE = -1;

    /** Collator to support natural sorting of strings. */
    private static final Collator COLLATOR = Collator.getInstance(CheckstylePlugin
            .getPlatformLocale());

    //
    // attributes
    //

    /** the comparable provider for sorting support. */
    private ITableComparableProvider mComparableProvider;

    /** the settings provider. */
    private ITableSettingsProvider mSettingsProvider;

    /** The index of the current sorted column. */
    private int mSortedColumnIndex;

    /** The sort direction. */
    private int mSortDirection = DIRECTION_FORWARD;

    /** The table listener. */
    private TableListener mTableListener = new TableListener();

    //
    // constructors
    //

    /**
     * {@inheritDoc}
     */
    public EnhancedTableViewer(Table table)
    {
        super(table);

        // table.setLayout(new TableLayout()
        // {});
    }

    /**
     * {@inheritDoc}
     */
    public EnhancedTableViewer(Composite parent, int style)
    {
        super(parent, style);
    }

    /**
     * {@inheritDoc}
     */
    public EnhancedTableViewer(Composite parent)
    {
        super(parent);
    }

    //
    // methods
    //

    /**
     * Sets the comparable provider for this table.
     * 
     * @param comparableProvider the comparable provider
     */
    public void setTableComparableProvider(ITableComparableProvider comparableProvider)
    {
        mComparableProvider = comparableProvider;

        if (mComparableProvider != null)
        {
            this.setSorter(new TableSorter());
        }
        else
        {
            this.setSorter(null);
        }
    }

    /**
     * Returns the comparable provider.
     * 
     * @return the comparable provider
     */
    public ITableComparableProvider getTableComparableProvider()
    {
        return mComparableProvider;
    }

    /**
     * Sets the settings provider.
     * 
     * @param settingsProvider the settings provider
     */
    public void setTableSettingsProvider(ITableSettingsProvider settingsProvider)
    {
        mSettingsProvider = settingsProvider;
    }

    /**
     * Returns the settings provider.
     * 
     * @return the settings provider
     */
    public ITableSettingsProvider getTableSettingsProvider()
    {
        return mSettingsProvider;
    }

    /**
     * This method installs the enhancements over the standard TableViewer. This
     * method must be called only after all columns are set up for the
     * associated table.
     * 
     */
    public void installEnhancements()
    {

        this.getTable().removeSelectionListener(mTableListener);

        TableColumn[] columns = this.getTable().getColumns();
        for (int i = 0, size = columns.length; i < size; i++)
        {

            columns[i].removeSelectionListener(mTableListener);
            columns[i].removeControlListener(mTableListener);
        }

        restoreState();

        this.getTable().addSelectionListener(mTableListener);

        for (int i = 0, size = columns.length; i < size; i++)
        {
            columns[i].addSelectionListener(mTableListener);
            columns[i].addControlListener(mTableListener);
        }
    }

    /**
     * Saves the sorting state to the dialog settings.
     */
    private void saveState()
    {

        IDialogSettings settings = mSettingsProvider != null ? mSettingsProvider.getTableSettings()
                : null;

        if (settings == null)
        {
            return;
        }

        settings.put(TAG_COLUMN_INDEX, mSortedColumnIndex);
        settings.put(TAG_SORT_DIRECTION, mSortDirection);

        // store the column widths
        TableColumn[] columns = this.getTable().getColumns();
        for (int i = 0, size = columns.length; i < size; i++)
        {
            int width = columns[i].getWidth();
            if (width > 0)
            {
                settings.put(TAG_COLUMN_WIDTH + i, width);
            }
        }

        // store the selection
        settings.put(TAG_CURRENT_SELECTION, this.getTable().getSelectionIndex());
    }

    /**
     * Restores the sorting state from the dialog settings.
     */
    private void restoreState()
    {
        IDialogSettings settings = mSettingsProvider != null ? mSettingsProvider.getTableSettings()
                : null;

        if (settings == null)
        {
            return;
        }
        try
        {
            mSortedColumnIndex = settings.getInt(TAG_COLUMN_INDEX);
        }
        catch (NumberFormatException e)
        {
            mSortedColumnIndex = 0;
        }
        try
        {
            mSortDirection = settings.getInt(TAG_SORT_DIRECTION);
        }
        catch (NumberFormatException e)
        {
            mSortDirection = DIRECTION_FORWARD;
        }

        TableLayout layout = new TableLayout();
        boolean columnsAdded = false;

        // store the column widths
        TableColumn[] columns = this.getTable().getColumns();
        for (int i = 0, size = columns.length; i < size; i++)
        {
            try
            {
                columns[i].setWidth(settings.getInt(TAG_COLUMN_WIDTH + i));
                layout.addColumnData(new ColumnPixelData(settings.getInt(TAG_COLUMN_WIDTH + i)));
                columnsAdded = true;
            }
            catch (NumberFormatException e)
            {
                // NOOP
            }
        }
        if (columnsAdded)
        {
            this.getTable().setLayout(layout);
        }

        // restore the selection
        try
        {
            this.getTable().select(settings.getInt(TAG_CURRENT_SELECTION));
        }
        catch (NumberFormatException e)
        {
            // NOOP
        }

        resort();
    }

    /**
     * Helper method to resort the table viewer.
     */
    private void resort()
    {

        this.getTable().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                getControl().setRedraw(false);
                refresh(false);
                getControl().setRedraw(true);
            }
        });
    }

    /**
     * Listener for header clicks and resize events.
     * 
     * @author Lars K�dderitzsch
     */
    private class TableListener implements SelectionListener, ControlListener
    {

        public void widgetSelected(SelectionEvent e)
        {

            if (e.getSource() instanceof TableColumn)
            {

                TableColumn col = (TableColumn) e.getSource();
                Table table = col.getParent();

                int colIndex = table.indexOf(col);

                if (colIndex == mSortedColumnIndex)
                {
                    mSortDirection = mSortDirection * DIRECTION_REVERSE;
                }
                else
                {
                    mSortedColumnIndex = colIndex;
                    mSortDirection = DIRECTION_FORWARD;
                }

                resort();
                saveState();
            }
        }

        public void controlResized(ControlEvent e)
        {
            if (e.getSource() instanceof TableColumn)
            {
                saveState();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
        // NOOP
        }

        public void controlMoved(ControlEvent e)
        {
        // NOOP
        }
    }

    /**
     * Sorter implementation that uses the values provided by the comparable
     * provider to sort the table.
     * 
     * @author Lars K�dderitzsch
     */
    private class TableSorter extends ViewerSorter
    {

        /**
         * {@inheritDoc}
         */
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            Comparable c1 = mComparableProvider.getComparableValue(e1, mSortedColumnIndex);
            Comparable c2 = mComparableProvider.getComparableValue(e2, mSortedColumnIndex);

            int compareResult = 0;

            // support for string collation
            if (c1 instanceof String && c2 instanceof String)
            {
                compareResult = COLLATOR.compare(c1, c2);
            }
            else
            {
                compareResult = c1.compareTo(c2);
            }

            // take sort direction into account
            return compareResult * mSortDirection;
        }
    }
}