package dev.nokee.platform.ios;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;

public interface IosApplication extends Variant {
	BinaryView<Binary> getBinaries();
}
