package cn.bugstack.xfg.dev.tech.trigger.http;

import cn.bugstack.xfg.dev.tech.api.IRAGService;
import cn.bugstack.xfg.dev.tech.api.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liangjiaquan
 * @date 2025/6/11
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RAGController implements IRAGService {
    
    @Resource
    private OllamaChatModel ollamaChatModel;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;
    
    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    @Override
    public Response<List<String>> queryRagTagList() {
        RList<String> elements = redissonClient.getList("ragTag");
        return Response.<List<String>>builder()
                .code("0000")
                .info("调用成功")
                .data(new ArrayList<>(elements))
                .build();
    }
    
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    @Override
    public Response<String> uploadFile(@RequestParam("ragTag") String ragTag,
                                       @RequestParam("files") List<MultipartFile> files) {
        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
        log.info("上传知识库开始 {}", ragTag);
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.split(documents);
            
            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            
            pgVectorStore.add(documentSplitterList);
            RList<String> elements = redissonClient.getList("ragTag");
            if (!elements.contains(ragTag)){
                elements.add(ragTag);
            }
        }
        
        log.info("上传知识库完成 {}", ragTag);
        return Response.<String>builder().code("0000").info("调用成功").build();
    }
}
