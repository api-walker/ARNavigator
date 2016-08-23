package de.dhge.ar.arnavigator;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ContentParserTest {
    private final String specXML = "<?xml version=\"1.0\"?><QRContent><Meta><type>ROOM</type><name>Labor</name><id>1</id><content><![CDATA[<b>Bold</b>]]></content></Meta></QRContent>";

    @Test
    public void getType() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("ROOM", cp.getType());
    }

    @Test
    public void getName() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("Labor", cp.getName());
    }

    @Test
    public void getID() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("1", cp.getID());
    }

    @Test
    public void getContent() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("<b>Bold</b>", cp.getContent());
    }

    @Test
    public void getCustomNode() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("<b>Bold</b>", cp.getCustomNode("content"));
    }
}