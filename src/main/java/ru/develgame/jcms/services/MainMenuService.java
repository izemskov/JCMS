/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */
package ru.develgame.jcms.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class MainMenuService {
    @Autowired
    private ContentService contentService;

    public void addMainMenuToModel(Model model) {
        model.addAttribute("mainMenuList", contentService.getContentsByParent(null));
    }
}
