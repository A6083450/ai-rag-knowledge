package cn.bugstack.xfg.dev.tech.api;

import cn.bugstack.xfg.dev.tech.api.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author liangjiaquan
 * @date 2025/6/11
 */
public interface IRAGService {
    
    Response<List<String>> queryRagTagList();
    
    Response<String> uploadFile(String ragTag, List<MultipartFile> files);
}
