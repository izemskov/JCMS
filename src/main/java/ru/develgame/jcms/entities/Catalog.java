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
import java.util.Set;

@Entity
public class Catalog {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private String name;
    private String link;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Catalog parentCatalog;

    @OneToMany(mappedBy = "parentCatalog")
    private Set<Catalog> childrenCatalogs;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CatalogItem> catalogItems = new ArrayList<>();

    private int orderCatalog;

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

    public Catalog getParentCatalog() {
        return parentCatalog;
    }

    public void setParentCatalog(Catalog parentCatalog) {
        this.parentCatalog = parentCatalog;
    }

    public Set<Catalog> getChildrenCatalogs() {
        return childrenCatalogs;
    }

    public void setChildrenCatalogs(Set<Catalog> childrenCatalogs) {
        this.childrenCatalogs = childrenCatalogs;
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

    public List<CatalogItem> getCatalogItems() {
        return catalogItems;
    }

    public void setCatalogItems(List<CatalogItem> catalogItems) {
        this.catalogItems = catalogItems;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getOrderCatalog() {
        return orderCatalog;
    }

    public void setOrderCatalog(int orderCatalog) {
        this.orderCatalog = orderCatalog;
    }

    /* Overrided */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Catalog catalog = (Catalog) o;
        return getId() == catalog.getId();
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
    public void addCatalogItem(CatalogItem catalogItem) {
        addCatalogItem(catalogItem, true);
    }

    protected void addCatalogItem(CatalogItem catalogItem, boolean set) {
        if (catalogItem != null) {
            if (getCatalogItems().contains(catalogItem)) {
                getCatalogItems().set(getCatalogItems().indexOf(catalogItem), catalogItem);
            }
            else {
                getCatalogItems().add(catalogItem);
            }

            if (set) {
                catalogItem.addCatalog(this, false);
            }
        }
    }

    public void removeCatalogItem(CatalogItem catalogItem) {
        removeCatalogItem(catalogItem, true);
    }

    protected void removeCatalogItem(CatalogItem catalogItem, boolean remove) {
        getCatalogItems().remove(catalogItem);
        if (remove)
            catalogItem.removeCatalog(this, false);
    }
}
