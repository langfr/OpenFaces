/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2012, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.component.table;

import org.openfaces.component.command.MenuItem;
import org.openfaces.util.Resources;
import org.openfaces.util.ScriptBuilder;

import javax.faces.context.FacesContext;

public class SortDescendingMenuItem extends MenuItem implements ColumnMenuItem {
    public static final String COMPONENT_TYPE = "org.openfaces.SortDescendingMenuItem";
    public static final String COMPONENT_FAMILY = "org.openfaces.SortDescendingMenuItem";

    public SortDescendingMenuItem() {
        setRendererType("org.openfaces.SortDescendingMenuItemRenderer");
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public Object saveState(FacesContext context) {
        return new Object[]{
            super.saveState(context),

        };
    }

    @Override
    public void restoreState(FacesContext context, Object stateObj) {
        Object[] state = (Object[]) stateObj;
        int i = 0;
        super.restoreState(context, state[i++]);

    }

    public void setupMenuItemParams(FacesContext context){
        if (getValue() == null)
            setValue("Sort Descending");
        setOnclick(new ScriptBuilder().functionCall("O$.ColumnMenu._sortColumnDescending",
                getTable("<o:sortDescendingMenuItem>", this)).toString());
        if (getIconUrl() == null)
                setIconUrl(Resources.internalURL(
                    context, "table/sortDescending.png"));
    }


}
