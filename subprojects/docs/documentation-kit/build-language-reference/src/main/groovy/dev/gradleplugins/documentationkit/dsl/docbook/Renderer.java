package dev.gradleplugins.documentationkit.dsl.docbook;

public interface Renderer<T> {
	T render(Renderable link);
}
