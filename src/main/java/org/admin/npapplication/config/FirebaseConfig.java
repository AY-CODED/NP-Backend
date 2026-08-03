package org.admin.npapplication.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        String firebaseConfig = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (firebaseConfig == null || firebaseConfig.isBlank()) {
            return;
        }

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8))))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
