package com.getamotel.integration.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.FileInputStream

@ApplicationScoped
class FirebaseAdminClient {

    private val log = Logger.getLogger(FirebaseAdminClient::class.java)

    @ConfigProperty(name = "firebase.service-account.path")
    lateinit var serviceAccountPath: String

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                val serviceAccount = FileInputStream(serviceAccountPath)
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                FirebaseApp.initializeApp(options)
                log.info("Firebase Admin SDK initialized successfully")
            } catch (e: Exception) {
                log.error("Failed to initialize Firebase Admin SDK: ${e.message}")
                // In production, this should probably fail the startup
            }
        }
    }

    fun verifyToken(idToken: String): FirebaseToken? {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: Exception) {
            log.warn("Failed to verify Firebase token: ${e.message}")
            null
        }
    }

    /**
     * Enforces US-only SMS guardrails by checking the phone number on the token.
     * Note: This assumes the token was generated via SMS auth.
     */
    fun verifyUsOnlySms(token: FirebaseToken): Boolean {
        val phone = token.claims["phone_number"] as? String ?: return false
        return phone.startsWith("+1")
    }
}
