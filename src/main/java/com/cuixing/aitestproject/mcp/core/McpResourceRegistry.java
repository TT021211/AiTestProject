package com.cuixing.aitestproject.mcp.core;

import com.cuixing.aitestproject.mcp.model.McpResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 资源注册中心
 */
@Component
public class McpResourceRegistry {
    private static final Logger log = LoggerFactory.getLogger(McpResourceRegistry.class);
    
    private final Map<String, McpResource> resources = new ConcurrentHashMap<>();
    private final Map<String, McpResourceProvider> providers = new ConcurrentHashMap<>();
    
    /**
     * 注册资源
     */
    public void registerResource(McpResource resource, McpResourceProvider provider) {
        if (resource == null || resource.getUri() == null) {
            throw new IllegalArgumentException("资源或资源URI不能为空");
        }
        
        resources.put(resource.getUri(), resource);
        providers.put(resource.getUri(), provider);
        log.info("MCP资源已注册: {} - {}", resource.getUri(), resource.getName());
    }
    
    /**
     * 注销资源
     */
    public void unregisterResource(String uri) {
        resources.remove(uri);
        providers.remove(uri);
        log.info("MCP资源已注销: {}", uri);
    }
    
    /**
     * 获取所有资源定义
     */
    public List<McpResource> listResources() {
        return new ArrayList<>(resources.values());
    }
    
    /**
     * 获取资源定义
     */
    public McpResource getResource(String uri) {
        return resources.get(uri);
    }
    
    /**
     * 读取资源内容
     */
    public Object readResource(String uri) throws Exception {
        McpResourceProvider provider = providers.get(uri);
        if (provider == null) {
            throw new IllegalArgumentException("未找到资源: " + uri);
        }
        
        log.info("读取MCP资源: {}", uri);
        return provider.readResource(uri);
    }
    
    /**
     * 检查资源是否存在
     */
    public boolean hasResource(String uri) {
        return resources.containsKey(uri);
    }
    
    /**
     * 获取资源数量
     */
    public int getResourceCount() {
        return resources.size();
    }
}








