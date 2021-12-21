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
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.CatalogItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogItemsRowRender implements RowRenderer<CatalogItem> {
    private List<CatalogItem> delCatalogItemsList = new ArrayList<>();

    @Override
    public void render(Row row, CatalogItem catalogItem, int i) throws Exception {
        if (catalogItem.getPhoto() != null && !catalogItem.getPhoto().isEmpty()) {
            File file = new File(CommonFunctions.getStaticCatalogItemSavePathSmall(), catalogItem.getPhoto());
            if (file.exists()) {
                BufferedImage in = ImageIO.read(file);
                Image image = new Image();
                image.setContent(in);
                image.setWidth("200px");
                row.appendChild(image);
            }
        }
        else {
            row.appendChild(new Label(""));
        }
        row.appendChild(new Label(catalogItem.getName()));
        row.appendChild(new Label(catalogItem.getLink()));

        Button buttonEdit = new Button(Labels.getLabel("catalogs.table.column.edit"));
        buttonEdit.addEventListener("onClick", t -> {
            Map<String, String> args = new HashMap<>();
            args.put("catalogItemId", Long.toString(catalogItem.getId()));

            Window window = (Window) Executions.createComponents(
                    "~./admin/widgets/addEditCatalogItem.zul", null, args);
            window.doModal();
        });
        Cell cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(buttonEdit);
        row.appendChild(cell);

        Checkbox checkbox = new Checkbox();
        checkbox.addEventListener("onCheck", t -> {
            if (((CheckEvent) t).isChecked()) {
                delCatalogItemsList.add(catalogItem);
            }
            else {
                delCatalogItemsList.remove(catalogItem);
            }

            EventQueue<Event> eq = EventQueues.lookup("delCatalogItems", EventQueues.DESKTOP, true);
            if (delCatalogItemsList.isEmpty())
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogItems", null, "disable"));
            else
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogItems", null, "enable"));
        });

        cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(checkbox);
        row.appendChild(cell);
    }

    public List<CatalogItem> getDelCatalogItemsList() {
        return delCatalogItemsList;
    }
}
