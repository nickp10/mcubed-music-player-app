package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLNode {
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final List<XMLNode> childNodes = new ArrayList<XMLNode>();
	private XMLNode parentNode;
	private String nodeName, nodeText;
	
	public XMLNode(XMLNode parentNode, Element element) {
		// Setup
		this(parentNode, element.getTagName());
		StringBuilder textBuilder = new StringBuilder();
		boolean doSetText = true;
		
		// Add all the attributes
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			setAttribute(attribute.getNodeName(), attribute.getNodeValue());
		}
		
		// Add all the child nodes as appropriate
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				new XMLNode(this, (Element)childNode);
				doSetText = false;
			} else if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				setAttribute(childNode.getNodeName(), childNode.getNodeValue());
			} else if (childNode.getNodeType() == Node.CDATA_SECTION_NODE || childNode.getNodeType() == Node.TEXT_NODE) {
				textBuilder.append(childNode.getNodeValue());
			}
		}
		
		// Set the node text if there were no other elements
		if (doSetText) {
			this.nodeText = textBuilder.toString();
		}
	}
	
	public XMLNode(String nodeName) {
		this(null, nodeName);
	}
	
	public XMLNode(XMLNode parentNode, String nodeName) {
		this.parentNode = parentNode;
		this.nodeName = nodeName;
		if (this.parentNode != null) {
			this.parentNode.childNodes.add(this);
		}
	}
	
	public String getAttribute(String name) {
		if (attributes.containsKey(name)) {
			return attributes.get(name);
		}
		return null;
	}
	public void setAttribute(String name, String value) {
		if (value == null) {
			value = "";
		}
		attributes.put(name, value);
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public String getNodeText() {
		return nodeText;
	}
	public void setNodeText(String nodeText) {
		this.nodeText = nodeText;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public XMLNode getRootNode() {
		if (parentNode == null) {
			return this;
		}
		return parentNode.getRootNode();
	}
	
	public XMLNode getParentNode() {
		return parentNode;
	}
	
	public List<XMLNode> getChildNodes() {
		return childNodes;
	}
	
	public List<XMLNode> getChildNodes(String nodeName) {
		List<XMLNode> nodes = new ArrayList<XMLNode>();
		for (XMLNode node : childNodes) {
			if (node.nodeName.equals(nodeName)) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public XMLNode getChildNode(String nodeName) {
		List<XMLNode> nodes = getChildNodes(nodeName);
		if (nodes.size() == 1) {
			return nodes.get(0);
		}
		return null;
	}
	
	public XMLNode addChildNode(String nodeName) {
		return new XMLNode(this, nodeName);
	}
	
	public void addChildNode(XMLNode node) {
		if (!childNodes.contains(node)) {
			if (node.parentNode != null) {
				node.parentNode.childNodes.remove(node);
			}
			childNodes.add(node);
		}
		node.parentNode = this;
	}
	
	public void removeChildNode(XMLNode node) {
		childNodes.remove(node);
	}
	
	public XMLNode getNodePath(String nodePath, boolean create) {
		// Check the original node path
		if (Utilities.isNullOrEmpty(nodePath)) {
			return this;
		}
		
		// Split on the first forward slash
		int slashIndex = nodePath.indexOf('/');
		String beforeSlash = null, afterSlash = null;
		if (slashIndex > -1) {
			beforeSlash = nodePath.substring(0, slashIndex);
			afterSlash = nodePath.substring(slashIndex + 1);
		} else {
			beforeSlash = nodePath;
		}
		
		// Check the before slash
		if (Utilities.isNullOrEmpty(beforeSlash)) {
			return null;
		}
		
		// If the first character is @, then get the attribute value
		if (beforeSlash.charAt(0) == '@') {
			if (Utilities.isNullOrEmpty(afterSlash)) {
				return this;
			} else {
				return null;
			}
		}
		
		// Get the children nodes, ensuring there's no more than one
		List<XMLNode> childNodes = getChildNodes(beforeSlash);
		if (childNodes.size() > 1) {
			return null;
		}
		
		// Create the node if one doesn't exist
		XMLNode childNode = null;
		if (childNodes.size() == 0) {
			if (create) {
				childNode = addChildNode(beforeSlash);
			} else {
				return null;
			}
		}
		
		// Or use the only node
		else {
			childNode = childNodes.get(0);
		}
		
		// Otherwise, find the child node specified, and recurse
		return childNode == null ? null : childNode.getNodePath(afterSlash, create);
	}
	
	public String getNodePathValue(String nodePath) {
		// Get the node to get the value of
		XMLNode node = getNodePath(nodePath, false);
		if (node == null || Utilities.isNullOrEmpty(nodePath)) {
			return null;
		}
		
		// Split on the last forward slash
		int slashIndex = nodePath.lastIndexOf('/');
		String afterSlash = null;
		if (slashIndex > -1) {
			afterSlash = nodePath.substring(slashIndex + 1);
		} else {
			afterSlash = nodePath;
		}
		
		// Check the after slash
		if (Utilities.isNullOrEmpty(afterSlash)) {
			return null;
		}
		
		// If the first character is @, then get the attribute value
		if (afterSlash.charAt(0) == '@') {
			return node.getAttribute(afterSlash.substring(1));
		}
		
		// Otherwise, get the node text
		return node.getNodeText();
	}
	
	public void setNodePathValue(String nodePath, String value) {
		// Get the node to set the value of
		XMLNode node = getNodePath(nodePath, true);
		if (node == null || Utilities.isNullOrEmpty(nodePath)) {
			return;
		}
		
		// Split on the last forward slash
		int slashIndex = nodePath.lastIndexOf('/');
		String afterSlash = null;
		if (slashIndex > -1) {
			afterSlash = nodePath.substring(slashIndex + 1);
		} else {
			afterSlash = nodePath;
		}
		
		// Check the after slash
		if (Utilities.isNullOrEmpty(afterSlash)) {
			return;
		}
		
		// If the first character is @, then set the attribute value
		if (afterSlash.charAt(0) == '@') {
			node.setAttribute(afterSlash.substring(1), value);
		}
		
		// Otherwise, set the node text
		else {
			node.setNodeText(value);
		}
	}
	
	public String encodeForXML(String value) {
		value = value.replace("&", "&amp;");
		value = value.replace("\"", "&quot;");
		value = value.replace("'", "&apos;");
		value = value.replace("<", "&lt;");
		value = value.replace(">", "&gt;");
		return value;
	}
	
	private String getTabs(boolean format, int indent) {
		if (format && indent > 0) {
			char[] tabs = new char[indent];
			Arrays.fill(tabs, '\t');
			return new String(tabs);
		}
		return "";
	}
	
	public String toXML() {
		return toXML(true);
	}
	
	public String toXML(boolean format) {
		StringBuilder xml = new StringBuilder();
		toXML(xml, format, 0);
		return xml.toString();
	}
	
	private void toXML(StringBuilder xml, boolean format, int indent) {
		xml.append(getTabs(format, indent));
		xml.append("<");
		xml.append(nodeName);
		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			xml.append(" ");
			xml.append(attribute.getKey());
			xml.append("=\"");
			xml.append(encodeForXML(attribute.getValue()));
			xml.append("\"");
		}
		if (childNodes.size() > 0) {
			xml.append(">");
			for (XMLNode childNode : childNodes) {
				if (format) {
					xml.append("\n");
				}
				childNode.toXML(xml, format, indent + 1);
			}
			if (format) {
				xml.append("\n");
			}
			xml.append(getTabs(format, indent));
			xml.append("</");
			xml.append(nodeName);
			xml.append(">");
		} else if (!Utilities.isNullOrEmpty(nodeText)) {
			xml.append(">");
			xml.append(encodeForXML(nodeText));
			xml.append("</");
			xml.append(nodeName);
			xml.append(">");
		} else {
			xml.append("/>");
		}
	}
}