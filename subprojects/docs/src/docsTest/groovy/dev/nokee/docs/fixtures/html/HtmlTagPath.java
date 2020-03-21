package dev.nokee.docs.fixtures.html;

import lombok.NonNull;
import lombok.Value;

import java.net.URI;

/**
 * Where a specific HTML tag is from.
 * It track the source file as well as the XPath to the tag itself.
 */
@Value
public class HtmlTagPath {
	@NonNull URI uri;
	@NonNull XPath xpath;
}
