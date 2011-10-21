/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2011, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.taglib.jsp.table;

import org.openfaces.taglib.internal.table.RowGroupingTag;
import org.openfaces.taglib.jsp.AbstractComponentJspTag;

import javax.el.ValueExpression;

public class RowGroupingJspTag extends AbstractComponentJspTag {

    public RowGroupingJspTag() {
        super(new RowGroupingTag());
    }


    public void setColumnHeaderVar(ValueExpression columnHeaderVar) {
        getDelegate().setPropertyValue("columnHeaderVar", columnHeaderVar);
    }

    public void setGroupingValueVar(ValueExpression groupingValueVar) {
        getDelegate().setPropertyValue("groupingValueVar", groupingValueVar);
    }

    public void setGroupHeaderText(ValueExpression groupHeaderText) {
        getDelegate().setPropertyValue("groupHeaderText", groupHeaderText);
    }

    public void setGroupingValueStringVar(ValueExpression groupingValueStringVar) {
        getDelegate().setPropertyValue("groupingValueStringVar", groupingValueStringVar);
    }
}