package com.securehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class)
public class HeroModel {

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String title;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String description;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String fileReference;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaText;

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private String ctaLink;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFileReference() {
        return fileReference;
    }

    public String getCtaText() {
        return ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }

    /** CTA href: page paths get .html appended; DAM paths, links with an extension and absolute URLs are kept. */
    public String getCtaUrl() {
        if (ctaLink == null || ctaLink.trim().isEmpty()) {
            return null;
        }
        String link = ctaLink.trim();
        String lastSegment = link.substring(link.lastIndexOf('/') + 1);
        if (link.startsWith("/content/") && !link.startsWith("/content/dam/") && !lastSegment.contains(".")) {
            return link + ".html";
        }
        return link;
    }
}
