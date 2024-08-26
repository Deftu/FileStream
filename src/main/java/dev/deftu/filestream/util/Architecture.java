package dev.deftu.filestream.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public enum Architecture {

    UNKNOWN(ProcessorType.UNKNOWN, "unknown", new HashSet<>()),
    X86(ProcessorType.X86, "x86", createAliases("x86", "amd32", "i386", "i486", "i586", "i686")),
    X86_64(ProcessorType.X64, "x86_64", createAliases("x86_64", "amd64", "x64")),
    ARM(ProcessorType.X86, "arm", createAliases("armv7l", "armv7", "armel", "armle", "armv", "arm")),
    AARCH(ProcessorType.X86, "aarch32", createAliases("aarch32", "arm32")),
    AARCH_64(ProcessorType.X64, "aarch64", createAliases("aarch64", "arm64")),
    MIPS(ProcessorType.X86, "mips", createAliases("misle", "mipsel", "mips")),
    MIPS_64(ProcessorType.X64, "mips64", createAliases("mips64", "mips64el", "mips")),
    PPC(ProcessorType.X86, "ppc", createAliases("ppcel", "ppcle", "powerpc", "ppc")),
    PPC_64(ProcessorType.X64, "ppc64", createAliases("ppc64", "powerpc64", "ppc64el", "ppc64le")),
    S390X(ProcessorType.X64, "s390x", createAliases("s390x", "s390")),
    SPARCV9(ProcessorType.X64, "sparcv9", createAliases("sparcv9", "sparc"));

    public final ProcessorType processorType;
    public final String name;
    public final Set<String> aliases;

    Architecture(ProcessorType processorType, String name, Set<String> aliases) {
        this.processorType = processorType;
        this.name = name;
        this.aliases = aliases;
    }

    private static Set<String> createAliases(String... aliases) {
        return new HashSet<>(Arrays.asList(aliases));
    }

    @NotNull
    public static String raw() {
        String value = rawProcessArchitecture();
        if (value == null) {
            value = rawProcessArchitecture64();
        }

        if (value == null) {
            value = rawArch();
        }

        if (value == null) {
            throw new IllegalStateException("Failed to determine system architecture");
        }

        value = value.toLowerCase();
        if (value.equals("zarch_64")) {
            value = "s390x";
        }

        if (value.equals("ppc64") && Objects.equals(System.getProperty("sun.cpu.endian"), "little")) {
            value = "ppc64le";
        }

        return value;
    }

    @NotNull
    public static Architecture find() {
        Architecture value = Architecture.UNKNOWN;
        String raw = raw();

        for (Architecture architecture : values()) {
            if (architecture.name.equals(raw)) {
                value = architecture;
                break;
            }
        }

        if (value == Architecture.UNKNOWN) {
            for (Architecture architecture : values()) {
                if (architecture.aliases.contains(raw)) {
                    value = architecture;
                    break;
                }
            }
        }

        return value;
    }

    @Nullable
    private static String rawProcessArchitecture() {
        try {
            return System.getenv("PROCESSOR_ARCHITECTURE");
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private static String rawProcessArchitecture64() {
        try {
            return System.getenv("PROCESSOR_ARCHITEW6432");
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private static String rawArch() {
        try {
            return System.getProperty("os.arch");
        } catch (Throwable ignored) {
            return null;
        }
    }

}
