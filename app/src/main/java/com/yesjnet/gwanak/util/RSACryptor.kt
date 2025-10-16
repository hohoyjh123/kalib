package com.yesjnet.gwanak.util

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.extension.getLocaleCalendar
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.security.spec.RSAKeyGenParameterSpec
import java.util.Calendar
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.security.auth.x500.X500Principal

/**
 * Description: DATA 암호화 (RSA)
 */
class RSACryptor private constructor() {

    private var keyEntry: KeyStore.Entry? = null

    companion object {
        private const val TAG = "RSACryptor"
        private const val CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"

        val instance: RSACryptor by lazy { RSACryptor() }
    }

    fun init(context: Context) {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)

            val alias = context.packageName

            if (!ks.containsAlias(alias)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    initAndroidM(alias)
                } else {
                    initAndroidK(context, alias)
                }
            }

            keyEntry = ks.getEntry(alias, null)
        } catch (e: Exception) {
            Logger.e("RSA Initialize fail: $e")
        }
    }

    // Android M 이상 (API 23+)
    private fun initAndroidM(alias: String) {
        try {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
            )
            kpg.initialize(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    // ✅ RSA는 ECB 모드 + PKCS1Padding만 사용해야 함
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                    .setDigests(
                        KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA384,
                        KeyProperties.DIGEST_SHA512
                    )
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            kpg.generateKeyPair()
            Logger.d("RSA Initialize (M+) complete")
        } catch (e: Exception) {
            Logger.e("RSA key generation failed: $e")
        }
    }

    // Android K~L (API 19~22)
    private fun initAndroidK(context: Context, alias: String) {
        try {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 25)

            val kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
            kpg.initialize(
                KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=$alias"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .setKeySize(2048)
                    .build()
            )
            kpg.generateKeyPair()
            Logger.d("RSA Initialize (K+) complete")
        } catch (e: Exception) {
            Logger.e("RSA key generation failed: $e")
        }
    }

    /**
     * 문자열 암호화 (Public Key)
     */
    fun encrypt(plain: String): String {
        return try {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                (keyEntry as KeyStore.PrivateKeyEntry).certificate.publicKey
            )

            val encryptedBytes = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            val encoded = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            Logger.d("Encrypted Text : $encoded")
            encoded
        } catch (e: Exception) {
            Logger.e("Encrypt fail: $e")
            plain
        }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(
                Cipher.DECRYPT_MODE,
                (keyEntry as KeyStore.PrivateKeyEntry).privateKey
            )

            // 안전한 디코드(오직 NO_WRAP 사용)
            val decodedBytes = try {
                Base64.decode(encryptedText, Base64.NO_WRAP)
            } catch (e: IllegalArgumentException) {
                Logger.e("Decrypt fail: Base64 decode error: $e - inputLen=${encryptedText.length}")
                return encryptedText
            }

            Logger.d("Decrypt debug: decodedLen=${decodedBytes.size}, prefix=${bytesPrefixHex(decodedBytes, 16)}")

            val decryptedBytes = cipher.doFinal(decodedBytes)
            val decryptedText = String(decryptedBytes, Charsets.UTF_8)
            Logger.d("Decrypted Text : $decryptedText")
            decryptedText
        } catch (e: IllegalBlockSizeException) {
            Logger.e("Decrypt fail: IllegalBlockSizeException: ${e.message}. inputLen=${encryptedText.length}")
            encryptedText
        } catch (e: BadPaddingException) {
            Logger.e("Decrypt fail: BadPaddingException: ${e.message}")
            encryptedText
        } catch (e: Exception) {
            Logger.e("Decrypt fail: ${e.javaClass.simpleName}: ${e.message}")
            encryptedText
        }
    }

    // 헬퍼: 바이트 앞부분을 헥사로 보여줌
    private fun bytesPrefixHex(bytes: ByteArray, max: Int): String {
        val len = minOf(bytes.size, max)
        return bytes.take(len).joinToString("") { String.format("%02x", it) }
    }
}