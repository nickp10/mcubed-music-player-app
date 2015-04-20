package dev.paddock.adp.mCubed.scrobble;

import dev.paddock.adp.mCubed.utilities.XMLNode;

public class MobileSessionResponse extends ScrobbleResponse {
	private String key, name;

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void parse(XMLNode node) {
		name = node.getNodePathValue("session/name");
		key = node.getNodePathValue("session/key");
	}
}
