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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.develgame.jcms.common.StorageService;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.repositories.CatalogItemRepository;

import java.util.Optional;

@Controller
@RequestMapping("catalog")
public class CatalogController {
    @Autowired
    private CatalogItemRepository catalogItemRepository;

    @Autowired
    private StorageService storageService;

    @RequestMapping(value = "/item/{id}")
    public String catalogItem(@PathVariable Long id, Model model) {
        Optional<CatalogItem> byId = catalogItemRepository.findById(id);

        if (!byId.isPresent())
            return "notFound";

        model.addAttribute("catalogItem", byId.get());
        return "catalogItem";
    }

    @GetMapping("/images/small/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getSmallImg(@PathVariable String filename) {
        Resource file = storageService.loadAsResource("/small/" + filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/images/big/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getBigImg(@PathVariable String filename) {
        Resource file = storageService.loadAsResource("/big/" + filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
