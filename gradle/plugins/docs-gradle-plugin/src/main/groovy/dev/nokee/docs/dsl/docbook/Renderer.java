package dev.nokee.docs.dsl.docbook;

public interface Renderer<T> {
	T render(Renderable link);
}
