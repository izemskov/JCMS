/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.CatalogRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CatalogsComposer extends SelectorComposer {
    @WireVariable
    private CatalogRepository catalogRepository;

    private ListModel<Content> catalogsDataModel = null;

    private void refreshCatalogsDataModel(Catalog parentCatalog) {
        List<Content> res = new ArrayList<>();

        catalogRepository.findByParentCatalog(parentCatalog);

        catalogsDataModel = new ListModelList<>(res);
    }

    public ListModel<Content> getCatalogsDataModel() {
        if (catalogsDataModel == null) {
            Execution e = Executions.getCurrent();
            HttpServletRequest request = (HttpServletRequest) e.getNativeRequest();

            Catalog parentCatalog = null;
            String catalogIdStr = request.getParameter("catalogId");
            if (catalogIdStr != null && !catalogIdStr.isEmpty()) {
                try {
                    long catalogId = Long.parseLong(catalogIdStr);
                    Optional<Catalog> byId = catalogRepository.findById(catalogId);
                    if (byId.isPresent())
                        parentCatalog = byId.get();
                }
                catch (NumberFormatException ex) {}
            }

            refreshCatalogsDataModel(parentCatalog);
        }

        return catalogsDataModel;
    }

    @Listen("onClick = #createCatalogButton")
    public void createCatalogButtonOnClick() {
        Map<String, String> args = new HashMap<>();
        Window window = (Window) Executions.createComponents(
                "~./admin/widgets/addEditCatalog.zul", null, args);
        window.doModal();
    }
}
