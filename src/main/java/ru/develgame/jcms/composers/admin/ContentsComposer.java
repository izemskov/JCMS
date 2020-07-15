/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.renders.ContentsRowRender;
import ru.develgame.jcms.repositories.ContentRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ContentsComposer extends SelectorComposer {
    @WireVariable private ContentRepository contentRepository;

    @Wire
    private Grid contentsGrid;

    @Wire
    private Button removeContentButton;

    private ListModel<Content> contentsDataModel = null;

    private void refreshContentsDataModel() {
        List<Content> res = new ArrayList<>();
        contentRepository.findAll(new Sort(Sort.Direction.DESC, "id")).forEach((t) -> {
            res.add(t);
        });

        contentsDataModel = new ListModelList<>(res);
    }

    public ListModel<Content> getContentsDataModel() {
        if (contentsDataModel == null) {
            refreshContentsDataModel();
        }

        return contentsDataModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        removeContentButton.setDisabled(true);

        EventQueue<Event> eq = EventQueues.lookup("addEditContents", EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            refreshContentsDataModel();
            contentsGrid.setModel(contentsDataModel);
        });

        EventQueue<org.zkoss.zk.ui.event.Event> eq1 = EventQueues.lookup("delContents", EventQueues.DESKTOP, true);
        eq1.subscribe(event -> {
            if (event.getData().equals("enable"))
                removeContentButton.setDisabled(false);
            else
                removeContentButton.setDisabled(true);
        });
    }

    @Listen("onClick = #createContentButton")
    public void createContentButtonOnClick() {
        Map<String, String> args = new HashMap<>();
        Window window = (Window) Executions.createComponents(
                "~./admin/widgets/addEditContent.zul", null, args);
        window.doModal();
    }

    @Listen("onClick = #removeContentButton")
    @Transactional
    public void removeContentButtonOnClick() {
        RowRenderer<Content> rowRenderer = contentsGrid.getRowRenderer();
        List<Content> delTemplatesList = ((ContentsRowRender) rowRenderer).getDelContentsList();

        delTemplatesList.forEach(t -> {

        });

        refreshContentsDataModel();
        contentsGrid.setModel(contentsDataModel);

        removeContentButton.setDisabled(true);
    }
}
