package org.mozilla.reference.browser.assist;

class SuggestItem {
    enum Type {
        QWANT_SUGGEST,
        HISTORY
    }

    final Type type;
    final String display_text;

    SuggestItem(Type type, String display_text) {
        this.type = type;
        this.display_text = display_text;
    }
}
