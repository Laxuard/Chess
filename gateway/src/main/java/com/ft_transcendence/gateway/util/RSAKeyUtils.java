package com.ft_transcendence.gateway.util;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ResourceLoader;

import java.util.Base64;
import java.security.KeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.spec.X509EncodedKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class RSAKeyUtils {

    private final ResourceLoader resourceLoader;

    public RSAKeyUtils(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public RSAPrivateKey loadPrivateKey(String path) throws Exception
    {

        String pem = readAndClean(path, "PRIVATE KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(encoded));

    }

    public RSAPublicKey loadPublicKey(String path) throws Exception {
        String pem = readAndClean(path, "PUBLIC KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(encoded));
    }

    private String readAndClean(String path, String label) throws Exception {
        Resource resource = resourceLoader.getResource(path);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return content
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
    }

}
