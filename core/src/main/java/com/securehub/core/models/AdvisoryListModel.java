package com.securehub.core.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
 * Builds the advisory list from Advisory Content Fragments.
 * When the author selects fragments, up to 4 of them are shown in authored order;
 * otherwise every fragment under the advisories folder is shown, newest first.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AdvisoryListModel {

    /** Only Content Fragments under this root are read. Must match the dialog rootPath. */
    static final String ADVISORY_ROOT = "/content/dam/securehub/advisories";

    private static final int MAX_SELECTED = 4;

    @ValueMapValue
    private String heading;

    @ValueMapValue
    private String[] advisoryPaths;

    @ValueMapValue
    private String detailsPage;

    @SlingObject
    private Resource resource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private final List<Advisory> advisories = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (resourceResolver == null) {
            return;
        }
        String detailsLink = toDetailsLink();
        if (advisoryPaths != null && advisoryPaths.length > 0) {
            for (String path : advisoryPaths) {
                if (advisories.size() >= MAX_SELECTED) {
                    break;
                }
                if (path != null && path.startsWith(ADVISORY_ROOT + "/")) {
                    addAdvisory(resourceResolver.getResource(path), detailsLink);
                }
            }
        } else {
            collectAll(resourceResolver.getResource(ADVISORY_ROOT), detailsLink);
            advisories.sort(Comparator.comparingLong(Advisory::getPublishTime).reversed());
        }
    }

    /** Adds every Content Fragment in the folder and its sub-folders. */
    private void collectAll(Resource folder, String detailsLink) {
        if (folder == null) {
            return;
        }
        for (Resource child : folder.getChildren()) {
            if (!addAdvisory(child, detailsLink) && !"jcr:content".equals(child.getName())) {
                collectAll(child, detailsLink);
            }
        }
    }

    private boolean addAdvisory(Resource candidate, String detailsLink) {
        ContentFragment fragment = candidate == null ? null : candidate.adaptTo(ContentFragment.class);
        if (fragment == null) {
            return false;
        }
        advisories.add(new Advisory(candidate.getName(), fragment, detailsLink));
        return true;
    }

    /** Returns the "View details" page URL, or null when it is not set or is the page this component is on. */
    private String toDetailsLink() {
        if (detailsPage == null || !detailsPage.startsWith("/content/")) {
            return null;
        }
        if (resource != null && resource.getPath().startsWith(detailsPage + "/jcr:content")) {
            return null;
        }
        return detailsPage + ".html";
    }

    public String getHeading() {
        return heading;
    }

    public List<Advisory> getAdvisories() {
        return advisories;
    }

    public int getCount() {
        return advisories.size();
    }

    /** Prefix for element ids, unique per component instance. */
    public String getId() {
        return "advisory-list-" + Integer.toHexString(resource == null ? 0 : resource.getPath().hashCode());
    }

    /** Values read from one advisory Content Fragment. Field names match the Content Fragment element names. */
    public static class Advisory {

        private final String name;
        private final String title;
        private final String severity;
        private final String status;
        private final String affectedSystems;
        private final String summary;
        private final String recommendedAction;
        private final Calendar publishCalendar;
        private final String publishDateText;
        private final String detailLink;

        Advisory(String name, ContentFragment fragment, String detailsLink) {
            String elementTitle = text(fragment, "title");
            this.name = name;
            this.title = elementTitle != null ? elementTitle : fragment.getTitle();
            this.severity = text(fragment, "severity");
            this.status = text(fragment, "status");
            this.affectedSystems = text(fragment, "affectedSystems");
            this.summary = text(fragment, "summary");
            this.recommendedAction = text(fragment, "recommendedAction");
            this.publishCalendar = calendar(fragment, "publishDate");
            this.publishDateText = text(fragment, "publishDate");
            this.detailLink = detailsLink == null ? null : detailsLink + "#" + getAnchor();
        }

        private static String text(ContentFragment fragment, String elementName) {
            ContentElement element = fragment.getElement(elementName);
            if (element == null || element.getContent() == null) {
                return null;
            }
            String value = element.getContent().trim();
            return value.isEmpty() ? null : value;
        }

        private static Calendar calendar(ContentFragment fragment, String elementName) {
            ContentElement element = fragment.getElement(elementName);
            try {
                Object value = element == null || element.getValue() == null ? null : element.getValue().getValue();
                return value instanceof Calendar ? (Calendar) value : null;
            } catch (UnsupportedOperationException e) {
                // Typed values are not available for every fragment implementation; the text value is used instead.
                return null;
            }
        }

        private String format(String pattern) {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
            format.setTimeZone(publishCalendar.getTimeZone());
            return format.format(publishCalendar.getTime());
        }

        /** Element id of the card, used as the target of "View details" links. */
        public String getAnchor() {
            return "advisory-" + name;
        }

        public String getTitle() {
            return title;
        }

        public String getSeverity() {
            return severity;
        }

        /**
         * Filter group: "critical" or "info". The Advisory model labels the stored value "High" as Critical.
         */
        public String getSeverityGroup() {
            String value = severity == null ? "" : severity.trim().toLowerCase(Locale.ENGLISH);
            return "critical".equals(value) || "high".equals(value) ? "critical" : "info";
        }

        public String getSeverityLabel() {
            return "critical".equals(getSeverityGroup()) ? "Critical" : "Info";
        }

        public String getStatus() {
            return status;
        }

        /** Lower-case status used as a CSS modifier, e.g. "in-progress". */
        public String getStatusKey() {
            if (status == null) {
                return "unknown";
            }
            String key = status.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
            return key.isEmpty() ? "unknown" : key;
        }

        /** Display date, e.g. "12 Nov 2026". */
        public String getPublishDate() {
            return publishCalendar != null ? format("d MMM yyyy") : publishDateText;
        }

        /** Machine-readable date for the time element, or null when unknown. */
        public String getPublishDateIso() {
            return publishCalendar != null ? format("yyyy-MM-dd") : null;
        }

        /** Epoch milliseconds used for sorting; 0 when the date is unknown. */
        public long getPublishTime() {
            return publishCalendar != null ? publishCalendar.getTimeInMillis() : 0L;
        }

        /** Rich text from the CF, rendered as HTML by the component. */
        public String getAffectedSystems() {
            return affectedSystems;
        }

        /** Rich text from the CF, rendered as HTML by the component. */
        public String getSummary() {
            return summary;
        }

        /** Rich text from the CF, rendered as HTML by the component. */
        public String getRecommendedAction() {
            return recommendedAction;
        }

        public String getDetailLink() {
            return detailLink;
        }
    }
}
