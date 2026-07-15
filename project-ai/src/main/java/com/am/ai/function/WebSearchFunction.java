package com.am.ai.function;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Configuration
public class WebSearchFunction {

    @Bean
    public Function<WebSearchRequest, WebSearchResponse> webSearch() {
        return request -> {
            try {
                RestClient restClient = RestClient.create();
                String result = restClient.get()
                        .uri("https://api.duckduckgo.com/?q={query}&format=json&no_html=1", request.query())
                        .retrieve()
                        .body(String.class);
                return new WebSearchResponse(result != null ? result : "未找到相关结果");
            } catch (Exception e) {
                return new WebSearchResponse("搜索失败: " + e.getMessage());
            }
        };
    }

    public record WebSearchRequest(String query) {}
    public record WebSearchResponse(String result) {}
}
