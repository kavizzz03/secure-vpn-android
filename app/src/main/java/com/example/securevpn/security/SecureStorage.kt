package com.example.securevpn.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(
    private val context: Context
) {

    companion object {

        private const val KEYSTORE =
            "AndroidKeyStore"

        private const val KEY_ALIAS =
            "SecureVPNStorageKey"

        private const val PREFS =
            "secure_vpn_storage"

        private const val IV_SIZE =
            12

        private const val TAG_SIZE =
            128
    }

    private val keyStore: KeyStore by lazy {

        KeyStore
            .getInstance(KEYSTORE)
            .apply {
                load(null)
            }
    }

    private fun getOrCreateKey(): SecretKey {

        if (keyStore.containsAlias(KEY_ALIAS)) {

            return keyStore
                .getKey(KEY_ALIAS, null) as SecretKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                "AES",
                KEYSTORE
            )

        keyGenerator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return keyGenerator.generateKey()
    }

    fun save(
        key: String,
        value: String
    ) {

        val secretKey =
            getOrCreateKey()

        val cipher =
            Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey
        )

        val encrypted =
            cipher.doFinal(
                value.toByteArray(
                    StandardCharsets.UTF_8
                )
            )

        val iv =
            cipher.iv

        val combined =
            ByteArray(iv.size + encrypted.size)

        System.arraycopy(
            iv,
            0,
            combined,
            0,
            iv.size
        )

        System.arraycopy(
            encrypted,
            0,
            combined,
            iv.size,
            encrypted.size
        )

        val encoded =
            Base64.encodeToString(
                combined,
                Base64.NO_WRAP
            )

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(key, encoded)
            .apply()
    }

    fun get(
        key: String
    ): String? {

        val stored =
            context
                .getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )
                .getString(
                    key,
                    null
                )
                ?: return null

        return try {

            val combined =
                Base64.decode(
                    stored,
                    Base64.NO_WRAP
                )

            val iv =
                combined.copyOfRange(
                    0,
                    IV_SIZE
                )

            val encrypted =
                combined.copyOfRange(
                    IV_SIZE,
                    combined.size
                )

            val cipher =
                Cipher.getInstance(
                    "AES/GCM/NoPadding"
                )

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    TAG_SIZE,
                    iv
                )
            )

            String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
            )

        } catch (
            exception: Exception
        ) {

            null
        }
    }

    fun delete(
        key: String
    ) {

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(key)
            .apply()
    }
}