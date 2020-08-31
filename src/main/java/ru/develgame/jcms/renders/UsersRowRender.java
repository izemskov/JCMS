/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.renders;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.*;
import ru.develgame.jcms.entities.SecurityUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersRowRender implements RowRenderer<SecurityUser> {
    private List<SecurityUser> delUsersList = new ArrayList<>();

    @Override
    public void render(Row row, SecurityUser securityUser, int i) throws Exception {
        row.appendChild(new Label(securityUser.getName()));

        Button buttonEdit = new Button(Labels.getLabel("users.table.column.edit"));
        buttonEdit.addEventListener("onClick", t -> {
            Map<String, String> args = new HashMap<>();
            args.put("userId", Long.toString(securityUser.getId()));

            Window window = (Window) Executions.createComponents(
                    "~./admin/widgets/addEditUser.zul", null, args);
            window.doModal();
        });
        Cell cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(buttonEdit);
        row.appendChild(cell);

        Checkbox checkbox = new Checkbox();
        checkbox.addEventListener("onCheck", t -> {
            if (((CheckEvent) t).isChecked()) {
                delUsersList.add(securityUser);
            }
            else {
                delUsersList.remove(securityUser);
            }

            EventQueue<Event> eq = EventQueues.lookup("delUsers", EventQueues.DESKTOP, true);
            if (delUsersList.isEmpty())
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogItems", null, "disable"));
            else
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelCatalogItems", null, "enable"));
        });

        cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(checkbox);
        row.appendChild(cell);
    }

    public List<SecurityUser> getDelUsersList() {
        return delUsersList;
    }
}
