/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import ru.develgame.jcms.CommonFunctions;
import ru.develgame.jcms.entities.Catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @WireVariable
    private CommonFunctions commonFunctions;

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

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditCatalogForm.detach();
    }
}
