package dev.deftu.filestream.util;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

public enum OperatingSystem {

    UNKNOWN("Unknown", "", "", createAliases("unknown")),
    WINDOWS("Windows", "", "dll", createAliases("windows", "win")),
    LINUX("Linux", "lib", "so", createAliases("linux", "nix", "nux"), () -> !isMusl() && !isAndroid()),
    ANDROID("Android", "lib", "so", createAliases("android", "linux", "nix", "nux"), OperatingSystem::isAndroid),
    MACOS("macOS", "lib", "dylib", createAliases("mac", "darwin", "osx")),
    SOLARIS("Solaris", "lib", "so", createAliases("sunos", "solaris")),
    FREE_BSD("FreeBSD", "lib", "so", createAliases("freebsd")),
    NET_BSD("NetBSD", "lib", "so", createAliases("netbsd")),
    OPEN_BSD("OpenBSD", "lib", "so", createAliases("openbsd")),
    DRAGONFLY_BSD("DragonFly BSD", "lib", "so", createAliases("dragonflybsd")),
    UNKNOWN_BSD("Unknown BSD", "lib", "so", createAliases("_DO_NOT_DETECT", "bsd")),
    AIX("AIX", "lib", "so", createAliases("aix")),
    HAIKU("Haiku", "lib", "so", createAliases("haiku", "hrev")),
    ILLUMOS("Illumos", "lib", "so", createAliases("illumos", "omnios", "openindiana"));

    public final String name, nativePrefix, nativeExtension;
    public final Set<String> aliases;
    private final BooleanSupplier condition;

    OperatingSystem(String name, String nativePrefix, String nativeExtension, Set<String> aliases, BooleanSupplier condition) {
        this.name = name;
        this.nativePrefix = nativePrefix;
        this.nativeExtension = nativeExtension;
        this.aliases = aliases;
        this.condition = condition;
    }

    OperatingSystem(String name, String nativePrefix, String nativeExtension, Set<String> aliases) {
        this(name, nativePrefix, nativeExtension, aliases, () -> true);
    }

    public boolean isUnixLike() {
        return this == LINUX || this == ANDROID || this == MACOS || this == SOLARIS || this == FREE_BSD || this == NET_BSD || this == OPEN_BSD || this == DRAGONFLY_BSD || this == UNKNOWN_BSD || this == AIX || this == HAIKU || this == ILLUMOS;
    }

    @NotNull
    public static OperatingSystem find() {
        OperatingSystem value = OperatingSystem.UNKNOWN;
        String name = System.getProperty("os.name").toLowerCase().replace(" ", "");

        for (OperatingSystem os : values()) {
            if (os.condition.getAsBoolean() && (os.name.toLowerCase().replace(" ", "").equals(name))) {
                value = os;
                break;
            }
        }

        if (value == OperatingSystem.UNKNOWN) {
            for (OperatingSystem os : values()) {
                if (os.condition.getAsBoolean() && os.aliases.contains(name)) {
                    value = os;
                    break;
                }
            }
        }

        return value;
    }

    public static boolean isAndroid() {
        try {
            return System.getProperty("java.vm.name").toLowerCase().contains("android");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isMusl() {
        try {
            Process process = new ProcessBuilder("ldd", "--version").start();
            String line = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (line == null) {
                line = new BufferedReader(new InputStreamReader(process.getErrorStream())).readLine();
            }

            return line != null && line.toLowerCase().contains("musl");
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Set<String> createAliases(String... aliases) {
        return new HashSet<>(Arrays.asList(aliases));
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        return outputStream.toByteArray();
    }

}
