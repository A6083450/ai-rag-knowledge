package cn.bugstack.xfg.dev.tech.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author liangjiaquan
 * @date 2025/6/13
 */
@Slf4j
@SpringBootTest
public class OpenAiTest {
    
    @Resource
    private ChatModel chatModel;
    
    @Test
    public void testChat() {
        Prompt prompt = Prompt.builder()
                .content("你好。你是谁？")
                .build();
        
        System.out.println(chatModel.call(prompt).getResult().getOutput().getText());
       /* chatModel.stream(prompt)
                .doOnNext(chatResponse -> {
                    System.out.print(chatResponse.getResult().getOutput().getText());
                })
                .blockLast();*/
    }
}
