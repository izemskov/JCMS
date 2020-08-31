/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.Content;
import ru.develgame.jcms.repositories.CatalogRepository;
import ru.develgame.jcms.repositories.ContentRepository;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Component
public class CommonFunctions {
    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Value("${catalogItemSavePath}")
    private String catalogItemSavePath;

    @Value("${catalogItemSavePathSmall}")
    private String catalogItemSavePathSmall;

    @Value("${catalogItemSavePathBig}")
    private String catalogItemSavePathBig;

    public String getCatalogItemSavePath() {
        return catalogItemSavePath;
    }

    public String getCatalogItemSavePathSmall() {
        return catalogItemSavePathSmall;
    }

    public String getCatalogItemSavePathBig() {
        return catalogItemSavePathBig;
    }

    // For access from RowRender
    private static String staticCatalogItemSavePathSmall;

    public static String getStaticCatalogItemSavePathSmall() {
        return staticCatalogItemSavePathSmall;
    }

    @PostConstruct
    public void init() {
        staticCatalogItemSavePathSmall = catalogItemSavePathSmall;
    }

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

    public BufferedImage createResizedCopy(BufferedImage originalImage, int newWidth, int newHeight) throws Exception {
        if (newWidth == 0 && newHeight == 0) {
            newWidth = originalImage.getWidth();
            newHeight = originalImage.getHeight();
        }
        else if (newWidth != 0 && newHeight != 0) {
            throw new Exception("Crop not supported. One of newWidth or newHeight must be a zero");
        }
        else {
            if (newWidth == 0)
                newWidth = originalImage.getWidth() * (newHeight * 100 / originalImage.getHeight()) / 100;

            if (newHeight == 0)
                newHeight = originalImage.getHeight() * (newWidth * 100 / originalImage.getWidth()) / 100;
        }

        int type = (originalImage.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage bufferedImage = new BufferedImage(newWidth, newHeight, type);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        if (originalImage.getTransparency() == Transparency.OPAQUE)
            g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return bufferedImage;
    }
}
