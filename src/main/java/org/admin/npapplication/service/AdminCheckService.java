package org.admin.npapplication.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class AdminCheckService {

    public boolean isAdmin(String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            Object adminClaim = userRecord.getCustomClaims().get("admin");
            return Boolean.TRUE.equals(adminClaim);
        } catch (FirebaseAuthException e) {
            return false;
        }
    }
}