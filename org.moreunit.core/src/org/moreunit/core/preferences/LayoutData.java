package org.moreunit.core.preferences;

import org.eclipse.swt.layout.GridData;

public class LayoutData
{
    public static GridData LABEL_AND_FIELD = new GridData(GridData.FILL_HORIZONTAL);
    static
    {
        LABEL_AND_FIELD.horizontalIndent = 15;
    }

    public static GridData ROW = new GridData(GridData.FILL_HORIZONTAL);
    static
    {
        ROW.horizontalSpan = 2;
    }
}
