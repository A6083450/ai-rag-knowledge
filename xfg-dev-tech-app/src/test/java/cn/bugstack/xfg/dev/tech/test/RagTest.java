package cn.bugstack.xfg.dev.tech.test;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liangjiaquan
 * @date 2025/6/11
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RagTest {
    
    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private PgVectorStore pgVectorStore;
    
    /**
     * 文本向量化
     */
    @Test
    public void upload() {
        TikaDocumentReader  reader = new TikaDocumentReader("./data/file.text");
        
        List<Document> documents = reader.get();
        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
        
        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        
        pgVectorStore.add(documentSplitterList);
        
        log.info("上传完成");
    }
    
    /**
     * 提问
     */
    @Test
    public void chat() {
        String message = "王大瓜，哪年出生";
        
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
                .filterExpression("knowledge == '知识库名称'")
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
                .model("deepseek-r1:1.5b")
                .build();
        Prompt prompt = Prompt.builder()
                .messages(messages)
                .chatOptions(ollamaOptions)
                .build();
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        
        log.info("测试结果:{}", JSON.toJSONString(chatResponse));
        chatResponse.getResults().stream();
        String content = Optional.of(chatResponse)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .orElse("");
        log.info("大模型回复：\n{}", content);
        
    }
}














