package org.nixos.idea.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class WslUncPathMapperTest {
    @Test
    fun mapsLocalhostUncPathsToPosixPaths() {
        val mapper = WslUncPathMapper.fromProjectPath("\\\\wsl.localhost\\NixOS\\home\\nixos\\nixos-config")

        assertEquals(
            "/home/nixos/nixos-config/flake.nix",
            mapper?.toWslPath("\\\\wsl.localhost\\NixOS\\home\\nixos\\nixos-config\\flake.nix"),
        )
    }

    @Test
    fun mapsLegacyWslUncPathsToPosixPaths() {
        val mapper = WslUncPathMapper.fromProjectPath("//wsl$/NixOS/home/nixos/nixos-config")

        assertEquals("/home/nixos/nixos-config", mapper?.toWslPath("//wsl$/NixOS/home/nixos/nixos-config"))
    }

    @Test
    fun mapsWslRootToPosixRoot() {
        val mapper = WslUncPathMapper.fromProjectPath("//wsl.localhost/NixOS")

        assertEquals("/", mapper?.toWslPath("//wsl.localhost/NixOS"))
    }

    @Test
    fun mapsPosixPathsBackToTheProjectDistribution() {
        val mapper = WslUncPathMapper.fromProjectPath("\\\\wsl.localhost\\NixOS\\home\\nixos\\nixos-config")

        assertEquals(
            "\\\\wsl.localhost\\NixOS\\home\\nixos\\nixos-config\\flake.nix",
            mapper?.toUncPath("/home/nixos/nixos-config/flake.nix"),
        )
    }

    @Test
    fun leavesNonWslAndRelativePathsUnchanged() {
        assertNull(WslUncPathMapper.fromProjectPath("C:\\work\\nixos-config"))
        val mapper = WslUncPathMapper.fromProjectPath("//wsl.localhost/NixOS")
        assertNull(mapper?.toWslPath("C:\\work\\nixos-config\\flake.nix"))
        assertNull(mapper?.toWslPath("//wsl.localhost/OtherDistro/home/file.nix"))
        assertNull(mapper?.toUncPath("relative/file.nix"))
    }
}
