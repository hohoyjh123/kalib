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
 *
 * Description: DATA 암호화
 */
class RSACryptor
/**
 * Singleton Pattern * Call -> RSACryptor.getInstance().method()
 */
private constructor() {
    private var keyEntry: KeyStore.Entry? = null

    private object RSACryptorHolder {
        val INSTANCE = RSACryptor()
    }

    //Android KeyStore 시스템에서는 암호화 키를
    // 컨테이너(시스템만이 접근 가능한 곳)에 저장해야 하므로
    // 이 키를 기기에서 추출해내기가 더 어려움
    fun init(context: Context) {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            if (!ks.containsAlias(context.packageName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    initAndroidM(context.packageName)
                } else {
                    initAndroidK(context)
                }
            }
            keyEntry = ks.getEntry(context.packageName, null)
        } catch (e: KeyStoreException) {
            Logger.e("Initialize fail = $e")
        } catch (e: IOException) {
            Logger.e("Initialize fail = $e")
        } catch (e: NoSuchAlgorithmException) {
            Logger.e("Initialize fail = $e")
        } catch (e: UnrecoverableEntryException) {
            Logger.e("Initialize fail = $e")
        } catch (e: CertificateException) {
            Logger.e("Initialize fail = $e")
        } catch (e : Exception) {
            Logger.e("Initialize fail = $e")
        }
    }

    //API Level 23 이상(마쉬멜로우) 개인키 생성
    private fun initAndroidM(alias: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //AndroidKeyStore 정확하게 기입
                val kpg =
                    KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA,
                        "AndroidKeyStore"
                    )
                kpg.initialize(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setAlgorithmParameterSpec(
                        RSAKeyGenParameterSpec(
                            2048,
                            RSAKeyGenParameterSpec.F4
                        )
                    ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setDigests(
                            KeyProperties.DIGEST_SHA512,
                            KeyProperties.DIGEST_SHA384,
                            KeyProperties.DIGEST_SHA256
                        ).setUserAuthenticationRequired(false).build()
                )
                kpg.generateKeyPair()
                Logger.d("RSA Initialize")
            }
        } catch (e: GeneralSecurityException) {
            Logger.e("이 디바이스는 관련 알고리즘을 지원하지 않음.$e")
        }
    }

    //API Level 19 이상(킷캣) 개인키 생성
    private fun initAndroidK(context: Context) {
        try {
            //유효성 기간
            val start = getLocaleCalendar()
            val end = getLocaleCalendar()
            end.add(Calendar.YEAR, 25)
            //AndroidKeyStore 정확하게 기입
            val kpg =
                KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
            kpg.initialize(
                KeyPairGeneratorSpec.Builder(context).setKeySize(2048)
                    .setAlias(context.packageName)
                    .setSubject(X500Principal("CN=myKey"))
                    .setSerialNumber(BigInteger.ONE).setStartDate(start.time)
                    .setEndDate(end.time).build()
            )
            kpg.generateKeyPair()
            Logger.d("RSA Initialize")
        } catch (e: Exception) {
            Logger.e("이 디바이스는 관련 알고리즘을 지원하지 않음.$e")
        }
    }

    /**
     * 문자열 암호화
     * @param plain 암호화할 문자열
     * @return 암호화된 문자열
     */
    fun encrypt(plain: String): String {
        return try {
            val bytes = plain.toByteArray(charset("UTF-8"))
            val cipher =
                Cipher.getInstance(CIPHER_ALGORITHM)
            //Public Key로 암호화
            cipher.init(
                Cipher.ENCRYPT_MODE,
                (keyEntry as KeyStore.PrivateKeyEntry?)!!.certificate
                    .publicKey
            )
            val encryptedBytes = cipher.doFinal(bytes)
            Logger.d("Encrypted Text : ${String(Base64.encode(encryptedBytes, Base64.DEFAULT))}")
            String(Base64.encode(encryptedBytes, Base64.DEFAULT))
        } catch (e: UnsupportedEncodingException) {
            Logger.e("Encrypt fail $e")
            plain
        } catch (e: NoSuchAlgorithmException) {
            Logger.e("Encrypt fail $e")
            plain
        } catch (e: NoSuchPaddingException) {
            Logger.e("Encrypt fail $e")
            plain
        } catch (e: InvalidKeyException) {
            Logger.e("Encrypt fail $e")
            plain
        } catch (e: IllegalBlockSizeException) {
            Logger.e("Encrypt fail $e")
            plain
        } catch (e: BadPaddingException) {
            Logger.e("Encrypt fail $e")
            plain
        }
    }

    /**
     * 문자열 복호화
     * @param encryptedText 복호화할 문자열
     * @return string
     */
    fun decrypt(encryptedText: String): String {
        return try {
            val cipher =
                Cipher.getInstance(CIPHER_ALGORITHM)
            // Private Key로 복호화
            cipher.init(
                Cipher.DECRYPT_MODE,
                (keyEntry as KeyStore.PrivateKeyEntry?)!!.privateKey
            )
            val base64Bytes = encryptedText.toByteArray(charset("UTF-8"))
            val decryptedBytes =
                Base64.decode(base64Bytes, Base64.DEFAULT)
            Logger.d("Decrypted Text : " + String(cipher.doFinal(decryptedBytes)))
            String(cipher.doFinal(decryptedBytes))
        } catch (e: NoSuchAlgorithmException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        } catch (e: NoSuchPaddingException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        } catch (e: InvalidKeyException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        } catch (e: UnsupportedEncodingException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        } catch (e: BadPaddingException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        } catch (e: IllegalBlockSizeException) {
            Logger.e("Decrypt fail $e")
            encryptedText
        }
    }

    companion object {
        private const val TAG = "RSACryptor"

        //비대칭 암호화(공개키) 알고리즘 호출 상수
        private const val CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"
        val instance: RSACryptor
            get() = RSACryptorHolder.INSTANCE
    }
}