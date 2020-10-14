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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.repositories.CatalogRepository;

import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddEditCatalogComposer extends SelectorComposer {
    @Wire private Window addEditCatalogForm;
    @Wire private Textbox nameTextBox;
    @Wire private Textbox linkTextBox;
    @Wire private Combobox parentComboBox;
    @Wire private Textbox orderTextBox;
    @Wire private Textbox metaTitleTextBox;
    @Wire private Textbox metaDescriptionTextBox;
    @Wire private Textbox metaKeywordsTextBox;

    @WireVariable private CommonFunctions commonFunctions;
    @WireVariable private CatalogRepository catalogRepository;
    @WireVariable private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = null;

    private ListModelList<Catalog> parentModel;

    private Catalog catalog = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void refreshParentModel(Catalog exclude) {
        parentModel = new ListModelList<>();
        Catalog nullParent = new Catalog();
        nullParent.setName("");
        parentModel.add(nullParent);

        List<Catalog> res = new ArrayList<>();
        commonFunctions.addRecursCatalogsToDataModel(res, null, exclude);

        parentModel.addAll(res);
        parentModel.setSelection(Arrays.asList(parentModel.getInnerList().get(0)));
    }

    public ListModelList<Catalog> getParentModel() {
        if (parentModel == null) {
            refreshParentModel(null);
        }

        return parentModel;
    }

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        addEditCatalogForm.addEventListener("onCustomClose", event -> {
            addEditCatalogForm.detach();
        });

        String catalogIdStr = (String) Executions.getCurrent().getArg().get("catalogId");
        if (catalogIdStr != null && !catalogIdStr.isEmpty()) {
            try {
                long catalogId = Long.parseLong(catalogIdStr);
                Optional<Catalog> byId = catalogRepository.findById(catalogId);
                if (!byId.isPresent()) {
                    Messagebox.show(Labels.getLabel("addEditCatalog.error.catalogNotFound"),
                            null, 0,  Messagebox.ERROR);
                    Events.postEvent("onCustomClose", addEditCatalogForm, null);
                    return;
                }
                catalog = byId.get();

                addEditCatalogForm.setTitle(Labels.getLabel("addEditCatalog.title.edit"));

                nameTextBox.setText(catalog.getName());
                linkTextBox.setText(catalog.getLink());
                refreshParentModel(catalog);
                parentComboBox.setModel(getParentModel());
                if (catalog.getParentCatalog() != null)
                    parentModel.setSelection(Arrays.asList(catalog.getParentCatalog()));
                orderTextBox.setText(Integer.toString(catalog.getOrderCatalog()));
                metaTitleTextBox.setText(catalog.getMetaTitle());
                metaDescriptionTextBox.setText(catalog.getMetaDescription());
                metaKeywordsTextBox.setText(catalog.getMetaKeyword());
            }
            catch (NumberFormatException ex) {
                Messagebox.show(Labels.getLabel("addEditCatalog.error.catalogIdNan"),
                        null, 0,  Messagebox.ERROR);
                Events.postEvent("onCustomClose", addEditCatalogForm, null);
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
                        refreshParentModel(null);
                        parentComboBox.setModel(getParentModel());
                        parentModel.setSelection(Arrays.asList(parentCatalog));
                    }
                }
                catch (NumberFormatException ex) {
                    return;
                }
            }
        }
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditCatalogForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalog.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (orderTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalog.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        try {
            Integer.parseInt(orderTextBox.getText());
        }
        catch (NumberFormatException ex) {
            Messagebox.show(Labels.getLabel("addEditCatalog.error.orderTextBoxNan"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        Set<Catalog> selection = parentModel.getSelection();

        // create build
        Integer status = getTransactionTemplate().execute(s -> {
            try {
                if (catalog == null)
                    catalog = new Catalog();
                catalog.setName(nameTextBox.getText());
                catalog.setLink(linkTextBox.getText());

                if (!selection.isEmpty()) {
                    Catalog parentCatalog = selection.iterator().next();
                    if (!parentCatalog.getName().isEmpty())
                        catalog.setParentCatalog(parentCatalog);
                    else
                        catalog.setParentCatalog(null);
                }

                catalog.setOrderCatalog(Integer.parseInt(orderTextBox.getText()));
                catalog.setMetaTitle(metaTitleTextBox.getText());
                catalog.setMetaDescription(metaDescriptionTextBox.getText());
                catalog.setMetaKeyword(metaKeywordsTextBox.getText());

                catalogRepository.save(catalog);

                return 0;
            }
            catch (Exception ex) {
                logger.error("", ex);
                return 1;
            }
        });

        if (status != 0) {
            Messagebox.show(Labels.getLabel("addEditCatalog.error.someError"),
                    null, 0,  Messagebox.ERROR);
            return;
        }

        addEditCatalogForm.detach();

        EventQueue<Event> eq = EventQueues.lookup("addEditCatalogs", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditCatalogs", null, "finished"));
    }
}
