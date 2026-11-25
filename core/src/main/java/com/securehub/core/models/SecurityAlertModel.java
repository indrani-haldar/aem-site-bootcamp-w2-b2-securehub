package com.securehub.core.models;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class)
public class SecurityAlertModel {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String severity;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String title;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String summary;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private Calendar publishedDate;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaText;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaLink;

    /** Returns critical, high or info; anything else falls back to info. */
    public String getSeverity() {
        if ("critical".equals(severity) || "high".equals(severity)) {
            return severity;
        }
        return "info";
    }

    public String getSeverityLabel() {
        switch (getSeverity()) {
            case "critical":
                return "Critical";
            case "high":
                return "High";
            default:
                return "Info";
        }
    }

    /** Critical and high alerts are announced assertively; info is a polite status update. */
    public String getAriaRole() {
        return "info".equals(getSeverity()) ? "status" : "alert";
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    /** Human-readable date, for example "Nov 20, 2026". */
    public String getPublishedDate() {
        if (publishedDate == null) {
            return null;
        }
        ZoneId zone = publishedDate.getTimeZone().toZoneId();
        return DISPLAY_DATE.format(publishedDate.toInstant().atZone(zone));
    }

    /** Machine-readable ISO 8601 timestamp for the HTML time element. */
    public String getPublishedDateTime() {
        return publishedDate == null ? null : publishedDate.toInstant().toString();
    }

    public String getCtaText() {
        return ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }
}
