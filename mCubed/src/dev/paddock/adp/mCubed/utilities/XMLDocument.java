package dev.paddock.adp.mCubed.utilities;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XMLDocument extends XMLNode {
	private String xmlVersion = "1.0", xmlEncoding = "UTF-8", xmlStandalone = "yes";
	
	public static XMLDocument read(String xml) {
		if (Utilities.isNullOrEmpty(xml)) {
			return null;
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			Document document = builder.parse(stream);
			stream.close();
			document.normalize();
			return new XMLDocument(document);
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public static XMLDocument newDocument(String rootNodeName) {
		if (Utilities.isNullOrEmpty(rootNodeName)) {
			return null;
		}
		return new XMLDocument(rootNodeName);
	}
	
	private XMLDocument(Document document) {
		super(null, document.getDocumentElement());
	}
	
	private XMLDocument(String rootNodeName) {
		super(null, rootNodeName);
	}
	
	public String getXMLVersion() {
		return xmlVersion;
	}
	public void setXMLVersion(String xmlVersion) {
		this.xmlVersion = xmlVersion;
	}
	
	public String getXMLEncoding() {
		return xmlEncoding;
	}
	public void setXMLEncoding(String xmlEncoding) {
		this.xmlEncoding = xmlEncoding;
	}

	public String getXMLStandalone() {
		return xmlStandalone;
	}
	public void setXMLStandalone(String xmlStandalone) {
		this.xmlStandalone = xmlStandalone;
	}
	
	@Override
	public String toXML(boolean format) {
		String declaration = String.format("<?xml version=\"%s\" encoding=\"%s\" standalone=\"%s\"?>", xmlVersion, xmlEncoding, xmlStandalone);
		String newLine = format ? "\n" : "";
		return declaration + newLine + super.toXML(format);
	}
}