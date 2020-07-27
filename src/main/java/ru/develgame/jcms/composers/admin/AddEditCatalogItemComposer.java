/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;
import ru.develgame.jcms.CommonFunctions;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.repositories.CatalogItemRepository;
import ru.develgame.jcms.repositories.CatalogRepository;

import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddEditCatalogItemComposer extends SelectorComposer {
    @Wire private Window addEditCatalogItemForm;
    @Wire private Textbox nameTextBox;
    @Wire private Textbox linkTextBox;
    @Wire private Listbox catalogsListBox;
    @Wire private Bandbox catalogsBandBox;
    @Wire private Textbox orderTextBox;
    @Wire private Textbox metaTitleTextBox;
    @Wire private Textbox metaDescriptionTextBox;
    @Wire private Textbox metaKeywordsTextBox;

    @WireVariable
    private CommonFunctions commonFunctions;
    @WireVariable private CatalogRepository catalogRepository;
    @WireVariable private CatalogItemRepository catalogItemRepository;
    @WireVariable private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = null;

    private ListModelList<Catalog> catalogsDataModel = null;

    private CatalogItem catalogItem = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }

    public ListModelList<Catalog> getCatalogsDataModel() {
        if (catalogsDataModel == null) {
            catalogsDataModel = new ListModelList<>(catalogRepository.findAll());
            catalogsDataModel.setMultiple(true);
        }

        return catalogsDataModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        addEditCatalogItemForm.addEventListener("onCustomClose", event -> {
            addEditCatalogItemForm.detach();
        });

        String catalogItemIdStr = (String) Executions.getCurrent().getArg().get("catalogItemId");
        if (catalogItemIdStr != null && !catalogItemIdStr.isEmpty()) {
            try {
                long catalogItemId = Long.parseLong(catalogItemIdStr);
                Optional<CatalogItem> byId = catalogItemRepository.findById(catalogItemId);
                if (!byId.isPresent()) {
                    Messagebox.show(Labels.getLabel("addEditCatalogItem.error.catalogItemNotFound"),
                            null, 0,  Messagebox.ERROR);
                    Events.postEvent("onCustomClose", addEditCatalogItemForm, null);
                    return;
                }
                catalogItem = byId.get();

                addEditCatalogItemForm.setTitle(Labels.getLabel("addEditCatalogItem.title.edit"));

                nameTextBox.setText(catalogItem.getName());
                linkTextBox.setText(catalogItem.getLink());
                if (catalogItem.getCatalogs() != null) {
                    catalogsDataModel.setSelection(catalogItem.getCatalogs());
                    catalogsListBox.renderAll();
                    fillCatalogsBandBox();
                }
                orderTextBox.setText(Integer.toString(catalogItem.getOrderCatalogItem()));
                metaTitleTextBox.setText(catalogItem.getMetaTitle());
                metaDescriptionTextBox.setText(catalogItem.getMetaDescription());
                metaKeywordsTextBox.setText(catalogItem.getMetaKeyword());
            }
            catch (NumberFormatException ex) {
                Messagebox.show(Labels.getLabel("addEditCatalogItem.error.catalogItemIdNan"),
                        null, 0,  Messagebox.ERROR);
                Events.postEvent("onCustomClose", addEditCatalogItemForm, null);
                return;
            }
        }
        else {
            String parentCatalogIdStr = (String) Executions.getCurrent().getArg().get("parentCatalogId");
            if (parentCatalogIdStr != null && !parentCatalogIdStr.isEmpty()) {
                try {
                    long parentCatalogId = Long.parseLong(parentCatalogIdStr);
                    Optional<Catalog> byId = catalogRepository.findById(parentCatalogId);
                    if (byId.isPresent()) {
                        Catalog parentCatalog = byId.get();
                        catalogsDataModel.setSelection(Arrays.asList(parentCatalog));
                        catalogsListBox.renderAll();
                        fillCatalogsBandBox();
                    }
                }
                catch (NumberFormatException ex) {
                    return;
                }
            }
        }

        catalogsListBox.addEventListener(Events.ON_CLICK, t -> catalogsBandBox.setOpen(true));
    }

    private void fillCatalogsBandBox() {
        StringBuilder str = new StringBuilder();

        for (Listitem li : catalogsListBox.getItems()) {
            if (!li.isSelected()) {
                continue;
            }

            if (str.length() > 0) {
                str.append(",");
            }

            str.append(li.getLabel());
        }

        catalogsBandBox.setValue(str.toString());
    }

    @Listen("onSelect = #catalogsListBox")
    public void setCatalogsListBoxOnSelect() {
        fillCatalogsBandBox();
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditCatalogItemForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (orderTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        try {
            Integer.parseInt(orderTextBox.getText());
        }
        catch (NumberFormatException ex) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.orderTextBoxNan"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        // create build
        Integer status = getTransactionTemplate().execute(s -> {
            try {
                List<Catalog> addCatalogs = new ArrayList<>();
                for (Catalog elem : catalogsDataModel.getSelection()) {
                    Optional<Catalog> byId = catalogRepository.findById(elem.getId());
                    if (byId.isPresent()) {
                        addCatalogs.add(byId.get());
                    }
                }

                if (catalogItem == null)
                    catalogItem = new CatalogItem();
                catalogItem.setName(nameTextBox.getText());
                catalogItem.setLink(linkTextBox.getText());
                catalogItem.setOrderCatalogItem(Integer.parseInt(orderTextBox.getText()));
                catalogItem.setMetaTitle(metaTitleTextBox.getText());
                catalogItem.setMetaDescription(metaDescriptionTextBox.getText());
                catalogItem.setMetaKeyword(metaKeywordsTextBox.getText());

                catalogItem.getCatalogs().clear();
                addCatalogs.forEach(t -> {
                    catalogItem.addCatalog(t);
                });

                catalogItemRepository.save(catalogItem);

                return 0;
            }
            catch (Exception ex) {
                logger.error("", ex);
                return 1;
            }
        });

        if (status != 0) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.someError"),
                    null, 0,  Messagebox.ERROR);
            return;
        }

        addEditCatalogItemForm.detach();

        EventQueue<Event> eq = EventQueues.lookup("addEditCatalogItems", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditCatalogItems", null, "finished"));
    }
}
