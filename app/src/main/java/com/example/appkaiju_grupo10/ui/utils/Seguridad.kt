package com.example.appkaiju_grupo10.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val ITERACIONES = 10_000      // cuántas veces se repite el cálculo
private const val LARGO_HASH_BITS = 256

// Calcula la "huella" de una contraseña usando una sal
private fun derivarClave(password: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, ITERACIONES, LARGO_HASH_BITS)
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
}

// Para REGISTRAR: devuelve "sal:hash", que es lo que se guarda en la BD
fun generarHash(password: String): String {
    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }   // 16 bytes al azar
    val hash = derivarClave(password, salt)
    val codificador = Base64.getEncoder()
    return "${codificador.encodeToString(salt)}:${codificador.encodeToString(hash)}"
}

// Para el LOGIN: ¿la contraseña escrita produce la misma huella guardada?
fun verificarPassword(password: String, guardado: String): Boolean {
    val partes = guardado.split(":")
    if (partes.size != 2) return false
    val decodificador = Base64.getDecoder()
    val salt = decodificador.decode(partes[0])
    val hashEsperado = decodificador.decode(partes[1])
    return MessageDigest.isEqual(derivarClave(password, salt), hashEsperado)
}