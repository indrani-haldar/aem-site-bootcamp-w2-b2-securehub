package com.securehub.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.dam.cfm.ContentFragment;

/**
 * Builds the policy cards from the Policy Content Fragments selected in the dialog, in authored order.
 * Missing or invalid selections are skipped, and missing Content Fragment elements are left null.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PolicyListingModel {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyListingModel.class);

    /** Only Content Fragments under this root are read. Must match the dialog rootPath. */
    static final String POLICY_ROOT = "/content/dam/securehub/policies/";

    @ValueMapValue
    private String[] fragmentPaths;

    @ValueMapValue
    private String detailsPage;

    @SlingObject
    private Resource resource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private final List<Policy> policies = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (fragmentPaths == null || resourceResolver == null) {
            return;
        }
        String linkPrefix = toLinkPrefix();
        for (String path : fragmentPaths) {
            ContentFragment fragment = ContentFragmentSupport.load(resourceResolver, path, POLICY_ROOT);
            if (fragment != null) {
                policies.add(new Policy(getName(path), fragment, linkPrefix));
            } else {
                LOG.warn("Skipping unavailable or invalid policy Content Fragment '{}' selected on '{}'",
                        path, resource.getPath());
            }
        }
    }

    /** Returns the policy detail page URL prefix, or an empty string so the link targets the card on this page. */
    private String toLinkPrefix() {
        if (detailsPage == null || !detailsPage.startsWith("/content/")) {
            return "";
        }
        return detailsPage + ".html";
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public int getCount() {
        return policies.size();
    }

    /** Prefix for element ids, unique per component instance. */
    public String getId() {
        return "policy-listing-" + Integer.toHexString(resource == null ? 0 : resource.getPath().hashCode());
    }

    /** Values read from one policy Content Fragment. */
    public static class Policy {

        private final String name;
        private final String title;
        private final String category;
        private final String owner;
        private final String effectiveDate;
        private final String reviewDate;
        private final String version;
        private final String detailLink;

        Policy(String name, ContentFragment fragment, String linkPrefix) {
            this.name = name;
            String fragmentTitle = ContentFragmentSupport.text(fragment, "title");
            this.title = fragmentTitle == null ? name : fragmentTitle;
            this.category = ContentFragmentSupport.text(fragment, "category");
            this.owner = ContentFragmentSupport.text(fragment, "owner");
            this.effectiveDate = ContentFragmentSupport.text(fragment, "effectiveDate");
            this.reviewDate = ContentFragmentSupport.text(fragment, "reviewDate");
            this.version = ContentFragmentSupport.text(fragment, "version");
            this.detailLink = linkPrefix + "#" + getAnchor();
        }

        /** Element id of the card, used as the "Read policy" anchor. */
        public String getAnchor() {
            return "policy-" + name;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        /** Lower-case, hyphenated category used by the category tabs to match cards (for example "data-privacy"). */
        public String getCategoryKey() {
            return category == null ? null : category.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", "-");
        }

        public String getOwner() {
            return owner;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public String getReviewDate() {
            return reviewDate;
        }

        public String getVersion() {
            return version;
        }

        public String getDetailLink() {
            return detailLink;
        }
    }
}
