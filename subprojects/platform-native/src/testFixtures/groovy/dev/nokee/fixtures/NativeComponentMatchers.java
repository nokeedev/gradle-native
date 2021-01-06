package dev.nokee.fixtures;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

public class NativeComponentMatchers {
	// TODO: Generalize this matcher when base name is better supported by every binaries/variants
	public static Matcher<Component> hasArtifactBaseNameOf(String name) {
		return new FeatureMatcher<Component, Iterable<String>>(everyItem(equalTo(name)), "has artifact base name of", "base name of") {
			@Override
			protected Iterable<String> featureValueOf(Component actual) {
				return ((BinaryAwareComponent) actual).getBinaries().filter(BaseNativeBinary.class::isInstance).get().stream().map(it -> ((BaseNativeBinary) it).getBaseName().get()).collect(Collectors.toList());
			}
		};
	}
}
