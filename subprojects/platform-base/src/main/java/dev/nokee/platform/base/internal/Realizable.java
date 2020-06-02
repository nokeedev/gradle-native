package dev.nokee.platform.base.internal;

public interface Realizable {
	Realizable IDENTITY = () -> {};

	void realize();
}
