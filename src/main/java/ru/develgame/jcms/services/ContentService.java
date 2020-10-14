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
package ru.develgame.jcms.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.ContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {
    @Autowired
    private ContentRepository contentRepository;

    @Cacheable(value = "contentByParent")
    public List<Content> getContentsByParent(Content parent) {
        List<Content> res = new ArrayList<>();
        List<Content> contents = contentRepository.findByParentContentOrderByOrderContent(parent);
        if (contents != null)
            res.addAll(contents);

        return res;
    }

    @Cacheable(value = "contentByIdLink")
    public Content getContentById(Long id) {
        Optional<Content> content = contentRepository.findById(id);
        if (content.isPresent())
            return content.get();
        return null;
    }

    @Cacheable(value = "contentByIdLink")
    public Content getContentByLink(String link) {
        Optional<Content> content = contentRepository.findByLink(link);
        if (content.isPresent())
            return content.get();
        return null;
    }

    @CacheEvict(value = {"contentByParent", "contentByIdLink"}, allEntries = true)
    public void saveContent(Content content) {
        contentRepository.save(content);
    }

    @CacheEvict(value = {"contentByParent", "contentByIdLink"}, allEntries = true)
    public void deleteContents(List<Content> delContentsList) {
        contentRepository.deleteAll(delContentsList);
    }
}
