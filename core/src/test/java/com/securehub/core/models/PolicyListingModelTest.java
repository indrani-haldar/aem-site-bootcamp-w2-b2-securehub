package com.securehub.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.securehub.core.testcontext.AppAemContext;
import com.adobe.cq.dam.cfm.ContentFragment;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PolicyListingModelTest {

    private static final String CF_ROOT = "/content/dam/securehub/policies/";
    private static final String COMPONENT = "/content/securehub/en/jcr:content/policy-listing";
    private static final String DETAILS_PAGE = "/content/securehub/us/en/policies";

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(PolicyListingModel.class);
        context.create().contentFragmentStructured(CF_ROOT + "access-policy", Map.<String, Object>of(
                "title", "Access Control Policy",
                "category", "Access",
                "owner", "IT Security",
                "effectiveDate", "2026-01-15",
                "reviewDate", "2027-01-15",
                "version", "2.1"));
        context.create().contentFragmentStructured(CF_ROOT + "data-policy", Map.<String, Object>of(
                "title", "Data Handling Policy",
                "category", "Data Privacy"));
    }

    @Test
    void mapsFragmentsInAuthoredOrderWithDetailLinks() {
        Resource component = context.create().resource(COMPONENT,
                "detailsPage", DETAILS_PAGE,
                "fragmentPaths", new String[] { CF_ROOT + "access-policy", CF_ROOT + "data-policy" });

        PolicyListingModel model = component.adaptTo(PolicyListingModel.class);
        List<PolicyListingModel.Policy> policies = model.getPolicies();

        assertEquals(2, model.getCount());
        PolicyListingModel.Policy first = policies.get(0);
        assertEquals("Access Control Policy", first.getTitle());
        assertEquals("Access", first.getCategory());
        assertEquals("access", first.getCategoryKey());
        assertEquals("IT Security", first.getOwner());
        assertEquals("2026-01-15", first.getEffectiveDate());
        assertEquals("2027-01-15", first.getReviewDate());
        assertEquals("2.1", first.getVersion());
        assertEquals(DETAILS_PAGE + ".html#policy-access-policy", first.getDetailLink());
        assertEquals("data-privacy", policies.get(1).getCategoryKey());
    }

    @Test
    void skipsMissingAndOutOfRootFragmentsAndKeepsMissingElementsNull() {
        context.create().contentFragmentStructured("/content/dam/securehub/advisories/other", Map.<String, Object>of(
                "title", "Not a policy"));
        Resource component = context.create().resource(COMPONENT,
                "fragmentPaths", new String[] {
                    CF_ROOT + "missing",
                    "/content/dam/securehub/advisories/other",
                    CF_ROOT + "data-policy" });

        PolicyListingModel model = component.adaptTo(PolicyListingModel.class);

        assertEquals(1, model.getCount());
        PolicyListingModel.Policy policy = model.getPolicies().get(0);
        assertEquals("Data Handling Policy", policy.getTitle());
        assertNull(policy.getOwner());
        assertNull(policy.getVersion());
        assertEquals("#policy-data-policy", policy.getDetailLink());
    }

    @Test
    void loadsFragmentFromResourceWithoutRequiringAssetAdapter() {
        ResourceResolver resolver = mock(ResourceResolver.class);
        Resource resource = mock(Resource.class);
        ContentFragment fragment = mock(ContentFragment.class);
        String path = CF_ROOT + "data-handling-standard";
        when(resolver.getResource(path)).thenReturn(resource);
        when(resource.adaptTo(ContentFragment.class)).thenReturn(fragment);

        assertSame(fragment, ContentFragmentSupport.load(resolver, path, CF_ROOT));
    }

    @Test
    void emptySelectionYieldsNoPolicies() {
        Resource component = context.create().resource(COMPONENT);

        PolicyListingModel model = component.adaptTo(PolicyListingModel.class);

        assertEquals(0, model.getCount());
        assertTrue(model.getPolicies().isEmpty());
    }
}
