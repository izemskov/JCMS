/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.renders;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import ru.develgame.jcms.entities.Catalog;

public class CatalogsListItemRender implements ListitemRenderer<Catalog> {
    @Override
    public void render(Listitem listitem, Catalog catalog, int i) throws Exception {
        listitem.appendChild(new Listcell(catalog.getName()));
    }
}
