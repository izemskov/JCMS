/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.entities;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
public class Content {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private String name;
    private String fullName;
    private String link;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Content parentContent;

    @OneToMany(mappedBy = "parentContent")
    private Set<Content> childrenContents;

    @Lob
    private String content;

    private int orderContent;

    private String metaTitle;
    private String metaDescription;
    private String metaKeyword;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Content getParentContent() {
        return parentContent;
    }

    public void setParentContent(Content parentContent) {
        this.parentContent = parentContent;
    }

    public Set<Content> getChildrenContents() {
        return childrenContents;
    }

    public void setChildrenContents(Set<Content> childrenContents) {
        this.childrenContents = childrenContents;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrderContent() {
        return orderContent;
    }

    public void setOrderContent(int orderContent) {
        this.orderContent = orderContent;
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return getId() == content.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
