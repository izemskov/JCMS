/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
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
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.repositories.CatalogItemRepository;
import ru.develgame.jcms.repositories.CatalogRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CatalogsComposer extends SelectorComposer {
    @Wire private Button removeCatalogButton;
    @Wire private Grid catalogsGrid;
    @Wire private Button removeCatalogItemButton;
    @Wire private Grid catalogItemsGrid;

    @WireVariable private CatalogRepository catalogRepository;
    @WireVariable private CatalogItemRepository catalogItemRepository;

    private ListModel<Catalog> catalogsDataModel = null;
    private ListModel<CatalogItem> catalogItemsDataModel = null;

    private Catalog parentCatalog = null;

    public Catalog getParentCatalog() {
        if (parentCatalog == null) {
            Execution e = Executions.getCurrent();
            HttpServletRequest request = (HttpServletRequest) e.getNativeRequest();

            String catalogIdStr = request.getParameter("catalogId");
            if (catalogIdStr != null && !catalogIdStr.isEmpty()) {
                try {
                    long parentCatalogId = Long.parseLong(catalogIdStr);
                    Optional<Catalog> byId = catalogRepository.findById(parentCatalogId);
                    if (byId.isPresent())
                        parentCatalog = byId.get();
                }
                catch (NumberFormatException ex) {}
            }
        }

        return parentCatalog;
    }

    private void refreshCatalogsDataModel(Catalog parentCatalog) {
        List<Catalog> res = catalogRepository.findByParentCatalogOrderByOrderCatalog(parentCatalog);
        catalogsDataModel = new ListModelList<>(res);
    }

    private void refreshCatalogItemsDataModel(Catalog parentCatalog) {
        List<CatalogItem> res = catalogItemRepository.findByCatalogsOrderByOrderCatalogItem(parentCatalog);
        catalogItemsDataModel = new ListModelList<>(res);
    }

    public ListModel<Catalog> getCatalogsDataModel() {
        if (catalogsDataModel == null) {
            refreshCatalogsDataModel(getParentCatalog());
        }

        return catalogsDataModel;
    }

    public ListModel<CatalogItem> getCatalogItemsDataModel() {
        if (catalogItemsDataModel == null) {
            refreshCatalogItemsDataModel(getParentCatalog());
        }

        return catalogItemsDataModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        removeCatalogButton.setDisabled(true);

        EventQueue<Event> eq = EventQueues.lookup("addEditCatalogs", EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            refreshCatalogsDataModel(parentCatalog);
            catalogsGrid.setModel(catalogsDataModel);
        });

        EventQueue<org.zkoss.zk.ui.event.Event> eq1 = EventQueues.lookup("delCatalogs", EventQueues.DESKTOP, true);
        eq1.subscribe(event -> {
            if (event.getData().equals("enable"))
                removeCatalogButton.setDisabled(false);
            else
                removeCatalogButton.setDisabled(true);
        });

        removeCatalogItemButton.setDisabled(true);

        EventQueue<Event> eq2 = EventQueues.lookup("addEditCatalogItems", EventQueues.DESKTOP, true);
        eq2.subscribe(event -> {
            refreshCatalogItemsDataModel(parentCatalog);
            catalogItemsGrid.setModel(catalogItemsDataModel);
        });

        EventQueue<org.zkoss.zk.ui.event.Event> eq3 = EventQueues.lookup("delCatalogItems", EventQueues.DESKTOP, true);
        eq3.subscribe(event -> {
            if (event.getData().equals("enable"))
                removeCatalogItemButton.setDisabled(false);
            else
                removeCatalogItemButton.setDisabled(true);
        });
    }

    @Listen("onClick = #createCatalogButton")
    public void createCatalogButtonOnClick() {
        Map<String, String> args = new HashMap<>();
        if (parentCatalog != null) {
            args.put("parentCatalogId", Long.toString(parentCatalog.getId()));
        }

        Window window = (Window) Executions.createComponents(
                "~./admin/widgets/addEditCatalog.zul", null, args);
        window.doModal();
    }

    @Listen("onClick = #createCatalogItemButton")
    public void createCatalogItemButtonOnClick() {
        Map<String, String> args = new HashMap<>();
        if (parentCatalog != null) {
            args.put("parentCatalogId", Long.toString(parentCatalog.getId()));
        }

        Window window = (Window) Executions.createComponents(
                "~./admin/widgets/addEditCatalogItem.zul", null, args);
        window.doModal();
    }
}
