/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.Catalog;
import ru.develgame.jcms.entities.CatalogItem;
import ru.develgame.jcms.repositories.CatalogItemRepository;
import ru.develgame.jcms.repositories.CatalogRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddEditCatalogItemComposer extends SelectorComposer {
    @Wire private Window addEditCatalogItemForm;
    @Wire private Textbox nameTextBox;
    @Wire private Textbox linkTextBox;
    @Wire private Listbox catalogsListBox;
    @Wire private Bandbox catalogsBandBox;
    @Wire private Textbox smallDescription;
    @Wire private Textbox description;
    @Wire private Textbox orderTextBox;
    @Wire private Textbox metaTitleTextBox;
    @Wire private Textbox metaDescriptionTextBox;
    @Wire private Textbox metaKeywordsTextBox;
    @Wire private Image img;

    private static final int FILE_SIZE = 30000;

    @WireVariable private CommonFunctions commonFunctions;
    @WireVariable private CatalogRepository catalogRepository;
    @WireVariable private CatalogItemRepository catalogItemRepository;
    @WireVariable private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = null;

    private ListModelList<Catalog> catalogsDataModel = null;

    private CatalogItem catalogItem = null;

    private String imageGUID = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }

    public ListModelList<Catalog> getCatalogsDataModel() {
        if (catalogsDataModel == null) {
            catalogsDataModel = new ListModelList<>(catalogRepository.findAll());
            catalogsDataModel.setMultiple(true);
        }

        return catalogsDataModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        addEditCatalogItemForm.addEventListener("onCustomClose", event -> {
            addEditCatalogItemForm.detach();
        });

        String catalogItemIdStr = (String) Executions.getCurrent().getArg().get("catalogItemId");
        if (catalogItemIdStr != null && !catalogItemIdStr.isEmpty()) {
            try {
                long catalogItemId = Long.parseLong(catalogItemIdStr);
                Optional<CatalogItem> byId = catalogItemRepository.findById(catalogItemId);
                if (!byId.isPresent()) {
                    Messagebox.show(Labels.getLabel("addEditCatalogItem.error.catalogItemNotFound"),
                            null, 0,  Messagebox.ERROR);
                    Events.postEvent("onCustomClose", addEditCatalogItemForm, null);
                    return;
                }
                catalogItem = byId.get();

                addEditCatalogItemForm.setTitle(Labels.getLabel("addEditCatalogItem.title.edit"));

                nameTextBox.setText(catalogItem.getName());
                linkTextBox.setText(catalogItem.getLink());
                if (catalogItem.getCatalogs() != null) {
                    catalogsDataModel.setSelection(catalogItem.getCatalogs());
                    catalogsListBox.renderAll();
                    fillCatalogsBandBox();
                }
                smallDescription.setText(catalogItem.getSmallDescription());
                description.setText(catalogItem.getDescription());
                orderTextBox.setText(Integer.toString(catalogItem.getOrderCatalogItem()));
                metaTitleTextBox.setText(catalogItem.getMetaTitle());
                metaDescriptionTextBox.setText(catalogItem.getMetaDescription());
                metaKeywordsTextBox.setText(catalogItem.getMetaKeyword());
            }
            catch (NumberFormatException ex) {
                Messagebox.show(Labels.getLabel("addEditCatalogItem.error.catalogItemIdNan"),
                        null, 0,  Messagebox.ERROR);
                Events.postEvent("onCustomClose", addEditCatalogItemForm, null);
                return;
            }
        }
        else {
            String parentCatalogIdStr = (String) Executions.getCurrent().getArg().get("parentCatalogId");
            if (parentCatalogIdStr != null && !parentCatalogIdStr.isEmpty()) {
                try {
                    long parentCatalogId = Long.parseLong(parentCatalogIdStr);
                    Optional<Catalog> byId = catalogRepository.findById(parentCatalogId);
                    if (byId.isPresent()) {
                        Catalog parentCatalog = byId.get();
                        catalogsDataModel.setSelection(Arrays.asList(parentCatalog));
                        catalogsListBox.renderAll();
                        fillCatalogsBandBox();
                    }
                }
                catch (NumberFormatException ex) {
                    return;
                }
            }
        }

        catalogsListBox.addEventListener(Events.ON_CLICK, t -> catalogsBandBox.setOpen(true));
    }

    private void fillCatalogsBandBox() {
        StringBuilder str = new StringBuilder();

        for (Listitem li : catalogsListBox.getItems()) {
            if (!li.isSelected()) {
                continue;
            }

            if (str.length() > 0) {
                str.append(",");
            }

            str.append(li.getLabel());
        }

        catalogsBandBox.setValue(str.toString());
    }

    private void saveFile(Media media, String filename) throws IOException {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            InputStream fin = media.getStreamData();
            in = new BufferedInputStream(fin);

            File baseDir = new File(commonFunctions.getCatalogItemSavePath());

            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }

            File file = new File(commonFunctions.getCatalogItemSavePath(), filename);

            OutputStream fout = new FileOutputStream(file);
            out = new BufferedOutputStream(fout);
            byte buffer[] = new byte[1024];
            int ch = in.read(buffer);
            while (ch != -1) {
                out.write(buffer, 0, ch);
                ch = in.read(buffer);
            }
        }
        finally {
            try {
                if (out != null)
                    out.close();

                if (in != null)
                    in.close();

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Listen("onUpload = #uploadBtn")
    public void onUpload(UploadEvent event) {
        Media media = event.getMedia();

        if (media == null) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.selectFile"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        String type = media.getContentType().split("/")[0];
        if (type.equals("image")) {
            if (media.getByteData().length > FILE_SIZE * 1024) {
                Messagebox.show(Labels.getLabel("addEditCatalogItem.error.limitFileSize") + FILE_SIZE + "k",
                        null, 0,  Messagebox.EXCLAMATION);
                return;
            }
            org.zkoss.image.Image picture = (org.zkoss.image.Image) media;
            img.setContent(picture);
        }

        try {
            imageGUID = UUID.randomUUID().toString() + ".png";
            saveFile(media, imageGUID);

            File file = new File(commonFunctions.getCatalogItemSavePath(), imageGUID);
            BufferedImage in = ImageIO.read(file);
            if (in == null) {
                Messagebox.show(Labels.getLabel("addEditCatalogItem.error.readImage"),
                        null, 0,  Messagebox.EXCLAMATION);
                return;
            }

            // TODO - set size from outside
            BufferedImage resizedCopyBig = commonFunctions.createResizedCopy(in, 400, 0);
            File fileBig = new File(commonFunctions.getCatalogItemSavePathBig(), imageGUID);
            ImageIO.write(resizedCopyBig, "png", fileBig);

            BufferedImage resizedCopySmall = commonFunctions.createResizedCopy(in, 190, 0);
            File fileSmall = new File(commonFunctions.getCatalogItemSavePathSmall(), imageGUID);
            ImageIO.write(resizedCopySmall, "png", fileSmall);
        }
        catch (IOException e) {
            logger.warn("", e);
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.limitFileSize", Arrays.asList(FILE_SIZE).toArray()),
                    null, 0,  Messagebox.EXCLAMATION);
        }
        catch (Exception e) {
            logger.warn("", e);
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.CannotResizeImage"),
                    null, 0,  Messagebox.EXCLAMATION);
        }
    }

    @Listen("onSelect = #catalogsListBox")
    public void setCatalogsListBoxOnSelect() {
        fillCatalogsBandBox();
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditCatalogItemForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (orderTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        try {
            Integer.parseInt(orderTextBox.getText());
        }
        catch (NumberFormatException ex) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.orderTextBoxNan"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        // create build
        Integer status = getTransactionTemplate().execute(s -> {
            try {
                List<Catalog> addCatalogs = new ArrayList<>();
                for (Catalog elem : catalogsDataModel.getSelection()) {
                    Optional<Catalog> byId = catalogRepository.findById(elem.getId());
                    if (byId.isPresent()) {
                        addCatalogs.add(byId.get());
                    }
                }

                if (catalogItem == null)
                    catalogItem = new CatalogItem();
                catalogItem.setName(nameTextBox.getText());
                catalogItem.setLink(linkTextBox.getText());
                catalogItem.setSmallDescription(smallDescription.getText());
                catalogItem.setDescription(description.getText());
                catalogItem.setOrderCatalogItem(Integer.parseInt(orderTextBox.getText()));
                catalogItem.setMetaTitle(metaTitleTextBox.getText());
                catalogItem.setMetaDescription(metaDescriptionTextBox.getText());
                catalogItem.setMetaKeyword(metaKeywordsTextBox.getText());

                if (imageGUID != null && !imageGUID.isEmpty())
                    catalogItem.setPhoto(imageGUID);

                catalogItem.getCatalogs().clear();
                addCatalogs.forEach(t -> {
                    catalogItem.addCatalog(t);
                });

                catalogItemRepository.save(catalogItem);

                return 0;
            }
            catch (Exception ex) {
                logger.error("", ex);
                return 1;
            }
        });

        if (status != 0) {
            Messagebox.show(Labels.getLabel("addEditCatalogItem.error.someError"),
                    null, 0,  Messagebox.ERROR);
            return;
        }

        addEditCatalogItemForm.detach();

        EventQueue<Event> eq = EventQueues.lookup("addEditCatalogItems", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditCatalogItems", null, "finished"));
    }
}
