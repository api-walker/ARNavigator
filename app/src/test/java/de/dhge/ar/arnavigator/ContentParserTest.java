package de.dhge.ar.arnavigator;

import org.junit.Test;

import de.dhge.ar.arnavigator.util.ContentParser;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ContentParserTest {
    private final String wrongContent = "123456789";
    private final String specXML = "<?xml version=\"1.0\"?><QRContent><Meta><type>ROOM</type><name>Labor</name><id>1</id><content><![CDATA[<b>Bold</b>]]></content></Meta></QRContent>";
    private final String specXMLWithRawContent = "<?xml version=\"1.0\"?><QRContent><Meta><type>ROOM</type><name>Labor</name><id>1</id><content raw=\"true\"><![CDATA[<b>Bold</b>]]></content></Meta></QRContent>";

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
    public void isRawContent() throws Exception {
        ContentParser cp = new ContentParser(specXMLWithRawContent);
        assertEquals(true, cp.isRawContent());
    }

    @Test
    public void isValidContent() throws Exception {
        // Worst case
        ContentParser cp = new ContentParser(wrongContent);
        assertEquals(false, cp.isValidContent());

        // Best case
        cp = new ContentParser(specXMLWithRawContent);
        assertEquals(true, cp.isValidContent());
    }

    @Test
    public void isRawContentWithoutAttribute() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals(false, cp.isRawContent());
    }

    @Test
    public void getCustomNode() throws Exception {
        ContentParser cp = new ContentParser(specXML);
        assertEquals("<b>Bold</b>", cp.getCustomNode("content"));
    }
}