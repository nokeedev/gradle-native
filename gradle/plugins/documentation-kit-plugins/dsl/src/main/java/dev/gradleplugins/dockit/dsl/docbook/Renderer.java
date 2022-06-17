package dev.gradleplugins.dockit.dsl.docbook;

public interface Renderer<T> {
	T render(Renderable link);
}
