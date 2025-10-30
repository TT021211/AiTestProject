package com.cuixing.aitestproject.mcp.core;

import com.cuixing.aitestproject.mcp.model.McpResource;

/**
 * MCP 资源提供者接口
 */
@FunctionalInterface
public interface McpResourceProvider {
    /**
     * 读取资源内容
     * 
     * @param uri 资源URI
     * @return 资源内容
     * @throws Exception 读取异常
     */
    Object readResource(String uri) throws Exception;
    
    /**
     * 获取资源定义（可选实现）
     */
    default McpResource getResourceDefinition() {
        return null;
    }
}








