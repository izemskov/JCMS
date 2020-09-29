/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.zkoss.util.resource.Labels;
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
import ru.develgame.jcms.CommonFunctions;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.renders.CatalogItemsRowRender;
import ru.develgame.jcms.renders.CatalogsRowRender;
import ru.develgame.jcms.repositories.CatalogItemRepository;
import ru.develgame.jcms.repositories.CatalogRepository;
import ru.develgame.jcms.repositories.SecurityUserRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CatalogsComposer extends SelectorComposer {
    @Wire private Button removeCatalogButton;
    @Wire private Grid catalogsGrid;
    @Wire private Button removeCatalogItemButton;
    @Wire private Grid catalogItemsGrid;
    @Wire private Div navBar;

    @WireVariable private CatalogRepository catalogRepository;
    @WireVariable private CatalogItemRepository catalogItemRepository;
    @WireVariable private CommonFunctions commonFunctions;
    @WireVariable private SecurityUserRepository securityUserRepository;

    private ListModel<Catalog> catalogsDataModel = null;
    private ListModel<CatalogItem> catalogItemsDataModel = null;

    private Catalog parentCatalog = null;

    @WireVariable private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }

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

        A buildsA = new A(Labels.getLabel("catalog.title"));
        buildsA.setHref("/admin/catalogs");
        navBar.appendChild(buildsA);

        if (parentCatalog != null) {
            List<Catalog> navBarCatalogs = new ArrayList<>();
            Catalog currentCatalog = parentCatalog;
            while (currentCatalog != null) {
                navBarCatalogs.add(currentCatalog);
                currentCatalog = currentCatalog.getParentCatalog();
            }
            Collections.reverse(navBarCatalogs);

            navBarCatalogs.forEach(e -> {
                A buildA = new A(e.getName());
                buildA.setHref("/admin/catalogs?catalogId=" + e.getId());
                navBar.appendChild(new Label(" > "));
                navBar.appendChild(buildA);
            });
        }
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

    @Listen("onClick = #removeCatalogItemButton")
    public void removeCatalogItemButtonOnClick() {
        RowRenderer<CatalogItem> rowRenderer = catalogItemsGrid.getRowRenderer();
        List<CatalogItem> delCatalogItemsList = ((CatalogItemsRowRender) rowRenderer).getDelCatalogItemsList();

        int status = 0;
        try {
            getTransactionTemplate().execute(transactionStatus -> {
                List<String> files = new ArrayList<>();
                for (CatalogItem elem : delCatalogItemsList) {
                    if (elem.getPhoto() != null && !elem.getPhoto().isEmpty())
                        files.add(elem.getPhoto());
                }

                for (CatalogItem elem : delCatalogItemsList) {
                    List<Catalog> catalogs = new ArrayList<>(elem.getCatalogs());
                    for (Catalog catalog : catalogs) {
                        catalog.removeCatalogItem(elem);
                        catalogRepository.save(catalog);
                    }

                    catalogItemRepository.delete(elem);
                }

                files.forEach(e -> {
                    File file = new File(commonFunctions.getCatalogItemSavePath(), e);
                    File fileSmall = new File(commonFunctions.getCatalogItemSavePathSmall(), e);
                    File fileBig = new File(commonFunctions.getCatalogItemSavePathBig(), e);

                    file.delete();
                    fileSmall.delete();
                    fileBig.delete();
                });

                return 0;
            });

        }
        catch (Exception ex) {
            status = 1;
            logger.error("", ex);
        }

        refreshCatalogItemsDataModel(parentCatalog);
        catalogItemsGrid.setModel(catalogItemsDataModel);

        delCatalogItemsList.clear();

        removeCatalogItemButton.setDisabled(true);

        if (status != 0) {
            Messagebox.show(Labels.getLabel("catalogs.error.cannotRemoveCatalogItems"),
                    null, 0,  Messagebox.ERROR);
            return;
        }
    }

    private void recursRemoveCatalogItems(Catalog parent, List<String> files) {
        List<CatalogItem> catalogItems = new ArrayList<>(parent.getCatalogItems());
        for (CatalogItem item : catalogItems) {
            item.removeCatalog(parent);
            catalogRepository.save(parent);

            if (item.getCatalogs().isEmpty()) {
                if (item.getPhoto() != null && !item.getPhoto().isEmpty())
                    files.add(item.getPhoto());
                catalogItemRepository.delete(item);
            }
        }

        for (Catalog elem : parent.getChildrenCatalogs()) {
            recursRemoveCatalogItems(elem, files);
        }
    }

    @Listen("onClick = #removeCatalogButton")
    public void removeCatalogButtonOnClick() {
        RowRenderer<Catalog> rowRenderer = catalogsGrid.getRowRenderer();
        List<Catalog> delCatalogList = ((CatalogsRowRender) rowRenderer).getDelCatalogsList();

        int status = 0;
        try {
            getTransactionTemplate().execute(transactionStatus -> {
                List<String> files = new ArrayList<>();
                List<Catalog> catalogsForDelete = new ArrayList<>();
                for (Catalog elem : delCatalogList) {
                    Optional<Catalog> byId = catalogRepository.findById(elem.getId());
                    if (byId.isPresent()) {
                        catalogsForDelete.add(byId.get());
                    }
                }

                for (Catalog elem : catalogsForDelete) {
                    recursRemoveCatalogItems(elem, files);
                }

                for (Catalog elem : catalogsForDelete) {
                    catalogRepository.delete(elem);
                }

                files.forEach(e -> {
                    File file = new File(commonFunctions.getCatalogItemSavePath(), e);
                    File fileSmall = new File(commonFunctions.getCatalogItemSavePathSmall(), e);
                    File fileBig = new File(commonFunctions.getCatalogItemSavePathBig(), e);

                    file.delete();
                    fileSmall.delete();
                    fileBig.delete();
                });

                return 0;
            });

        }
        catch (Exception ex) {
            status = 1;
            logger.error("", ex);
        }


    }
}
