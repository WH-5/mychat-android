import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.wh5.mychat.viewmodel.EncryptionInfo
import com.github.wh5.mychat.viewmodel.base64ToPrivateKey
import com.github.wh5.mychat.viewmodel.base64ToPublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.GCMParameterSpec


// 生成随机盐值
fun generateSalt(length: Int = 16): ByteArray {
    val secureRandom = SecureRandom()
    val salt = ByteArray(length)
    secureRandom.nextBytes(salt)
    return salt
}

// 通过密码和盐生成对称密钥（KDF）
fun deriveKey(password: String, salt: ByteArray): ByteArray {
    val keySpec = PBEKeySpec(password.toCharArray(), salt, 10000, 256) // PBKDF2
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    return keyFactory.generateSecret(keySpec).encoded
}

// 使用对称密钥加密私钥，并返回拼接后的密文和 IV
@RequiresApi(Build.VERSION_CODES.O)
fun encryptPrivateKey(privateKey: ByteArray, symmetricKey: ByteArray): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val iv = ByteArray(12)
    SecureRandom().nextBytes(iv)  // 生成随机的 IV
    val keySpec = GCMParameterSpec(128, iv)  // GCM 参数

    val key = SecretKeySpec(symmetricKey, "AES")
    cipher.init(Cipher.ENCRYPT_MODE, key, keySpec)
    val cipherText = cipher.doFinal(privateKey)

    // 合并 IV 和密文，返回拼接后的字符串
    val ivAndCipherText = iv + cipherText
    return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP)  // 返回拼接后的字符串
}

// 修改函数：返回 EncryptionInfo 类型
@RequiresApi(Build.VERSION_CODES.O)
fun generateEncryptionInfo(password: String): EncryptionInfo {
    if (password.isBlank()) {
        Log.e("EncryptionError", "密码为空，无法生成加密信息")
        throw IllegalArgumentException("密码不能为空")
    }

    // 1. 生成盐值
    val salt = generateSalt()

    // 2. 通过密码 + 盐派生对称密钥
    val symmetricKey = deriveKey(password, salt)

    // 3. 真正生成一对 RSA 密钥对
    val keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048)
    val keyPair = keyPairGenerator.generateKeyPair()
    val privateKey = keyPair.private.encoded
    // 生成公钥的 Base64 编码
    val publicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)

    // 4. 使用对称密钥加密私钥，并返回拼接的 Base64 字符串
    val encryptedPrivateKey = encryptPrivateKey(privateKey, symmetricKey)

    // 5. 返回 EncryptionInfo 对象，包含盐、加密后的私钥（字符串）和公钥
    return EncryptionInfo(
        kdfSalt = Base64.encodeToString(salt, Base64.NO_WRAP), // 盐值 Base64 编码
        publicKey = publicKey, // 公钥
        encryptedPrivateKey = encryptedPrivateKey  // 返回拼接后的加密私钥字符串
    )
}

// 解密函数：从拼接的 Base64 字符串中提取 IV 和密文进行解密
fun decryptPrivateKey(encryptedPrivateKeyBase64: String, password: String, saltBase64: String): ByteArray? {
    if (encryptedPrivateKeyBase64.isNullOrEmpty()) {
        Log.e("LoginError", "Encrypted private key is null or empty")
        return null
    }

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val encrypted = Base64.decode(encryptedPrivateKeyBase64, Base64.NO_WRAP)

    val key = deriveKey(password, salt)

    // 从拼接的字节数组中拆分出 IV 和密文
    val iv = encrypted.sliceArray(0 until 12)  // 前12字节是 IV
    val cipherText = encrypted.sliceArray(12 until encrypted.size)  // 剩下的是密文

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val spec = GCMParameterSpec(128, iv)
    cipher.init(Cipher.DECRYPT_MODE,SecretKeySpec(key, "AES"), spec)

    return cipher.doFinal(cipherText)
}
@RequiresApi(Build.VERSION_CODES.O)
fun testEncryptionRoundTrip() {
    val password = "123456"

    // 生成加密信息
    val info = generateEncryptionInfo(password)

    Log.d("Test", "kdfSalt: ${info.kdfSalt}")
    Log.d("Test", "publicKey: ${info.publicKey}")
    Log.d("Test", "encryptedPrivateKey: ${info.encryptedPrivateKey}")

    // 解密回原始私钥
    val decryptedPrivateKeyBytes = decryptPrivateKey(info.encryptedPrivateKey, password, info.kdfSalt)

    if (decryptedPrivateKeyBytes != null) {
        Log.d("Test", "Decrypt success, private key bytes length: ${decryptedPrivateKeyBytes.size}")
    } else {
        Log.e("Test", "Decrypt failed.")
    }
}