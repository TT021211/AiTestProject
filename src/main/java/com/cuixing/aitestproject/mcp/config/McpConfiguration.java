package com.cuixing.aitestproject.mcp.config;

import com.cuixing.aitestproject.mcp.core.McpToolRegistry;
import com.cuixing.aitestproject.mcp.tools.McpCalculatorTool;
import com.cuixing.aitestproject.mcp.tools.McpStockTool;
import com.cuixing.aitestproject.mcp.tools.McpWeatherTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * MCP 配置 - 自动注册工具和资源
 * 
 * 🔧 工具扩展位置：在这里添加新工具的注册
 */
@Configuration
public class McpConfiguration {
    private static final Logger log = LoggerFactory.getLogger(McpConfiguration.class);
    
    private final McpToolRegistry toolRegistry;
    private final McpWeatherTool weatherTool;
    private final McpStockTool stockTool;
    private final McpCalculatorTool calculatorTool;  // 新增：计算器工具
    
    @Autowired
    public McpConfiguration(
            McpToolRegistry toolRegistry,
            McpWeatherTool weatherTool,
            McpStockTool stockTool,
            McpCalculatorTool calculatorTool) {  // 新增：注入计算器工具
        this.toolRegistry = toolRegistry;
        this.weatherTool = weatherTool;
        this.stockTool = stockTool;
        this.calculatorTool = calculatorTool;
    }
    
    @PostConstruct
    public void registerTools() {
        log.info("开始注册MCP工具...");
        
        // 注册天气工具
        toolRegistry.registerTool(
            weatherTool.getToolDefinition(),
            weatherTool
        );
        
        // 注册股票工具
        toolRegistry.registerTool(
            stockTool.getToolDefinition(),
            stockTool
        );
        
        // 🔧 新增：注册计算器工具
        // 添加新工具就在这里注册
        toolRegistry.registerTool(
            calculatorTool.getToolDefinition(),
            calculatorTool
        );
        
        log.info("MCP工具注册完成，共 {} 个工具", toolRegistry.getToolCount());
    }
}

