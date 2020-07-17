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
import ru.develgame.jcms.entities.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentsRowRender implements RowRenderer<Content> {
    private List<Content> delContentsList = new ArrayList<>();

    private String indentsContent(Content content) {
        int level = 0;
        Content parentContent = content.getParentContent();
        while (parentContent != null) {
            level++;
            parentContent = parentContent.getParentContent();
        }

        StringBuilder res = new StringBuilder(content.getName());
        for (int i = 0; i < level * 4; i++)
            res.insert(0, " ");

        return res.toString();
    }

    @Override
    public void render(Row row, Content content, int i) throws Exception {
        Label nameLabel = new Label(indentsContent(content));
        nameLabel.setPre(true);
        row.appendChild(nameLabel);
        row.appendChild(new Label(content.getLink()));

        Button buttonEdit = new Button(Labels.getLabel("contents.table.column.edit"));
        buttonEdit.addEventListener("onClick", t -> {
            Map<String, String> args = new HashMap<>();
            args.put("contentId", Long.toString(content.getId()));

            Window window = (Window) Executions.createComponents(
                    "~./admin/widgets/addEditContent.zul", null, args);
            window.doModal();
        });
        Cell cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(buttonEdit);
        row.appendChild(cell);

        Checkbox checkbox = new Checkbox();
        checkbox.addEventListener("onCheck", t -> {
            if (((CheckEvent) t).isChecked()) {
                delContentsList.add(content);
            }
            else {
                delContentsList.remove(content);
            }

            EventQueue<Event> eq = EventQueues.lookup("delContents", EventQueues.DESKTOP, true);
            if (delContentsList.isEmpty())
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelContents", null, "disable"));
            else
                eq.publish(new org.zkoss.zk.ui.event.Event("checkBoxDelContents", null, "enable"));
        });

        cell = new Cell();
        cell.setAlign("center");
        cell.appendChild(checkbox);
        row.appendChild(cell);
    }

    public List<Content> getDelContentsList() {
        return delContentsList;
    }
}
