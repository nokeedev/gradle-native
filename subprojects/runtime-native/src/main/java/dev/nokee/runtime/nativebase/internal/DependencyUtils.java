package dev.nokee.runtime.nativebase.internal;

import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DependencyUtils {
	private static final Logger LOGGER = Logger.getLogger(DependencyUtils.class.getName());

	public static boolean isFrameworkDependency(ResolvedArtifactResult result) {
		Optional<Attribute<?>> attribute = result.getVariant().getAttributes().keySet().stream().filter(it -> it.getName().equals(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName())).findFirst();
		if (attribute.isPresent()) {
			String v = result.getVariant().getAttributes().getAttribute(attribute.get()).toString();
			if (v.equals(LibraryElements.FRAMEWORK_BUNDLE)) {
				return true;
			}
			return false;
		}
		LOGGER.finest(() -> "No library elements on dependency\n" + result.getVariant().getAttributes().keySet().stream().map(Attribute::getName).collect(Collectors.joining(", ")));
		return false;
	}
}
