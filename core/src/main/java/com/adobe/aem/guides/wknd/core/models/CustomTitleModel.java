package com.adobe.aem.guides.wknd.core.models;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CustomTitleModel {

    
    @ValueMapValue
    private String title;

    
    @ValueMapValue
    private String pagePath;

  
    @SlingObject
    private ResourceResolver resourceResolver;

   
    @SlingObject
    private Resource resource;

    private String finalTitle = "Default Title";

    @PostConstruct
    protected void init() {

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        
        Page currentPage = pageManager.getContainingPage(resource);

      
        if (StringUtils.isNotBlank(title)) {
            finalTitle = title;
            return;
        }

        
        if (StringUtils.isNotBlank(pagePath)) {
            Page selectedPage = pageManager.getPage(pagePath);

            if (selectedPage != null && StringUtils.isNotBlank(selectedPage.getTitle())) {
                finalTitle = selectedPage.getTitle();
                return;
            }
        }

      
        if (currentPage != null && StringUtils.isNotBlank(currentPage.getTitle())) {
            finalTitle = currentPage.getTitle();
            return;
        }

       
        if (currentPage != null && currentPage.getParent() != null) {
            Page parent = currentPage.getParent();

            if (StringUtils.isNotBlank(parent.getTitle())) {
                finalTitle = parent.getTitle();
                return;
            }
        }
    }

    public String getTitle() {
        return finalTitle;
    }
}