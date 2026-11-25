package com.securehub.core.models;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;

/**
 * Builds the featured policy cards from the Content Fragments selected in the dialog, in authored order.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PolicyCardModel {

    /** Only Content Fragments under this root are read. Must match the dialog rootPath. */
    private static final String POLICY_ROOT = "/content/dam/securehub/policies/";

    private static final int MAX_POLICIES = 4;

    @ValueMapValue
    private String[] fragmentPaths;

    @SlingObject
    private ResourceResolver resourceResolver;

    private final List<Policy> policies = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (fragmentPaths == null || resourceResolver == null) {
            return;
        }
        for (String path : fragmentPaths) {
            if (policies.size() >= MAX_POLICIES) {
                break;
            }
            if (path == null || !path.startsWith(POLICY_ROOT)) {
                continue;
            }
            Resource resource = resourceResolver.getResource(path);
            if (resource == null) {
                continue;
            }
            ContentFragment fragment = resource.adaptTo(ContentFragment.class);
            if (fragment == null) {
                continue;
            }
            policies.add(new Policy(
                    getElement(fragment, "title"),
                    getElement(fragment, "policyId"),
                    getElement(fragment, "category"),
                    getElement(fragment, "effectiveDate"),
                    getElement(fragment, "owner"),
                    getElement(fragment, "summary"),
                    getElement(fragment, "body")));
        }
    }

    private static String getElement(ContentFragment fragment, String name) {
        ContentElement element = fragment.getElement(name);
        if (element == null || element.getContent() == null) {
            return null;
        }
        String value = element.getContent().trim();
        return value.isEmpty() ? null : value;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    /** Values read from one policy Content Fragment. Field names match the Content Fragment element names. */
    public static class Policy {

        private final String title;
        private final String policyId;
        private final String category;
        private final String effectiveDate;
        private final String owner;
        private final String summary;
        private final String body;

        public Policy(String title, String policyId, String category, String effectiveDate, String owner,
                String summary, String body) {
            this.title = title;
            this.policyId = policyId;
            this.category = category;
            this.effectiveDate = effectiveDate;
            this.owner = owner;
            this.summary = summary;
            this.body = body;
        }

        public String getTitle() {
            return title;
        }

        public String getPolicyId() {
            return policyId;
        }

        public String getCategory() {
            return category;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public String getOwner() {
            return owner;
        }

        public String getSummary() {
            return summary;
        }

        /** Rich text body, rendered as HTML by the component. */
        public String getBody() {
            return body;
        }
    }
}
