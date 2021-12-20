/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */
package ru.develgame.jcms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.services.ContentService;
import ru.develgame.jcms.services.MainMenuService;

@Component
@RequestMapping("content")
public class ContentController {
    @Autowired
    private ContentService contentService;

    @Autowired
    private MainMenuService mainMenuService;

    @RequestMapping(value = "/id/{id}")
    public String getContent(@PathVariable Long id, Model model) {
        mainMenuService.addMainMenuToModel(model);

        Content contentById = contentService.getContentById(id);
        if (contentById == null)
            return "notFound";

        model.addAttribute("content", contentById);
        return "content";
    }

    @RequestMapping(value = "{link}")
    public String getContent(@PathVariable String link, Model model) {
        mainMenuService.addMainMenuToModel(model);

        Content contentById = contentService.getContentByLink(link);
        if (contentById == null) {
            return "notFound";
        }

        model.addAttribute("content", contentById);
        return "content";
    }
}
