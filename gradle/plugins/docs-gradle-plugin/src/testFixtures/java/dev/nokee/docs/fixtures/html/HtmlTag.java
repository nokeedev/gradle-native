package dev.nokee.docs.fixtures.html;

import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;

import java.net.URI;
import java.util.Objects;

public abstract class HtmlTag<T> {
	public static final HtmlTag<HtmlTagFixture.A> A = new HtmlTag<HtmlTagFixture.A>() {
		@Override
		public boolean is(GPathResult e) {
			return e.name().equals("A");
		}

		@Override
		public HtmlTagFixture.A create(URI uri, NodeChild n) {
			return new HtmlTagFixture.A(new HtmlTagPath(uri, XPath.of(n)), Objects.toString(n.attributes().get("href"), null));
		}
	};

	public static final HtmlTag<HtmlTagFixture.Img> IMG = new HtmlTag<HtmlTagFixture.Img>() {
		@Override
		public boolean is(GPathResult e) {
			return e.name().equals("IMG");
		}

		@Override
		public HtmlTagFixture.Img create(URI uri, NodeChild n) {
			return new HtmlTagFixture.Img(new HtmlTagPath(uri, XPath.of(n)), Objects.toString(n.attributes().get("src"), null), Objects.toString(n.attributes().get("alt"), null));
		}
	};

	public static final HtmlTag<HtmlTagFixture.Script> SCRIPT = new HtmlTag<HtmlTagFixture.Script>() {
		@Override
		public boolean is(GPathResult e) {
			return e.name().equals("SCRIPT");
		}

		@Override
		public HtmlTagFixture.Script create(URI uri, NodeChild n) {
			return new HtmlTagFixture.Script(new HtmlTagPath(uri, XPath.of(n)), Objects.toString(n.attributes().get("src"), null));
		}
	};

	public static final HtmlTag<HtmlTagFixture.Link> LINK = new HtmlTag<HtmlTagFixture.Link>() {
		@Override
		public boolean is(GPathResult e) {
			return e.name().equals("LINK");
		}

		@Override
		public HtmlTagFixture.Link create(URI uri, NodeChild n) {
			return new HtmlTagFixture.Link(new HtmlTagPath(uri, XPath.of(n)), Objects.toString(n.attributes().get("href"), null), n.attributes().get("rel").toString());
		}
	};

	public static final HtmlTag<HtmlTagFixture.Meta> META = new HtmlTag<HtmlTagFixture.Meta>() {
		@Override
		public boolean is(GPathResult e) {
			return e.name().equals("META");
		}

		@Override
		public HtmlTagFixture.Meta create(URI uri, NodeChild n) {
			String nameOrProperty = Objects.toString(n.attributes().get("name"), null);
			if (nameOrProperty == null) {
				nameOrProperty = Objects.toString(n.attributes().get("property"), null);
			}
			return new HtmlTagFixture.Meta(new HtmlTagPath(uri, XPath.of(n)), nameOrProperty, Objects.toString(n.attributes().get("content"), null));
		}
	};

	/**
	 * A HTML tag that can be used as anchor with the URI fragment (e.g. #some-value).
	 */
	public static final HtmlTag<HtmlTagFixture.HtmlAnchor> ANCHOR = new HtmlTag<HtmlTagFixture.HtmlAnchor>() {
		@Override
		public boolean is(GPathResult e) {
			return ((NodeChild)e).attributes().containsKey("id");
		}

		@Override
		public HtmlTagFixture.HtmlAnchor create(URI uri, NodeChild n) {
			return new HtmlTagFixture.HtmlAnchor(new HtmlTagPath(uri, XPath.of(n)), n.attributes().get("id").toString());
		}
	};

	public abstract boolean is(GPathResult e);

	public abstract T create(URI uri, NodeChild n);
}
