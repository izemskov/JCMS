/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.CatalogRepository;
import ru.develgame.jcms.repositories.ContentRepository;

import java.util.List;

@Component
public class CommonFunctions {
    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    public void addRecursContentsToDataModel(List<Content> res, Content parent, Content exclude) {
        List<Content> byParentContent = contentRepository.findByParentContentOrderByOrderContent(parent);
        for (Content elem : byParentContent) {
            if (!elem.equals(exclude)) {
                res.add(elem);
                addRecursContentsToDataModel(res, elem, exclude);
            }
        }
    }

    public void addRecursCatalogsToDataModel(List<Catalog> res, Catalog parent, Catalog exclude) {
        List<Catalog> byParentCatalog = catalogRepository.findByParentCatalogOrderByOrderCatalog(parent);
        for (Catalog elem : byParentCatalog) {
            if (!elem.equals(exclude)) {
                res.add(elem);
                addRecursCatalogsToDataModel(res, elem, exclude);
            }
        }
    }
}
