package de.dhge.ar.arnavigator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// This library is for processing the content of a scanned QR Code
public class ContentParser {
    private Document doc;
    private Element metaNode;

    public ContentParser(String qrCodeContent) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(qrCodeContent.getBytes("UTF-8"));
        this.doc = dBuilder.parse(input);
        doc.getDocumentElement().normalize();

        metaNode = (Element) doc.getElementsByTagName("Meta").item(0);
    }

    /**
     * Get type of object
     *
     * @return String type
     */
    public String getType() {
        return getCustomNode("type");
    }

    /**
     * Get name of object
     *
     * @return String name
     */
    public String getName() {
        return getCustomNode("name");
    }

    /**
     * Get id of object
     *
     * @return String id
     */
    public String getID() {
        return getCustomNode("id");
    }

    /**
     * Get content of object (shown in App->Info)
     *
     * @return String content
     */
    public String getContent() {
        return getCustomNode("content");
    }

    /**
     * Get custom meta tag
     *
     * @param name Name of custom tag
     * @return String content of tag
     */
    public String getCustomNode(String name) {
        return metaNode.getElementsByTagName(name).item(0).getTextContent();
    }
}
