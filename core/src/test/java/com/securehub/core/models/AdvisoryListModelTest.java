package com.securehub.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.securehub.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class AdvisoryListModelTest {

    private static final String CF_ROOT = "/content/dam/securehub/advisories/";
    private static final String COMPONENT = "/content/securehub/en/jcr:content/advisory-list";
    private static final String DETAILS_PAGE = "/content/securehub/us/en/security-advisories";

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(AdvisoryListModel.class);
        for (int i = 1; i <= 5; i++) {
            context.create().contentFragmentStructured(CF_ROOT + "adv" + i, Map.<String, Object>of(
                    "title", "Advisory " + i,
                    "severity", i == 1 ? "High" : "Low",
                    "status", "In Progress",
                    "recommendedAction", "<p>Patch now</p>"));
        }
    }

    @Test
    void selectedFragmentsAreLimitedToFourInAuthoredOrder() {
        Resource component = context.create().resource(COMPONENT,
                "heading", "Latest Advisories",
                "detailsPage", DETAILS_PAGE,
                "advisoryPaths", new String[] {
                    CF_ROOT + "adv1", CF_ROOT + "adv2", CF_ROOT + "adv3", CF_ROOT + "adv4", CF_ROOT + "adv5" });

        AdvisoryListModel model = component.adaptTo(AdvisoryListModel.class);
        List<AdvisoryListModel.Advisory> advisories = model.getAdvisories();

        assertEquals("Latest Advisories", model.getHeading());
        assertEquals(4, advisories.size());
        AdvisoryListModel.Advisory first = advisories.get(0);
        assertEquals("Advisory 1", first.getTitle());
        assertEquals("critical", first.getSeverityGroup());
        assertEquals("info", advisories.get(1).getSeverityGroup());
        assertEquals("in-progress", first.getStatusKey());
        assertEquals("<p>Patch now</p>", first.getRecommendedAction());
        assertEquals(DETAILS_PAGE + ".html#advisory-adv1", first.getDetailLink());
    }

    @Test
    void showsAllFragmentsInFolderWhenNoneSelected() {
        Resource component = context.create().resource(COMPONENT);

        assertEquals(5, component.adaptTo(AdvisoryListModel.class).getCount());
    }

    @Test
    void ignoresSelectedFragmentsOutsideAdvisoriesFolder() {
        context.create().contentFragmentStructured("/content/dam/securehub/policies/p1",
                Map.<String, Object>of("title", "Policy 1"));
        Resource component = context.create().resource(COMPONENT,
                "advisoryPaths", new String[] { "/content/dam/securehub/policies/p1", CF_ROOT + "adv2" });

        List<AdvisoryListModel.Advisory> advisories = component.adaptTo(AdvisoryListModel.class).getAdvisories();

        assertEquals(1, advisories.size());
        assertEquals("Advisory 2", advisories.get(0).getTitle());
        assertNull(advisories.get(0).getDetailLink());
    }
}
