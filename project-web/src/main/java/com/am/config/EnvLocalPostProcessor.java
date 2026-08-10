package com.am.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动加载项目根目录的 .env.local 文件作为环境变量。
 * 使 IDE 启动也能正确读取敏感配置，无需手动设置环境变量。
 */
public class EnvLocalPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 从当前工作目录向上查找 .env.local
        Path envFile = findEnvFile(Paths.get("").toAbsolutePath());
        if (envFile == null || !Files.exists(envFile)) {
            return;
        }

        Map<String, Object> envVars = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                // 移除可能的引号
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                // 仅在环境变量未设置时填充（不覆盖已有系统环境变量）
                if (System.getenv(key) == null) {
                    envVars.put(key, value);
                }
            }
        } catch (IOException e) {
            return;
        }

        if (!envVars.isEmpty()) {
            environment.getPropertySources().addFirst(
                new MapPropertySource("envLocal", envVars)
            );
        }
    }

    private Path findEnvFile(Path start) {
        Path current = start;
        // 向上查找最多 5 层
        for (int i = 0; i < 5; i++) {
            Path candidate = current.resolve(".env.local");
            if (Files.exists(candidate)) return candidate;
            Path parent = current.getParent();
            if (parent == null) break;
            current = parent;
        }
        return null;
    }
}
