package dev.nokee.xcode.internal;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Xcode workspace data, typically located in {@literal contents.xcworkspacedata} under Xcode workspace.
 *
 * @see XCWorkspaceDataReader
 * @see XCWorkspaceDataWriter
 */
public final class XCWorkspaceData {
	private final List<XCFileReference> fileRefs;

	private XCWorkspaceData(List<XCFileReference> fileRefs) {
		this.fileRefs = ImmutableList.copyOf(fileRefs);
	}

	public List<XCFileReference> getFileRefs() {
		return fileRefs;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<XCFileReference> fileReferences = new ArrayList<>();

		public Builder fileRef(XCFileReference fileReference) {
			fileReferences.add(fileReference);
			return this;
		}

		public XCWorkspaceData build() {
			return new XCWorkspaceData(fileReferences);
		}
	}
}
