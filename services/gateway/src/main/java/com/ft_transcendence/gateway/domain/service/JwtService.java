package com.ft_transcendence.gateway.domain.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JWSSigner jwsSigner;
    private final String activeKid;

    public JwtService(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws JOSEException {

        this.jwsSigner = new RSASSASigner(privateKey);

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        this.activeKid = jwk.computeThumbprint().toString();
    }

    /**
     * Mints a cryptographically signed Transit JWT payload.
     * Includes the critical 'sid' pointer claim to enable downstream programmatic 2FA verification updates.
     */
    public String mint(String publicId, List<String> roles, String sessionId, String traceId) { // Added sessionId parameter
        Instant now = Instant.now();

        try {
            // Assemble the protected envelope header containing your dynamic thumbprint ID
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(this.activeKid)
                    .build();

            // Fill out the token contents safely
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(publicId)
                    .issuer("transcendence-gateway")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(60))) // Short-lived transit window
                    .claim("roles", roles)
                    .claim("sid", sessionId)
                    .claim("tid", traceId)
                    .build();

            // Seal, cryptographically stamp, and compile into a string text block
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(this.jwsSigner);

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Cryptographic Error: Failed to mint internal transit token payload", e);
        }
    }

}
