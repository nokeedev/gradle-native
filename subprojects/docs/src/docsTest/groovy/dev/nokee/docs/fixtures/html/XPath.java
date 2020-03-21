package dev.nokee.docs.fixtures.html;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Value
public class XPath {
	@NonNull String value;

	/**
	 * Creates an XPath-like instance for a specific HTML node.
	 *
	 * @param e a {@link GPathResult} from {@link XmlSlurper}
	 * @return an XPath instance representing the absolute path with id and class attributes if available, never null.
	 */
	public static XPath of(GPathResult e) {
		List<String> builder = new ArrayList<>();
		NodeChild n = (NodeChild) e;
		while (!n.name().equals("HTML")) {
			List<String> attributes = new ArrayList<>();
			String id = Objects.toString(n.attributes().get("id"), null);
			if (id != null) {
				attributes.add(String.format("@id='%s'", id));
			}
			String classes = Objects.toString(n.attributes().get("class"), null);
			if (classes != null) {
				attributes.add(String.format("@class='%s'", classes));
			}

			String segment = n.name();
			if (!attributes.isEmpty()) {
				segment = String.format("%s[%s]", n.name(), String.join(",", attributes));
			}
			builder.add(0, segment);
			n = (NodeChild)n.parent();
		}
		builder.add(0, n.name());
		return new XPath("/" + String.join("/", builder));
	}
}
