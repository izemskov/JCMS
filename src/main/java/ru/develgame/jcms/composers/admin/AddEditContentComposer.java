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
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.ContentRepository;

import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddEditContentComposer extends SelectorComposer {
    @Wire private Window addEditContentForm;
    @Wire private Textbox nameTextBox;
    @Wire private Textbox fullNameTextBox;
    @Wire private Textbox linkTextBox;
    @Wire private Combobox parentComboBox;
    @Wire private Textbox contentTextBox;
    @Wire private Textbox orderTextBox;
    @Wire private Textbox metaTitleTextBox;
    @Wire private Textbox metaDescriptionTextBox;
    @Wire private Textbox metaKeywordsTextBox;

    @WireVariable private ContentRepository contentRepository;
    @WireVariable private PlatformTransactionManager transactionManager;
    @WireVariable private CommonFunctions commonFunctions;

    private TransactionTemplate transactionTemplate = null;

    private ListModelList<Content> parentModel;

    private Content content = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void refreshParentModel(Content exclude) {
        parentModel = new ListModelList<>();
        Content nullParent = new Content();
        nullParent.setName("");
        parentModel.add(nullParent);

        List<Content> res = new ArrayList<>();
        commonFunctions.addRecursContentsToDataModel(res, null, exclude);

        parentModel.addAll(res);
        parentModel.setSelection(Arrays.asList(parentModel.getInnerList().get(0)));
    }

    public ListModelList<Content> getParentModel() {
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

        addEditContentForm.addEventListener("onCustomClose", event -> {
            addEditContentForm.detach();
        });

        String contentIdStr = (String) Executions.getCurrent().getArg().get("contentId");
        if (contentIdStr != null && !contentIdStr.isEmpty()) {
            try {
                long contentId = Long.parseLong(contentIdStr);
                Optional<Content> byId = contentRepository.findById(contentId);
                if (!byId.isPresent()) {
                    Messagebox.show(Labels.getLabel("addEditContent.error.contentNotFound"),
                            null, 0,  Messagebox.ERROR);
                    Events.postEvent("onCustomClose", addEditContentForm, null);
                    return;
                }
                content = byId.get();

                addEditContentForm.setTitle(Labels.getLabel("addEditContent.title.edit"));

                nameTextBox.setText(content.getName());
                fullNameTextBox.setText(content.getFullName());
                linkTextBox.setText(content.getLink());
                refreshParentModel(content);
                parentComboBox.setModel(getParentModel());
                if (content.getParentContent() != null)
                    parentModel.setSelection(Arrays.asList(content.getParentContent()));
                contentTextBox.setText(content.getContent());
                orderTextBox.setText(Integer.toString(content.getOrderContent()));
                metaTitleTextBox.setText(content.getMetaTitle());
                metaDescriptionTextBox.setText(content.getMetaDescription());
                metaKeywordsTextBox.setText(content.getMetaKeyword());
            }
            catch (NumberFormatException ex) {
                Messagebox.show(Labels.getLabel("addEditContent.error.contentIdNan"),
                        null, 0,  Messagebox.ERROR);
                Events.postEvent("onCustomClose", addEditContentForm, null);
                return;
            }
        }
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditContentForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditContent.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (orderTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditContent.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        try {
            Integer.parseInt(orderTextBox.getText());
        }
        catch (NumberFormatException ex) {
            Messagebox.show(Labels.getLabel("addEditContent.error.orderTextBoxNan"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        Set<Content> selection = parentModel.getSelection();

        // create build
        Integer status = getTransactionTemplate().execute(s -> {
            try {
                if (content == null)
                    content = new Content();
                content.setName(nameTextBox.getText());
                content.setFullName(fullNameTextBox.getText());
                content.setLink(linkTextBox.getText());

                if (!selection.isEmpty()) {
                    Content parentContent = selection.iterator().next();
                    if (!parentContent.getName().isEmpty())
                        content.setParentContent(parentContent);
                    else
                        content.setParentContent(null);
                }

                content.setOrderContent(Integer.parseInt(orderTextBox.getText()));
                content.setMetaTitle(metaTitleTextBox.getText());
                content.setMetaDescription(metaDescriptionTextBox.getText());
                content.setMetaKeyword(metaKeywordsTextBox.getText());

                contentRepository.save(content);

                return 0;
            }
            catch (Exception ex) {
                logger.error("", ex);
                return 1;
            }
        });

        if (status != 0) {
            Messagebox.show(Labels.getLabel("addEditContent.error.someError"),
                    null, 0,  Messagebox.ERROR);
            return;
        }

        addEditContentForm.detach();

        EventQueue<Event> eq = EventQueues.lookup("addEditContents", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditContents", null, "finished"));
    }
}
