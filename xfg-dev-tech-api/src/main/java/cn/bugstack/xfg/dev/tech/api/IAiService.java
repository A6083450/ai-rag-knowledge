package cn.bugstack.xfg.dev.tech.api;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author liangjiaquan
 * @date 2025/6/10
 */
public interface IAiService {
    
    ChatResponse generate(String model, String message);
    
    Flux<ChatResponse> generateStream(String model, String message);
    
}
