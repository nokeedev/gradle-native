package dev.gradleplugins.exemplarkit.asciidoc;

import lombok.val;

import java.util.regex.Pattern;

final class AsciidocCalloutNormalizer {
	private AsciidocCalloutNormalizer() {}

	public static String normalize(String content) {
		val pattern = Pattern.compile("\\s*(//|#)\\s*(<\\d+>|\\(\\d+\\))\\s*$", Pattern.MULTILINE);
		val matches = pattern.matcher(content);
		return matches.replaceAll("");
	}
}
