package dev.gradleplugins.dockit.dsl.docbook;

public interface Renderable {
	default <T> T render(Renderer<T> renderer) {
		return renderer.render(this);
	}
}
