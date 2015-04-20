package dev.paddock.adp.mCubed.scrobble;

import dev.paddock.adp.mCubed.utilities.XMLDocument;
import dev.paddock.adp.mCubed.utilities.XMLNode;

public abstract class ScrobbleResponse {
	public static <T extends ScrobbleResponse> T parse(Class<T> clazz, String response) throws ScrobbleException {
		try {
			XMLDocument document = XMLDocument.read(response);
			XMLNode root = document.getRootNode();
			String status = root.getAttribute("status");
			if ("ok".equalsIgnoreCase(status)) {
				T scrobbleResponse = clazz.newInstance();
				scrobbleResponse.parse(root);
				return scrobbleResponse;
			} else {
				XMLNode errorNode = root.getChildNode("error");
				throw new ScrobbleException(errorNode.getAttribute("code"), errorNode.getNodeText().trim());
			}
		} catch (IllegalAccessException e) {
			throw new ScrobbleException(e);
		} catch (InstantiationException e) {
			throw new ScrobbleException(e);
		}
	}

	protected abstract void parse(XMLNode node);
}
