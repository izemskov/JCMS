/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.renders;

import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import ru.develgame.jcms.entities.Content;

public class ContentsParentComboItemRender implements ComboitemRenderer<Content> {
    private String indentsContent(Content content) {
        int level = 0;
        Content parentContent = content.getParentContent();
        while (parentContent != null) {
            level++;
            parentContent = parentContent.getParentContent();
        }

        StringBuilder res = new StringBuilder(content.getName());
        for (int i = 0; i < level * 2; i++)
            res.insert(0, "-");

        return res.toString();
    }

    @Override
    public void render(Comboitem comboitem, Content content, int i) throws Exception {
        comboitem.setLabel(indentsContent(content));
    }
}
