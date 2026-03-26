package com.adobe.aem.guides.wknd.core.models;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

@Model(adaptables = Resource.class)
public class FeatureListComponentModel {

    @Self
    private Resource resource;

    private List<Item> items = new ArrayList<>();

    @PostConstruct
    protected void init() {

        Resource features = resource.getChild("features");

        if (features != null) {
            for (Resource item : features.getChildren()) {

                String title = item.getValueMap().get("title", "");
                String description = item.getValueMap().get("description", "");
                String link = item.getValueMap().get("link", "");

                items.add(new Item(title, description, link));
            }
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private final String title;
        private final String description;
        private final String link;

        public Item(String title, String description, String link) {
            this.title = title;
            this.description = description;
            this.link = link;
        }

        public String getTitle() {
             return title;
            }
        public String getDescription() {
            return description;
        }
        public String getLink() {
            return link;
        }
    }
}