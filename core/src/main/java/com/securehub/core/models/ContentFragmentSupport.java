package com.securehub.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;

/**
 * Shared helpers for reading Content Fragments selected through a path field.
 */
final class ContentFragmentSupport {

    private ContentFragmentSupport() {
    }

    /**
     * Loads the Content Fragment at the given path, or returns null when the path is blank, lies outside the allowed
     * root, or does not point to a Content Fragment.
     */
    static ContentFragment load(ResourceResolver resolver, String path, String allowedRoot) {
        if (resolver == null || path == null || !path.startsWith(allowedRoot)) {
            return null;
        }
        Resource resource = resolver.getResource(path);
        if (resource == null) {
            return null;
        }
        return resource.adaptTo(ContentFragment.class);
    }

    /** Returns the trimmed text of a Content Fragment element, or null when it is missing or empty. */
    static String text(ContentFragment fragment, String elementName) {
        ContentElement element = fragment.getElement(elementName);
        return element == null ? null : trimToNull(element.getContent());
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
