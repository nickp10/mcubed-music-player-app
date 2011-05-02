package dev.paddock.adp.mCubed.utilities;

import junit.framework.TestCase;

public class XMLDocumentTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testParseFromNoFormat() {
		// Specify the XML that should be read
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<AppState Chars=\"SPECIAL&quot;&apos;=&amp;&lt;&gt;/\\CHARS\">" +
				"<Child Time=\"1000\">" +
				"<Text>Blizzard</Text>" +
				"</Child>" +
				"<Empty/>" +
				"<Att Rribute=\"\"/>" +
				"</AppState>";
		
		// Read the xml
		XMLDocument document = XMLDocument.read(xml);
		
		// Assert the root node is good
		assertEquals("AppState", document.getNodeName());
		assertEquals("SPECIAL\"'=&<>/\\CHARS", document.getAttribute("Chars"));
		assertNull(document.getAttribute("Unknown"));
		assertNull(document.getNodeText());
		assertNull(document.getParentNode());
		assertEquals(document, document.getRootNode());
		
		// Assert the child node is good
		XMLNode childNode = document.getChildNode("Child");
		assertNotNull(childNode);
		assertEquals("Child", childNode.getNodeName());
		assertEquals("1000", childNode.getAttribute("Time"));
		assertNull(childNode.getAttribute("Unknown"));
		assertNull(childNode.getNodeText());
		assertEquals(document, childNode.getParentNode());
		assertEquals(document, childNode.getRootNode());
		
		// Assert the child node's text node is good
		XMLNode textNode = childNode.getChildNode("Text");
		assertNotNull(textNode);
		assertEquals("Text", textNode.getNodeName());
		assertNull(textNode.getAttribute("Unknown"));
		assertEquals("Blizzard", textNode.getNodeText());
		assertEquals(childNode, textNode.getParentNode());
		assertEquals(document, textNode.getRootNode());
		
		// Assert the empty node is good
		XMLNode emptyNode = document.getChildNode("Empty");
		assertNotNull(emptyNode);
		assertEquals("Empty", emptyNode.getNodeName());
		assertNull(emptyNode.getAttribute("Unknown"));
		assertEquals("", emptyNode.getNodeText());
		assertEquals(document, emptyNode.getParentNode());
		assertEquals(document, emptyNode.getRootNode());
		
		// Assert the att node is good
		XMLNode attNode = document.getChildNode("Att");
		assertNotNull(attNode);
		assertEquals("Att", attNode.getNodeName());
		assertEquals("", attNode.getAttribute("Rribute"));
		assertNull(attNode.getAttribute("Unknown"));
		assertEquals("", attNode.getNodeText());
		assertEquals(document, attNode.getParentNode());
		assertEquals(document, attNode.getRootNode());
		
		// ToXML it to make sure the same result is returned
		assertEquals(xml, document.toXML(false));
	}
	
	public void testParseFromFormatted() {
		// Specify the XML that should be read
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<AppState Chars=\"SPECIAL&quot;&apos;=&amp;&lt;&gt;/\\CHARS\">\n" +
				"\t<Child Time=\"1000\">\n" +
				"\t\t<Text>Blizzard</Text>\n" +
				"\t</Child>\n" +
				"\t<Empty/>\n" +
				"\t<Att Rribute=\"\"/>\n" +
				"</AppState>";
		
		// Read the xml
		XMLDocument document = XMLDocument.read(xml);
		
		// Assert the root node is good
		assertEquals("AppState", document.getNodeName());
		assertEquals("SPECIAL\"'=&<>/\\CHARS", document.getAttribute("Chars"));
		assertNull(document.getAttribute("Unknown"));
		assertNull(document.getNodeText());
		assertNull(document.getParentNode());
		assertEquals(document, document.getRootNode());
		
		// Assert the child node is good
		XMLNode childNode = document.getChildNode("Child");
		assertNotNull(childNode);
		assertEquals("Child", childNode.getNodeName());
		assertEquals("1000", childNode.getAttribute("Time"));
		assertNull(childNode.getAttribute("Unknown"));
		assertNull(childNode.getNodeText());
		assertEquals(document, childNode.getParentNode());
		assertEquals(document, childNode.getRootNode());
		
		// Assert the child node's text node is good
		XMLNode textNode = childNode.getChildNode("Text");
		assertNotNull(textNode);
		assertEquals("Text", textNode.getNodeName());
		assertNull(textNode.getAttribute("Unknown"));
		assertEquals("Blizzard", textNode.getNodeText());
		assertEquals(childNode, textNode.getParentNode());
		assertEquals(document, textNode.getRootNode());
		
		// Assert the empty node is good
		XMLNode emptyNode = document.getChildNode("Empty");
		assertNotNull(emptyNode);
		assertEquals("Empty", emptyNode.getNodeName());
		assertNull(emptyNode.getAttribute("Unknown"));
		assertEquals("", emptyNode.getNodeText());
		assertEquals(document, emptyNode.getParentNode());
		assertEquals(document, emptyNode.getRootNode());
		
		// Assert the att node is good
		XMLNode attNode = document.getChildNode("Att");
		assertNotNull(attNode);
		assertEquals("Att", attNode.getNodeName());
		assertEquals("", attNode.getAttribute("Rribute"));
		assertNull(attNode.getAttribute("Unknown"));
		assertEquals("", attNode.getNodeText());
		assertEquals(document, attNode.getParentNode());
		assertEquals(document, attNode.getRootNode());
		
		// ToXML it to make sure the same result is returned
		assertEquals(xml, document.toXML(true));
	}

	public void testToXMLNoFormat() {
		// Create a XML document
		XMLNode root = XMLDocument.newDocument("AppState");
		root.setAttribute("Chars", "SPECIAL\"'=&<>/\\CHARS");
		XMLNode child = root.addChildNode("Child");
		child.setAttribute("Time", "1000");
		child.addChildNode("Text").setNodeText("Blizzard");
		root.addChildNode("Empty");
		root.addChildNode("Att").setAttribute("Rribute", null);
		String actual = root.toXML(false);
		
		// Specify the actual XML that should be generated
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<AppState Chars=\"SPECIAL&quot;&apos;=&amp;&lt;&gt;/\\CHARS\">" +
				"<Child Time=\"1000\">" +
				"<Text>Blizzard</Text>" +
				"</Child>" +
				"<Empty/>" +
				"<Att Rribute=\"\"/>" +
				"</AppState>";
		
		// Assert it
		assertEquals(expected, actual);
	}

	public void testToXMLFormatted() {
		// Create a XML document
		XMLNode root = XMLDocument.newDocument("AppState");
		root.setAttribute("Chars", "SPECIAL\"'=&<>/\\CHARS");
		XMLNode child = root.addChildNode("Child");
		child.setAttribute("Time", "1000");
		child.addChildNode("Text").setNodeText("Blizzard");
		root.addChildNode("Empty");
		root.addChildNode("Att").setAttribute("Rribute", null);
		String actual = root.toXML(true);
		
		// Specify the actual XML that should be generated
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
				"<AppState Chars=\"SPECIAL&quot;&apos;=&amp;&lt;&gt;/\\CHARS\">\n" +
				"\t<Child Time=\"1000\">\n" +
				"\t\t<Text>Blizzard</Text>\n" +
				"\t</Child>\n" +
				"\t<Empty/>\n" +
				"\t<Att Rribute=\"\"/>\n" +
				"</AppState>";
		
		// Assert it
		assertEquals(expected, actual);
	}
	
	public void testGetNodePathValue() {
		// Create a XML document
		XMLNode root = XMLDocument.newDocument("AppState");
		root.setAttribute("Version", "2");
		XMLNode child = root.addChildNode("Child");
		child.setAttribute("Time", "1000");
		child.addChildNode("Element").setNodeText("Android");
		root.addChildNode("Second").addChildNode("Element").setAttribute("Attribute", "My Value");
		
		// Assert values
		assertNull(root.getNodePathValue(null));
		assertNull(root.getNodePathValue(""));
		assertNull(root.getNodePathValue("Child"));
		assertEquals("2", root.getNodePathValue("@Version"));
		assertEquals("1000", root.getNodePathValue("Child/@Time"));
		assertEquals("Android", root.getNodePathValue("Child/Element"));
		assertEquals("Android", child.getNodePathValue("Element"));
		assertEquals("My Value", root.getNodePathValue("Second/Element/@Attribute"));
	}
	
	public void testSetNodePathValue() {
		// Create a XML document with SetNodePathValue
		XMLNode root = XMLDocument.newDocument("AppState");
		root.setNodePathValue("Child/Element", "Android");
		root.setNodePathValue("Child/@Time", "1000");
		root.setNodePathValue("@Version", "2");
		root.setNodePathValue("Second/Element/@Attribute", "My Value");
		
		// Assert values
		assertEquals("Android", root.getNodePathValue("Child/Element"));
		assertEquals("1000", root.getNodePathValue("Child/@Time"));
		assertEquals("2", root.getNodePathValue("@Version"));
		assertEquals("My Value", root.getNodePathValue("Second/Element/@Attribute"));
	}
}