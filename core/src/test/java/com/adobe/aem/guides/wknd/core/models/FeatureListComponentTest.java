
package com.adobe.aem.guides.wknd.core.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.adobe.aem.guides.wknd.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext; //mock framework for AEM unit testing
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class) //Integrates AEM Mock with JUnit 5
class FeatureListComponentTest {

    private final AemContext context = AppAemContext.newAemContext();

    private FeatureListComponentModel model;

    private Page page;
    private Resource resource;

    @BeforeEach
    void setUp() {

        //SetUp(): Runs before each test method to initialize the AEM context, 
        // create a test page, and set up the resource structure for the feature list component.
        page = context.create().page("/content/test-page");

       
        resource = context.create().resource(page, "featurelist",
                "sling:resourceType", "wknd/components/featurelist");

        
        context.create().resource(resource, "features/item0",
                "title", "Feature 1",
                "description", "Description 1",
                "link", "/content/page1");

        context.create().resource(resource, "features/item1",
                "title", "Feature 2",
                "description", "Description 2",
                "link", "/content/page2");

       
        model = resource.adaptTo(FeatureListComponentModel.class);
    }

    @Test
    void testItemsPopulated() {

        //Verify model correctly reads data from the resource and populates the list of items.
        List<FeatureListComponentModel.Item> items = model.getItems();

        assertNotNull(items);
        assertEquals(2, items.size());

        assertEquals("Feature 1", items.get(0).getTitle());
        assertEquals("Description 1", items.get(0).getDescription());
        assertEquals("/content/page1", items.get(0).getLink());
    }

    @Test
    void testEmptyFeatures() {

        //Check behavior when no features exist.
        
        Resource emptyResource = context.create().resource(page, "emptyFeature",
                "sling:resourceType", "wknd/components/featurelist");

        FeatureListComponentModel emptyModel =
                emptyResource.adaptTo(FeatureListComponentModel.class);

        assertNotNull(emptyModel);
        assertTrue(emptyModel.getItems().isEmpty());
    }

    @Test
    void testMissingProperties() {

        //Verify model correctly reads data
        
        Resource testResource = context.create().resource(page, "missingProps",
                "sling:resourceType", "wknd/components/featurelist");

        context.create().resource(testResource, "features/item0");

        FeatureListComponentModel testModel =
                testResource.adaptTo(FeatureListComponentModel.class);

        List<FeatureListComponentModel.Item> items = testModel.getItems();

        assertEquals(1, items.size());

        assertEquals("", items.get(0).getTitle());
        assertEquals("", items.get(0).getDescription());
        assertEquals("", items.get(0).getLink());
    }
}