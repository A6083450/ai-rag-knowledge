package cn.bugstack.xfg.dev.tech.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author liangjiaquan
 * @date 2025/6/16
 */
// @Configuration
public class ToolConfig {
    
    @Bean
    public CommandLineRunner predefineQuestions(ChatClient.Builder chatClientBuilder,
                                                ToolCallbackProvider tools,
                                                ConfigurableApplicationContext context) {
        return args -> {
            var chatClient = chatClientBuilder.defaultToolCallbacks(tools).build();
            chatClient.prompt().call().content();
            context.close();
        };
    }
    
    @Bean
    public ChatClient.Builder chatClientBuilder(OpenAiChatModel openAiChatModel) {
        return new DefaultChatClientBuilder(openAiChatModel, ObservationRegistry.NOOP, null);
    }
}
