/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.renders;

import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import ru.develgame.jcms.entities.Catalog;

public class CatalogsParentComboItemRender implements ComboitemRenderer<Catalog> {
    private String indentsCatalog(Catalog catalog) {
        int level = 0;
        Catalog parentCatalog = catalog.getParentCatalog();
        while (parentCatalog != null) {
            level++;
            parentCatalog = parentCatalog.getParentCatalog();
        }

        StringBuilder res = new StringBuilder(catalog.getName());
        for (int i = 0; i < level * 2; i++)
            res.insert(0, "-");

        return res.toString();
    }

    @Override
    public void render(Comboitem comboitem, Catalog catalog, int i) throws Exception {
        comboitem.setLabel(indentsCatalog(catalog));
    }
}
