package ticamac.dev_complex.market_plus.domain.model;

import java.util.List;
import java.util.UUID;

public class Category {

    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private int sortOrder;
    private boolean active;
    private List<Category> children;

    public Category() {
    }

    public Category(UUID id, String name, String slug,
            UUID parentId, int sortOrder, boolean active) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.active = active;
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }
}