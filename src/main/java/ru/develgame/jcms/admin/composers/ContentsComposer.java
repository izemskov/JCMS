/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.composers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.zkoss.util.resource.Labels;
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
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.admin.renders.ContentsRowRender;
import ru.develgame.jcms.repositories.ContentRepository;
import ru.develgame.jcms.services.ContentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ContentsComposer extends SelectorComposer {
    @WireVariable private ContentRepository contentRepository;
    @WireVariable private CommonFunctions commonFunctions;
    @WireVariable private ContentService contentService;

    @Wire private Grid contentsGrid;
    @Wire private Button removeContentButton;

    private ListModel<Content> contentsDataModel = null;

    @WireVariable private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void refreshContentsDataModel() {
        List<Content> res = new ArrayList<>();

        commonFunctions.addRecursContentsToDataModel(res, null, null);

        contentsDataModel = new ListModelList<>(res);
    }

    public ListModel<Content> getContentsDataModel() {
        if (contentsDataModel == null) {
            refreshContentsDataModel();
        }

        return contentsDataModel;
    }

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
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
    public void removeContentButtonOnClick() {
        RowRenderer<Content> rowRenderer = contentsGrid.getRowRenderer();
        List<Content> delContentsList = ((ContentsRowRender) rowRenderer).getDelContentsList();

        int status = 0;
        try {
            getTransactionTemplate().execute(transactionStatus -> {
                contentService.deleteContents(delContentsList);
                return 0;
            });
        }
        catch (Exception ex) {
            logger.error("", ex);
            status = 1;
        }

        refreshContentsDataModel();
        contentsGrid.setModel(contentsDataModel);

        delContentsList.clear();

        removeContentButton.setDisabled(true);

        if (status != 0) {
            Messagebox.show(Labels.getLabel("content.error.cannotRemove"),
                    null, 0,  Messagebox.ERROR);
            return;
        }
    }
}
