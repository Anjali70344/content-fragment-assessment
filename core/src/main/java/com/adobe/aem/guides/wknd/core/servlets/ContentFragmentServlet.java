package com.adobe.aem.guides.wknd.core.servlets;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/cf-data", // FRONTEND -> API CALL
        "sling.servlet.methods=GET",
        "sling.servlet.extensions=json"
})
public class ContentFragmentServlet extends SlingSafeMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ContentFragmentServlet.class);

    // DAM Folder Path
    private static final String CF_FOLDER_PATH = "/content/dam/wknd/content-fragment-folder";

    // CF Data Node Path
    private static final String DATA_NODE = "jcr:content/data/master";

    // Property Names (must match CF model field names)
    private static final String PROP_TITLE = "title";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_IMAGE_URL = "imageUrl";
    private static final String PROP_REDIRECT_URL = "redirectPageUrl";

    private final ObjectMapper objectMapper = new ObjectMapper();

    // This method is triggered when the frontend sends the request
    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        // Read DAM Folder
        Resource cfFolderResource = request.getResourceResolver()
                .getResource(CF_FOLDER_PATH);

        if (cfFolderResource == null) {
            response.getWriter().write("{\"error\":\"CF folder not found\"}");
            return;
        }

        List<Map<String, String>> contentFragments = new ArrayList<>();

        // Iterate through all children in the folder
        // Loop through Content Fragments
        for (Resource child : cfFolderResource.getChildren()) {

            if (child.getName().equals("jcr:content")) {
                continue;
            }

            // Read jcr:content/data/master
            Resource dataResource = child.getChild(DATA_NODE);

            if (dataResource == null) {
                LOG.warn("No data node found for: {}", child.getPath());
                continue;
            }

            // Access CF Data Node
            // Extract data from fragment
            ValueMap vm = dataResource.getValueMap();

            String title = vm.get(PROP_TITLE, "");
            String description = vm.get(PROP_DESCRIPTION, "");
            String imageUrl = vm.get(PROP_IMAGE_URL, "");
            String redirectUrl = vm.get(PROP_REDIRECT_URL, "");

            Map<String, String> cfData = new HashMap<>();

            // Extract Fields
            cfData.put("name", child.getName());
            cfData.put("path", child.getPath());
            cfData.put("title", title);
            cfData.put("description", description);
            cfData.put("imageUrl", imageUrl);
            cfData.put("redirectPageUrl", redirectUrl);

            // Return JSON Array
            // String temp = "message";
            contentFragments.add(cfData);
        }

        // JSON Array Response
        // response.getWriter().write(objectMapper.writeValueAsString(contentFragments));
        // Wrap response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data1", contentFragments);

        // Send JSON
        response.getWriter().write(objectMapper.writeValueAsString(responseMap));
    }

}