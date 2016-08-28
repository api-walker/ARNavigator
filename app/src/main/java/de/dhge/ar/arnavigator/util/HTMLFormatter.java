package de.dhge.ar.arnavigator.util;

/**
 * Helper for unformatted object content.
 * Beautifies raw text.
 */

public class HTMLFormatter {
    final String HTMl_START = "<html><body style=\"text-align:center;\">";
    final String HTML_END = "</body></html>";

    private String text;

    public HTMLFormatter(String rawText) {
        this.text = rawText;
    }

    /*
    * This function formats the raw content with color, background-color, font-size and custom stylesheets
    * @param String color(red)
    * @param String backgroundColor(aliceblue)
    * @param String textSize(20pt)
    * @param String additionalStyleSheet(xxxxxx:xxxxx;)
     */
    public String prettyPrint(String color, String backgroundColor, String textSize, String additionalStyleSheet) {
        String newText = String.format("<p style=\"color: %s;background-color: %s;font-size: %s;%s\">%s</p>", color, backgroundColor, textSize, additionalStyleSheet, this.text);
        return HTMl_START + newText + HTML_END;
    }

    /*
    * This function wraps the raw content into an basic HTML website
     */
    public String getWebSite() {
        return HTMl_START + this.text + HTML_END;
    }
}
