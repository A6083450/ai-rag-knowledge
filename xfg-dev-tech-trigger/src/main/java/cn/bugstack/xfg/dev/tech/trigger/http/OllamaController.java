package cn.bugstack.xfg.dev.tech.trigger.http;

import cn.bugstack.xfg.dev.tech.api.IAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liangjiaquan
 * @date 2025/6/10
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama/")
public class OllamaController implements IAiService {
    
    @Autowired
    private ChatModel chatClient;
    @Autowired
    private PgVectorStore pgVectorStore;
    
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
    
    
    /**
     * http://localhost:8090/api/v1/ollama/generate_stream_rag?model=qwen3:1.7b&message=宏图科技公司地址在哪里？
     */
    @GetMapping(value = "generate_stream_rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
        String SYSTEM_PROMPT = """
                请使用“文档”部分中的信息来提供准确答案，但要表现得好像你本身就知道这些信息一样。
                如果不确定，就直接说你不知道。
                另外，你需要注意的是，你的回答必须用中文！
                文档:
                    {documents}
                """;
        SearchRequest request = SearchRequest.builder()
                .query(message)
                .topK(5)
                .filterExpression("knowledge == '" + ragTag + "'")
                .build();
        
        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentsCollectors = Optional.ofNullable(documents)
                .stream()
                .flatMap(List::stream)
                .map(Document::getText)
                .collect(Collectors.joining());
        
        /**
         * 创建 rag 提示
         */
        Message ragMessage = SystemPromptTemplate.
                builder()
                .template(SYSTEM_PROMPT)
                .variables(Map.of("documents", documentsCollectors))
                .build()
                .createMessage();
        
        List<Message> messages = new ArrayList<>();
        messages.add(ragMessage);
        // 用户问题
        messages.add(UserMessage.builder()
                .text(message)
                .build());
        
        OllamaOptions ollamaOptions = OllamaOptions.builder()
                .model(model)
                .build();
        Prompt prompt = Prompt.builder()
                .messages(messages)
                .chatOptions(ollamaOptions)
                .build();
        
        return chatClient.stream(prompt);
    }
}














