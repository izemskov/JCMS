/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin")
    public String mainPage() {
        return "admin/contents";
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin/contents")
    public String contents() {
        return "admin/contents";
    }

    @GetMapping("/admin/catalogs")
    public String catalogs() {
        return "admin/catalogs";
    }

    @GetMapping("/admin/users")
    public String users() {
        return "admin/users";
    }
}
