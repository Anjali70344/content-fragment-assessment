package com.adobe.aem.guides.wknd.core.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class ContentFragmentServletTest {

    private final ContentFragmentServlet fixture = new ContentFragmentServlet();
    private final ObjectMapper objectMapper = new ObjectMapper();


    //Confirms the guard clause works when the configured folder path does not exist
    @Test
    void doGetReturnsErrorWhenFolderMissing(AemContext context) throws ServletException, IOException {
	MockSlingHttpServletRequest request = context.request();
	MockSlingHttpServletResponse response = context.response();

	fixture.doGet(request, response);

	assertEquals("application/json", response.getContentType());
	assertEquals("{\"error\":\"CF folder not found\"}", response.getOutputAsString());
    }


    //Valid content fragment returns mapped data
    @Test
    void doGetReturnsFragmentData(AemContext context) throws Exception {
	context.build()
		.resource("/content/dam/wknd/content-fragment-folder")
		.resource("/content/dam/wknd/content-fragment-folder/feature-one")
		.resource("/content/dam/wknd/content-fragment-folder/feature-one/jcr:content/data/master",
			"title", "Feature title",
			"description", "Feature description",
			"imageUrl", "/content/dam/wknd/feature.jpg",
			"redirectPageUrl", "/content/wknd/us/en/feature")
		.commit();

	MockSlingHttpServletRequest request = context.request();
	MockSlingHttpServletResponse response = context.response();

	fixture.doGet(request, response);

	Map<String, Object> responseMap = objectMapper.readValue(
		response.getOutputAsString(),
		new TypeReference<Map<String, Object>>() {
		});

	assertTrue(responseMap.containsKey("data1"));

	List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data1");
	assertEquals(1, data.size());

	Map<String, Object> first = data.get(0);
	assertEquals("feature-one", first.get("name"));
	assertEquals("/content/dam/wknd/content-fragment-folder/feature-one", first.get("path"));
	assertEquals("Feature title", first.get("title"));
	assertEquals("Feature description", first.get("description"));
	assertEquals("/content/dam/wknd/feature.jpg", first.get("imageUrl"));
	assertEquals("/content/wknd/us/en/feature", first.get("redirectPageUrl"));
    }


    //Skips invalid children and applies empty-string defaults
    @Test
    void doGetSkipsJcrContentAndMissingDataNodes(AemContext context) throws Exception {
	context.build()
		.resource("/content/dam/wknd/content-fragment-folder")
		.resource("/content/dam/wknd/content-fragment-folder/jcr:content")
		.resource("/content/dam/wknd/content-fragment-folder/missing-master")
		.resource("/content/dam/wknd/content-fragment-folder/valid-fragment")
		.resource("/content/dam/wknd/content-fragment-folder/valid-fragment/jcr:content/data/master",
			"title", "Only title")
		.commit();

	MockSlingHttpServletRequest request = context.request();
	MockSlingHttpServletResponse response = context.response();

	fixture.doGet(request, response);

	Map<String, Object> responseMap = objectMapper.readValue(
		response.getOutputAsString(),
		new TypeReference<Map<String, Object>>() {
		});

	List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data1");
	assertEquals(1, data.size());

	Map<String, Object> first = data.get(0);
	assertEquals("valid-fragment", first.get("name"));
	assertEquals("Only title", first.get("title"));
	assertEquals("", first.get("description"));
	assertEquals("", first.get("imageUrl"));
	assertEquals("", first.get("redirectPageUrl"));
	assertNotNull(first.get("path"));
    }
}
