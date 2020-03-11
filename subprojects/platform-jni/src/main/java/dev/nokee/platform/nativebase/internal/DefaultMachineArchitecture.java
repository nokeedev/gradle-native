package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.MachineArchitecture;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.gradle.api.Named;

import static java.util.Arrays.asList;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE) /** Use {@link DefaultMachineArchitecture#forName(String)} instead */
public class DefaultMachineArchitecture implements MachineArchitecture, Named {
	@NonNull String name;

	public static DefaultMachineArchitecture X86 = new DefaultMachineArchitecture("x86");
	public static DefaultMachineArchitecture X86_64 = new DefaultMachineArchitecture("x86-64");
	public static DefaultMachineArchitecture HOST = forName(System.getProperty("os.arch"));

	public static DefaultMachineArchitecture forName(String name) {
		String archName = name.toLowerCase();
		if (asList("x86", "i386", "ia-32", "i686").contains(archName)) {
			return X86;
		} else if (asList("x86-64", "x86_64", "amd64", "x64").contains(archName)) {
			return X86_64;
		} else {
			throw new UnsupportedOperationException("Unsupported architecture of name '" + archName + "'");
		}
	}
}
