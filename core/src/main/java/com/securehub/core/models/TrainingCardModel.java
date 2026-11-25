package com.securehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class)
public class TrainingCardModel {

    private static final String DEFAULT_CTA_TEXT = "View";

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String courseTitle;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String level;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String duration;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String format;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String fileReference;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaText;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaLink;

    public String getCourseTitle() {
        return courseTitle;
    }

    /** Returns beginner, intermediate or advanced; null when unset or unrecognised. */
    public String getLevel() {
        if ("beginner".equals(level) || "intermediate".equals(level) || "advanced".equals(level)) {
            return level;
        }
        return null;
    }

    /** Readable level text, shown alongside the level colour so colour is not the only cue. */
    public String getLevelLabel() {
        String value = getLevel();
        if (value == null) {
            return null;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public String getDuration() {
        return duration;
    }

    /** Returns self-paced or instructor-led; null when unset or unrecognised. */
    public String getFormat() {
        if ("self-paced".equals(format) || "instructor-led".equals(format)) {
            return format;
        }
        return null;
    }

    public String getFormatLabel() {
        if ("self-paced".equals(getFormat())) {
            return "Self-paced";
        }
        if ("instructor-led".equals(getFormat())) {
            return "Instructor-led";
        }
        return null;
    }

    public String getFileReference() {
        return fileReference;
    }

    /** Action label; defaults to "View" when the author leaves CTA text empty. */
    public String getCtaText() {
        return ctaText == null || ctaText.trim().isEmpty() ? DEFAULT_CTA_TEXT : ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }
}
