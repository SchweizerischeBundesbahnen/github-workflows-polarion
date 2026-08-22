package ch.sbb.polarion.extension.example.xxe;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.StringReader;

public class XxeFixed {

    // ok: polarion-xxe-unsafe-parser — disallow-doctype-decl set
    public Document parseDoc(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // ok: polarion-xxe-unsafe-parser — disallow-doctype-decl set on SAX
    public void parseSax(String xml) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(new StringReader(xml)), null);
    }

    // ok: polarion-xxe-unsafe-parser — ACCESS_EXTERNAL_DTD/SCHEMA emptied, the
    // alternative OWASP documents where DOCTYPE cannot be disabled
    public Document parseDocNoExternalAccess(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // ok: polarion-xxe-unsafe-parser — external entities disabled on StAX
    public void parseStax(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        while (reader.hasNext()) {
            reader.next();
        }
    }

    // ok: polarion-xxe-unsafe-parser — ACCESS_EXTERNAL_DTD emptied on the parser
    // rather than the factory, which is the only SAX route to that property
    public void parseSaxParserLevel(String xml) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        parser.parse(new InputSource(new StringReader(xml)), null);
    }

    // ok: polarion-xxe-unsafe-parser — the constant reached through a static
    // import, so the accepted spelling does not depend on the qualifier
    public Document parseDocStaticImport(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }
}
