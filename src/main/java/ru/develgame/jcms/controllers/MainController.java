/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.CatalogItemRepository;
import ru.develgame.jcms.repositories.ContentRepository;
import ru.develgame.jcms.services.MainMenuService;

import java.util.Optional;

@Controller
public class MainController {
    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private MainMenuService mainMenuService;

    @Autowired
    private ContentRepository contentRepository;

    @RequestMapping("/")
    public String index(Model model) {
        mainMenuService.addMainMenuToModel(model);

        Optional<Content> content = contentRepository.findTop1ByParentContentOrderByOrderContent(null);
        if (!content.isPresent())
            model.addAttribute("content", new Content());
        else
            model.addAttribute("content", content.get());

        Pageable limit = PageRequest.of(0,3);
        Page<CatalogItem> all = catalogItemRepository.findAll(limit);
        model.addAttribute("contentItems", all.getContent());

        return "index";
    }
}
