package dev.nokee.model.graphdb;

public interface Label {
	String name();

	static Label label(String name) {
		return new DefaultLabel(name);
	}
}
