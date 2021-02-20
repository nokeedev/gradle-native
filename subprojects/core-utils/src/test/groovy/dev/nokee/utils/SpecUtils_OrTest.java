package dev.nokee.utils;

import org.gradle.api.specs.Spec;

import static dev.nokee.utils.SpecUtils.or;

class SpecUtils_OrTest implements SpecOrTester {
	@Override
	public <T> Spec<T> createOrSpec(SpecUtils.Spec<T> first, org.gradle.api.specs.Spec<? super T> second) {
		return or(first, second);
	}
}
