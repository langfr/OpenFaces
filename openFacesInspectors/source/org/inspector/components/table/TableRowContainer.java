/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2015, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.inspector.components.table;

import org.inspector.api.ElementWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;

/**
 * @author Max Yurin
 */
public class TableRowContainer extends ElementWrapper implements Iterable<TableRow> {
    public static final By ROWS = By.xpath(TableRow.TAG_NAME);

    public TableRowContainer(WebDriver webDriver, String id, String type) {
        super(webDriver, id, type);
    }

    public TableRowContainer(WebDriver driver, WebElement element, String type) {
        super(driver, element, type);
    }

    public Iterator<TableRow> iterator() {
        return new RowIterator(element().findElements(ROWS));
    }

    private final class RowIterator implements Iterator<TableRow> {
        private List<WebElement> rows;
        private int index = 0;

        public RowIterator(List<WebElement> rows) {
            this.rows = rows;
        }

        public boolean hasNext() {
            return index < rows.size();
        }

        public TableRow next() {
            return new TableRow(driver(), rows.get(index++).getAttribute("id"));
        }

        public void remove() {
            rows.remove(index);
        }
    }

    public TableRow row(int index) {
        final Iterator<TableRow> iterator = iterator();
        TableRow row;
        int counter = 0;

        while(iterator.hasNext() && (row = iterator.next()) != null){
            if(counter == index){
                return row;
            }
        }

        return null;
    }
}