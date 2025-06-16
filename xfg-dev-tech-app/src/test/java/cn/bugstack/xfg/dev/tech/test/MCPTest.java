package cn.bugstack.xfg.dev.tech.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author liangjiaquan
 * @date 2025/6/16
 */
@Slf4j
@SpringBootTest
public class MCPTest {
    
    @Resource
    private ChatModel chatModel;
    @Resource
    private ToolCallbackProvider tools;
    @Resource
    private ChatClient.Builder chatClientBuilder;
    
    @Test
    public void test_tool() {
        ChatClient chatClient = chatClientBuilder
                .defaultToolCallbacks(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-max")
                        .build())
                .build();
        String userInput = "有哪些工具可以使用？";
        // userInput = "获取电脑配置";
        
        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }
    
    @Test
    public void test_workflow() {
        String userInput = "获取电脑配置";
        userInput = "在 /Users/liangjiaquan/Downloads/puppeteer 文件夹下，创建文件 电脑.txt";
        
        var chatClient = chatClientBuilder
                .defaultToolCallbacks(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-max")
                        .build())
                .build();
        
        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }
    
    @Test
    public void test() {
        String userInput = "获取电脑配置 在 /Users/liangjiaquan/Downloads/puppeteer 文件夹下，创建文件 电脑.json 把电脑配置写入 电脑.json";
        var chatClient = chatClientBuilder
                .defaultToolCallbacks(tools)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("qwen-max")
                        .build())
                .build();
        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }
    
    
}
