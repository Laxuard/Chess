package com.ft_transcendence.gateway.util;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.security.KeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.spec.X509EncodedKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class RSAKeyUtils {

    public RSAPrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = readAndClean(resource, "PRIVATE KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    public RSAPublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = readAndClean(resource, "PUBLIC KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(encoded));
    }

    private String readAndClean(Resource resource, String label) throws Exception {
        try (var inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return content
                    .replace("-----BEGIN " + label + "-----", "")
                    .replace("-----END " + label + "-----", "")
                    .replaceAll("\\s", "");
        }
    }

}
