package org.example.logo.analysis

object LogoRegex {
    val procedureDeclaration = Regex(
        """^\s*TO\s+([A-Za-z_][A-Za-z0-9_]*)""",
        RegexOption.IGNORE_CASE
    )

    val procedureEnd = Regex(
        """^\s*END\s*$""",
        RegexOption.IGNORE_CASE
    )

    val makeDeclaration = Regex(
        """\bMAKE\s+"([A-Za-z_][A-Za-z0-9_]*)""",
        RegexOption.IGNORE_CASE
    )

    val localDeclaration = Regex(
        """\bLOCAL\s+"([A-Za-z_][A-Za-z0-9_]*)""",
        RegexOption.IGNORE_CASE
    )

    val variableReference = Regex(
        """:([A-Za-z_][A-Za-z0-9_]*)"""
    )

    val number = Regex(
        """\b\d+(\.\d+)?\b"""
    )

    val word = Regex(
        """\b([A-Za-z_][A-Za-z0-9_]*)\b"""
    )
}