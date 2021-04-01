package dev.gradleplugins.grava.util;

import org.gradle.api.specs.Spec;

import static dev.gradleplugins.grava.util.SpecUtils.and;

class SpecUtils_AndTest implements SpecAndTester {
	@Override
	public <T> Spec<T> createAndSpec(SpecUtils.Spec<T> first, Spec<? super T> second) {
		return and(first, second);
	}
}
