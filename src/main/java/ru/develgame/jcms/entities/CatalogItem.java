/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class CatalogItem {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private String name;
    private String fullName;
    private String link;

    private String photo;

    @ManyToMany(mappedBy = "catalogItems", fetch = FetchType.EAGER)
    private List<Catalog> catalogs = new ArrayList<>();

    @Column(columnDefinition="TEXT")
    private String smallDescription;

    @Column(columnDefinition="TEXT")
    private String description;

    private int orderCatalogItem;

    private String metaTitle;
    private String metaDescription;
    private String metaKeyword;

    /* Setters and getters */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Catalog> getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(List<Catalog> catalogs) {
        this.catalogs = catalogs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderCatalogItem() {
        return orderCatalogItem;
    }

    public void setOrderCatalogItem(int orderCatalogItem) {
        this.orderCatalogItem = orderCatalogItem;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeyword() {
        return metaKeyword;
    }

    public void setMetaKeyword(String metaKeyword) {
        this.metaKeyword = metaKeyword;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSmallDescription() {
        return smallDescription;
    }

    public void setSmallDescription(String smallDescription) {
        this.smallDescription = smallDescription;
    }

    /* Overrided */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogItem that = (CatalogItem) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return name;
    }

    /* Relationships */
    public void addCatalog(Catalog catalog) {
        addCatalog(catalog, true);
    }

    protected void addCatalog(Catalog catalog, boolean set) {
        if (catalog != null) {
            if (getCatalogs().contains(catalog)) {
                getCatalogs().set(getCatalogs().indexOf(catalog), catalog);
            }
            else {
                getCatalogs().add(catalog);
            }

            if (set) {
                catalog.addCatalogItem(this, false);
            }
        }
    }

    public void removeCatalog(Catalog catalog) {
        removeCatalog(catalog, true);
    }

    protected void removeCatalog(Catalog catalog, boolean remove) {
        getCatalogs().remove(catalog);
        if (remove)
            catalog.removeCatalogItem(this, false);
    }
}
