/*
 *
 * This software is copyright protected (c) 2020 by S-Terra CSP
 *
 * Author:              Ilya Zemskov
 * E-mail:              izemskov@s-terra.com
 *
 * Owner:               Ilya Zemskov
 * E-mail:              izemskov@s-terra.com
 *
 * $Header: $
 *
 */
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
        Content contentById = contentService.getContentById(id);
        if (contentById == null)
            return "notFound";

        mainMenuService.addMainMenuToModel(model);
        model.addAttribute("content", contentById);
        return "content";
    }

    @RequestMapping(value = "{link}")
    public String getContent(@PathVariable String link, Model model) {
        Content contentById = contentService.getContentByLink(link);
        if (contentById == null)
            return "notFound";

        mainMenuService.addMainMenuToModel(model);
        model.addAttribute("content", contentById);
        return "content";
    }
}
