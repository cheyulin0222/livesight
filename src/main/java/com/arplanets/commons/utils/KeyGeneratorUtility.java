package com.arplanets.commons.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.UUID;

public class KeyGeneratorUtility {

    public static void main(String[] args) {
        try {
            System.out.println("正在產生新的 RSA-2048 金鑰對...");

            // 1. 產生金鑰對
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // 2. 定義儲存路徑 (在同一個地方)
            Path resourcesPath = Paths.get("src/main/resources");
            Path privateKeyPath = resourcesPath.resolve("private_key.pem");
            Path jwksPath = resourcesPath.resolve("jwks.json");

            // 確保父目錄存在
            Files.createDirectories(resourcesPath);

            // 3. 將私鑰以 PKCS#8 格式寫入檔案
            try (OutputStream privateOs = new FileOutputStream(privateKeyPath.toFile())) {
                privateOs.write(privateKey.getEncoded());
            }

            // 2. 產生並儲存公鑰 (JWKS JSON)
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            String keyId = UUID.randomUUID().toString();
            Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
            String n = urlEncoder.encodeToString(rsaPublicKey.getModulus().toByteArray());
            String e = urlEncoder.encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

            String jwksJson = "{\n" +
                    "  \"keys\": [\n" +
                    "    {\n" +
                    "      \"kty\": \"RSA\",\n" +
                    "      \"use\": \"sig\",\n" + // 新增 "use": "sig" (signature)，表明此金鑰用於簽章驗證
                    "      \"kid\": \"" + keyId + "\",\n" +
                    "      \"n\": \"" + n + "\",\n" +
                    "      \"e\": \"" + e + "\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jwksPath.toFile()), StandardCharsets.UTF_8)) {
                writer.write(jwksJson);
            }

            System.out.println("成功！");

        } catch (Exception e) {
            System.err.println("產生金鑰時發生錯誤！");
        }
    }

}
