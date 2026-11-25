package com.securehub.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * Reads the question and answer pairs authored in the FAQ multifield.
 * Each multifield entry is stored as a child node of {@value #ITEMS_NODE}, so this model reads those children in order.
 */
@Model(adaptables = Resource.class)
public class FAQModel {

    /** Parent node that holds the multifield entries; must match the dialog field name. */
    static final String ITEMS_NODE = "faqItems";
    static final String PROPERTY_QUESTION = "question";
    static final String PROPERTY_ANSWER = "answer";

    @Self
    private Resource resource;

    private List<FaqEntry> items = Collections.emptyList();

    @PostConstruct
    protected void init() {
        Resource container = resource.getChild(ITEMS_NODE);
        if (container == null) {
            return;
        }
        List<FaqEntry> result = new ArrayList<>();
        for (Resource child : container.getChildren()) {
            ValueMap values = child.getValueMap();
            String question = trimToNull(values.get(PROPERTY_QUESTION, String.class));
            String answer = trimToNull(values.get(PROPERTY_ANSWER, String.class));
            // An entry without a question has nothing to show, so it is skipped.
            if (question != null) {
                result.add(new FaqEntry(question, answer));
            }
        }
        items = Collections.unmodifiableList(result);
    }

    /** Unique per component instance, used to link buttons to their panels with aria-controls. */
    public String getId() {
        return "faq-" + Integer.toHexString(resource.getPath().hashCode());
    }

    public boolean isHasItems() {
        return !items.isEmpty();
    }

    public List<FaqEntry> getItems() {
        return items;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** One question and its answer. The answer may be null when the author left it empty. */
    public static final class FaqEntry {

        private final String question;
        private final String answer;

        FaqEntry(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
