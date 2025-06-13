package cn.bugstack.xfg.dev.tech.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @author liangjiaquan
 * @date 2025/6/13
 */
@Slf4j
@SpringBootTest
public class JGitTest {
    
    @Autowired
    private PgVectorStore pgVectorStore;
    
    @Test
    public void test() throws IOException, GitAPIException {
        // 这部分替换为你的
        String repoURL = "https://gitee.com/shao1chuan/pythonbook";
        String username = "xxxxx@qq.com";
        String password = "xxxxxx";
        
        String localPath = "./cloned-repo";
        log.info("克隆路径：" + new File(localPath).getAbsolutePath());
        
        FileUtils.deleteDirectory(new File(localPath));
        
        Git git = Git.cloneRepository()
                .setURI(repoURL)
                .setDirectory(new File(localPath))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .call();
        
        git.close();
    }
    
    @Test
    public void test_file() throws IOException {
        Files.walkFileTree(Paths.get("./cloned-repo"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                log.info("文件路径:{}", file.toString());
                
                PathResource resource = new PathResource(file);
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.get();
                TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
                List<Document> documentSplitterList = tokenTextSplitter.split(documents);
                documents.forEach(doc -> doc.getMetadata().put("knowledge", "group-buy-market-liergou"));
                documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "group-buy-market-liergou"));
                pgVectorStore.add(documentSplitterList);
                
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
