package dev.nokee.docs.fixtures.html;

import lombok.*;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.Optional;

@ToString
@AllArgsConstructor
public abstract class HtmlTagFixture {
	@Getter
	private HtmlTagPath path;

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
		String type;
		String text;

		public Optional<String> getSrc() {
			return Optional.ofNullable(src);
		}

		public Optional<String> getType() {
			return Optional.ofNullable(type);
		}

		public Optional<String> getText() {
			return Optional.ofNullable(text);
		}

		public Script(HtmlTagPath path, String src, String type, String text) {
			super(path);
			this.src = src;
			this.type = type;
			this.text = text;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Link extends HtmlTagFixture {
		String href;
		@NonNull String rel;

		public Optional<String> getHref() {
			return Optional.ofNullable(href);
		}

		public boolean isCanonical() {
			return rel.equals("canonical");
		}

		public Link(HtmlTagPath path, String href, String rel) {
			super(path);
			this.href = href;
			this.rel = rel;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Meta extends HtmlTagFixture {
		String nameOrProperty;
		String content;

		public boolean isOpenGraphUrl() {
			return nameOrProperty != null && nameOrProperty.equals("og:url");
		}

		public boolean isDescription() {
			return nameOrProperty != null && nameOrProperty.equals("description");
		}

		public boolean isKeywords() {
			return nameOrProperty != null && nameOrProperty.equals("keywords");
		}

		public boolean isTwitterDescription() {
			return nameOrProperty != null && nameOrProperty.equals("twitter:description");
		}

		public boolean isTwitterImage() {
			return nameOrProperty != null && nameOrProperty.equals("twitter:image");
		}

		public boolean isTwitterCard() {
			return nameOrProperty != null && nameOrProperty.equals("twitter:card");
		}

		public boolean isTwitterPlayer() {
			return nameOrProperty != null && nameOrProperty.equals("twitter:player");
		}

		public Meta(HtmlTagPath path, String nameOrProperty, String content) {
			super(path);
			this.nameOrProperty = nameOrProperty;
			this.content = content;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class HtmlAnchor extends HtmlTagFixture {
		String idOrName;

		public HtmlAnchor(HtmlTagPath path, String idOrName) {
			super(path);
			this.idOrName = idOrName;
		}
	}
}
