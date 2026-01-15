package apptive.team5.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${spring.ai.gemini.base-url}")
    private String geminiBaseUrl;

    @Value("${spring.ai.gemini.completions-path}")
    private String completionsPath;

    @Value("${spring.ai.gemini.model}")
    private String geminiModel;

    @Bean
    public OpenAiApi geminiApi() {


        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(10_000); // 10초
        requestFactory.setReadTimeout(60_000); // 1분

        RestClient.Builder customRestClientBuilder = RestClient.builder()
                .requestFactory(requestFactory);

        return OpenAiApi.builder()
                .apiKey(geminiApiKey)
                .baseUrl(geminiBaseUrl)
                .completionsPath(completionsPath)
                .restClientBuilder(customRestClientBuilder)
                .build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel() {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(geminiModel)
                .temperature(0.7)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(geminiApi())
                .defaultOptions(options)
                .build();
    }

    @Bean
    public ChatClient geminiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }



}
