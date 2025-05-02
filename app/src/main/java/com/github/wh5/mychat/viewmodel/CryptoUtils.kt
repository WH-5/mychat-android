/**
 * CryptoUtils.kt
 * 该工具类封装了 RSA 加密解密操作，并提供了公钥、私钥与 Base64 字符串之间的转换。
 *
 * 使用方式：
 *
 * 1. 加密消息：使用公钥加密消息
 *    例如：
 *    val encryptedMessage = encryptMessageWithPublicKey(publicKey, "消息内容")
 *
 * 2. 解密消息：使用私钥解密消息
 *    例如：
 *    val decryptedMessage = decryptMessageWithPrivateKey(privateKey, encryptedMessage)
 *
 * 3. 密钥转换：
 *    - 将 Base64 字符串转换为 PublicKey 或 PrivateKey：
 *      例如：
 *      val publicKey = base64ToPublicKey(publicKeyBase64)
 *      val privateKey = base64ToPrivateKey(privateKeyBase64)
 *    - 将 PublicKey 或 PrivateKey 转换为 Base64 字符串：
 *      例如：
 *      val publicKeyBase64 = publicKeyToBase64(publicKey)
 *      val privateKeyBase64 = privateKeyToBase64(privateKey)
 */

package com.github.wh5.mychat.viewmodel

import android.util.Base64
import java.security.PrivateKey
import java.security.PublicKey
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.interfaces.RSAPrivateKey
import javax.crypto.Cipher

/**
 * 使用公钥加密消息
 * @param publicKey 对方的公钥，用于加密消息
 * @param message 明文消息
 * @return 加密后的消息（Base64 编码）
 */
fun encryptMessageWithPublicKey(publicKey: PublicKey, message: String): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encryptedBytes = cipher.doFinal(message.toByteArray())
    return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
}

/**
 * 使用私钥解密消息
 * @param privateKey 自己的私钥，用于解密消息
 * @param base64EncryptedMessage 加密后的消息（Base64 编码）
 * @return 解密后的消息（明文）
 */
fun decryptMessageWithPrivateKey(privateKey: PrivateKey, base64EncryptedMessage: String): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val encryptedBytes = Base64.decode(base64EncryptedMessage, Base64.NO_WRAP)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes)
}

/**
 * 将 Base64 字符串转换为 PublicKey 对象
 * @param base64PublicKey 公钥的 Base64 编码
 * @return 公钥对象
 */
fun base64ToPublicKey(base64PublicKey: String): PublicKey {
    val keyBytes = Base64.decode(base64PublicKey, Base64.NO_WRAP)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(keyBytes))
}

/**
 * 将 Base64 字符串转换为 PrivateKey 对象
 * @param base64PrivateKey 私钥的 Base64 编码
 * @return 私钥对象
 */
fun base64ToPrivateKey(base64PrivateKey: String): PrivateKey {
    val keyBytes = Base64.decode(base64PrivateKey, Base64.NO_WRAP)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(keyBytes))
}

/**
 * 将 PublicKey 对象转换为 Base64 字符串
 * @param publicKey 公钥对象
 * @return Base64 编码的公钥字符串
 */
fun publicKeyToBase64(publicKey: PublicKey): String {
    val keyBytes = publicKey.encoded
    return Base64.encodeToString(keyBytes, Base64.NO_WRAP)
}

/**
 * 将 PrivateKey 对象转换为 Base64 字符串
 * @param privateKey 私钥对象
 * @return Base64 编码的私钥字符串
 */
fun privateKeyToBase64(privateKey: PrivateKey): String {
    val keyBytes = privateKey.encoded
    return Base64.encodeToString(keyBytes, Base64.NO_WRAP)
}