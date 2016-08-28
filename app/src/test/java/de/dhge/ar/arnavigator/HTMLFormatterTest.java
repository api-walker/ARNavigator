package de.dhge.ar.arnavigator;

import org.junit.Test;

import de.dhge.ar.arnavigator.util.ContentParser;
import de.dhge.ar.arnavigator.util.HTMLFormatter;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HTMLFormatterTest {
    private final String specXML = "<?xml version=\"1.0\"?><QRContent><Meta><type>ROOM</type><name>Labor</name><id>1</id><content><![CDATA[<b>Bold</b>]]></content></Meta></QRContent>";
    private final String specXMLWithRawContent = "<?xml version=\"1.0\"?><QRContent><Meta><type>ROOM</type><name>Labor</name><id>1</id><content raw=\"true\"><![CDATA[<b>Bold</b>]]></content></Meta></QRContent>";

    @Test
    public void prettyPrint() throws Exception {
        HTMLFormatter htmlFormatter = new HTMLFormatter("Hello World!");
        assertEquals("<html><body style=\"text-align:center;\"><p style=\"color: white;background-color: none;font-size: 20pt;\">Hello World!</p></body></html>", htmlFormatter.prettyPrint("white", "none", "20pt", ""));
    }

    @Test
    public void getWebsite() throws Exception {
        HTMLFormatter htmlFormatter = new HTMLFormatter("Hello World!");
        assertEquals("<html><body style=\"text-align:center;\">Hello World!</body></html>", htmlFormatter.getWebSite());
    }
}