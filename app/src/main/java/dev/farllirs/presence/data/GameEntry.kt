package dev.farllirs.presence.data

data class GameEntry(val packageName: String, val displayName: String) {
 init { require(packageName.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+"))) { "Package name no válido" }; require(displayName.isNotBlank()) { "El nombre no puede estar vacío" } }
}
