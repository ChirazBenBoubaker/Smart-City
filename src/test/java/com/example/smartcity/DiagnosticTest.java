package com.example.smartcity;

import com.example.smartcity.config.TestSecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
@Disabled("Disabled in CI – context requires external dependencies")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)  // ✅ Import la config de test
class DiagnosticTest {

    @Test
    void contextLoads() {
        System.out.println("✅ Le contexte Spring charge correctement !");
    }
}