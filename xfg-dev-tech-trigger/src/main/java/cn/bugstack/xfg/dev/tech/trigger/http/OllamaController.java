package cn.bugstack.xfg.dev.tech.trigger.http;

import cn.bugstack.xfg.dev.tech.api.IAiService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author liangjiaquan
 * @date 2025/6/10
 */
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama/")
public class OllamaController implements IAiService {
    
    @Autowired
    private ChatModel chatClient;
    
    /**
     * http://localhost:8090/api/v1/ollama/generate?model=qwen3:1.7b&message=1+1
     */
    
    @RequestMapping(value = "generate", method = RequestMethod.GET)
    @Override
    public ChatResponse generate(@RequestParam("model") String model, @RequestParam("message") String message) {
        OllamaOptions chatOptions = OllamaOptions.builder()
                .model(model)
                .build();
        Prompt prompt = Prompt.builder()
                .content(message)
                .chatOptions(chatOptions)
                .build();
        return chatClient.call(prompt);
    }
    
    /**
     * http://localhost:8090/api/v1/ollama/generate_stream?model=qwen3:1.7b&message=hi
     */
    
    @RequestMapping(value = "generate_stream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam("model") String model, @RequestParam("message") String message) {
        OllamaOptions chatOptions = OllamaOptions.builder()
                .model(model)
                .build();
        Prompt prompt = Prompt.builder()
                .content(message)
                .chatOptions(chatOptions)
                .build();
        return chatClient.stream(prompt);
    }
}
