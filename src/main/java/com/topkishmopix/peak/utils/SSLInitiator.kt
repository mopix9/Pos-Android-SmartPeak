package com.topkishmopix.peak.utils

import android.content.Context
import com.fanap.corepos.di.DependencyManager
import com.topkishmopix.peak.R
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

class SSLInitiator {

    fun init(isSSLEnabled : Boolean,context: Context? = null){
        if (isSSLEnabled){
            val sslContext: SSLContext?
            val passphrase = "aryan1234".toCharArray()
            try {
                val keystore: KeyStore = KeyStore.getInstance("BKS")
                keystore.load(context?.resources?.openRawResource(R.raw.aryan2), passphrase)
                val tmf: TrustManagerFactory =
                    TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                tmf.init(keystore)
                sslContext = SSLContext.getInstance("TLS")
                val trustManagers: Array<TrustManager> = tmf.trustManagers
                sslContext.init(null, trustManagers, null)
                DependencyManager.sslContext = sslContext
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            DependencyManager.sslContext = null
        }
    }

}