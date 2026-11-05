package org.nixos.idea.lsp

/**
 * Translates Windows' UNC representation of a WSL path to the POSIX path that
 * an LSP server running inside that WSL distribution expects.
 */
internal class WslUncPathMapper private constructor(
    private val uncRoot: String,
    private val separator: Char,
) {
    fun toWslPath(path: String): String? {
        val match = wslUncPath.matchEntire(path) ?: return null
        val fileRoot = "${match.groupValues[1]}/${match.groupValues[2]}".replace('\\', '/')
        if (!fileRoot.equals(uncRoot.replace('\\', '/'), ignoreCase = true)) return null

        val relativePath = match.groups[3]?.value.orEmpty().replace('\\', '/')
        return if (relativePath.isEmpty()) "/" else "/${relativePath.trimStart('/')}"
    }

    fun toUncPath(wslPath: String): String? {
        // LSP file paths must be absolute. Avoid accidentally turning a URI
        // authority or a relative path into a path under the WSL share.
        if (!wslPath.startsWith('/') || wslPath.startsWith("//")) return null

        val relativePath = wslPath.trimStart('/').replace('/', separator)
        return if (relativePath.isEmpty()) uncRoot else "$uncRoot$separator$relativePath"
    }

    companion object {
        private val wslUncPath = Regex(
            """^([/\\]{2}wsl(?:\.localhost|\$))[/\\]([^/\\]+)(?:[/\\](.*))?$""",
            RegexOption.IGNORE_CASE,
        )

        fun fromProjectPath(path: String): WslUncPathMapper? {
            val match = wslUncPath.matchEntire(path) ?: return null
            val separator = if ('\\' in path) '\\' else '/'
            val uncRoot = "${match.groupValues[1]}$separator${match.groupValues[2]}"
            return WslUncPathMapper(uncRoot, separator)
        }
    }
}
