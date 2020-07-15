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
import ru.develgame.jcms.repositories.ContentRepository;

import java.util.Arrays;
import java.util.Set;

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

    private TransactionTemplate transactionTemplate = null;

    private ListModelList<Content> parentModel;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ListModelList<Content> getParentModel() {
        if (parentModel == null) {
            parentModel = new ListModelList<>(contentRepository.findAll());
            if (!parentModel.getInnerList().isEmpty())
                parentModel.setSelection(Arrays.asList(parentModel.getInnerList().get(0)));
        }

        return parentModel;
    }

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditContentForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addBuild.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (orderTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addBuild.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        try {
            Integer.parseInt(orderTextBox.getText());
        }
        catch (NumberFormatException ex) {
            Messagebox.show(Labels.getLabel("addBuild.error.orderTextBoxNan"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        Set<Content> selection = parentModel.getSelection();

        // create build
        Integer status = getTransactionTemplate().execute(s -> {
            try {
                Content content = new Content();
                content.setName(nameTextBox.getText());
                content.setFullName(fullNameTextBox.getText());
                content.setLink(fullNameTextBox.getText());

                if (!selection.isEmpty()) {
                    content.setParentContent(selection.iterator().next());
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
            Messagebox.show(Labels.getLabel("addBuild.error.someError"),
                    null, 0,  Messagebox.ERROR);
            return;
        }

        addEditContentForm.detach();

        EventQueue<Event> eq = EventQueues.lookup("addEditContents", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditContents", null, "finished"));
    }
}
