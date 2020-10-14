/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.renders;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.*;
import ru.develgame.jcms.entities.Catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogsRowRender implements RowRenderer<Catalog> {
    private List<Catalog> delCatalogsList = new ArrayList<>();

    @Override
    public void render(Row row, Catalog catalog, int i) throws Exception {
        A a = new A(catalog.getName());
        a.setHref("/admin/catalogs?catalogId=" + catalog.getId());
        row.appendChild(a);

        row.appendChild(new Label(catalog.getLink()));

        Button buttonEdit = new Button(Labels.getLabel("catalogs.table.column.edit"));
        buttonEdit.addEventListener("onClick", t -> {
            Map<String, String> args = new HashMap<>();
            args.put("catalogId", Long.toString(catalog.getId()));

            Window window = (Window) Executions.createComponents(
                    "~./admin/widgets/addEditCatalog.zul", null, args);
            window.doModal();
        });
        Cell cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(buttonEdit);
        row.appendChild(cell);

        Checkbox checkbox = new Checkbox();
        checkbox.addEventListener("onCheck", t -> {
            if (((CheckEvent) t).isChecked()) {
                delCatalogsList.add(catalog);
            }
            else {
                delCatalogsList.remove(catalog);
            }

            EventQueue<Event> eq = EventQueues.lookup("delCatalogs", EventQueues.DESKTOP, true);
            if (delCatalogsList.isEmpty())
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogs", null, "disable"));
            else
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogs", null, "enable"));
        });

        cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(checkbox);
        row.appendChild(cell);
    }

    public List<Catalog> getDelCatalogsList() {
        return delCatalogsList;
    }
}
