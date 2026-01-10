package com.schedulecore.ufu.infrasctructure.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.schedulecore.ufu.domains.models.UserModel;
import com.schedulecore.ufu.domains.models.enums.AcessEnum;
import com.schedulecore.ufu.infrasctructure.database.entitys.ConfigEntity;
import com.schedulecore.ufu.infrasctructure.database.repositorys.ConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 horas (1 dia)
    private static final String SECRET_KEY_ID = "secret_key";
    private final ConfigRepository configRepository;
    private Algorithm cachedAlgorithm;
    private JWTVerifier jwtVerifier;

    @PostConstruct
    private void init() {
        this.cachedAlgorithm = Algorithm.HMAC256(hashSecret(getSecret()));
        this.jwtVerifier = JWT.require(this.cachedAlgorithm).build();
    }

    public String generateToken(UserModel model) {
        return JWT.create()
                .withSubject(model.getEmail())
                .withClaim("matricula", model.getMatricula())
                .withClaim("email", model.getEmail())
                .withClaim("name", model.getNome())
                .withClaim("role", model.getAcess().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
                .sign(cachedAlgorithm);
    }

    public UserModel parseToken(String token) {
        try {
            DecodedJWT decoded = jwtVerifier.verify(token);
            return UserModel.builder()
                    .email(decoded.getClaim("email").asString())
                    .matricula(decoded.getClaim("matricula").asString())
                    .nome(decoded.getClaim("name").asString())
                    .acess(AcessEnum.valueOf(decoded.getClaim("role").asString()))
                    .build();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash do e-mail", e);
        }
    }

    private String getSecret() {
        Optional<ConfigEntity> config = configRepository.findById(SECRET_KEY_ID);
        if (config.isPresent()) {
            return config.get().getValue();
        }
        String secret = createSecret();
        configRepository.save(
                ConfigEntity.builder()
                        .nome(SECRET_KEY_ID)
                        .value(secret)
                        .build()
        );
        return secret;
    }

    private String createSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

}
