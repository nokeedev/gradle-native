package dev.nokee.docs.fixtures.html;

import lombok.*;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.Optional;

@ToString
@AllArgsConstructor
public abstract class HtmlTagFixture {
	@Getter private HtmlTagPath path;

	public void assertBob() {
		System.out.println(path);
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class A extends HtmlTagFixture {
		String href;

		public Optional<String> getHref() {
			return Optional.ofNullable(href);
		}

		public A(HtmlTagPath path, String href) {
			super(path);
			this.href = href;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Img extends HtmlTagFixture {
		String src;
		String altText;

		public Optional<String> getSrc() {
			return Optional.ofNullable(src);
		}

		public Optional<String> getAltText() {
			return Optional.ofNullable(altText);
		}

		public Img(HtmlTagPath path, String src, String altText) {
			super(path);
			this.src = src;
			this.altText = altText;
		}

		public void assertHasAltText() {
			Assert.assertThat(altText, Matchers.notNullValue());
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Script extends HtmlTagFixture {
		String src;

		public Optional<String> getSrc() {
			return Optional.ofNullable(src);
		}

		public Script(HtmlTagPath path, String src) {
			super(path);
			this.src = src;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Link extends HtmlTagFixture {
		String href;

		public Optional<String> getHref() {
			return Optional.ofNullable(href);
		}

		public Link(HtmlTagPath path, String href) {
			super(path);
			this.href = href;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class HtmlAnchor extends HtmlTagFixture {
		String id;

		public HtmlAnchor(HtmlTagPath path, String id) {
			super(path);
			this.id = id;
		}
	}
}
