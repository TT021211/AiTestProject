package com.cuixing.aitestproject.mcp.config;

import com.cuixing.aitestproject.mcp.core.McpResourceRegistry;
import com.cuixing.aitestproject.mcp.resources.ConversationHistoryResource;
import com.cuixing.aitestproject.mcp.resources.SystemPromptResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * MCP 资源配置 - 自动注册资源
 */
@Configuration
public class McpResourceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(McpResourceConfiguration.class);
    
    private final McpResourceRegistry resourceRegistry;
    private final ConversationHistoryResource conversationHistoryResource;
    private final SystemPromptResource systemPromptResource;
    
    @Autowired
    public McpResourceConfiguration(
            McpResourceRegistry resourceRegistry,
            ConversationHistoryResource conversationHistoryResource,
            SystemPromptResource systemPromptResource) {
        this.resourceRegistry = resourceRegistry;
        this.conversationHistoryResource = conversationHistoryResource;
        this.systemPromptResource = systemPromptResource;
    }
    
    @PostConstruct
    public void registerResources() {
        log.info("开始注册MCP资源...");
        
        // 注册对话历史资源
        resourceRegistry.registerResource(
            conversationHistoryResource.getResourceDefinition(),
            conversationHistoryResource
        );
        
        // 注册系统提示词资源
        resourceRegistry.registerResource(
            systemPromptResource.getResourceDefinition(),
            systemPromptResource
        );
        
        log.info("MCP资源注册完成，共 {} 个资源", resourceRegistry.getResourceCount());
    }
}








