package dev.nokee.model;

import org.gradle.api.Named;

import javax.inject.Inject;

public class TestProjection implements Named {
	private final String name;

	@Inject
	public TestProjection(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
