package com.cuixing.aitestproject.mcp.tools;

import com.cuixing.aitestproject.mcp.core.McpToolExecutor;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema.PropertySchema;
import com.cuixing.aitestproject.tool.StockTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP 股票工具 - 包装现有 StockTool
 */
@Component
public class McpStockTool implements McpToolExecutor {
    
    private final StockTool stockTool;
    private final McpTool toolDefinition;
    
    @Autowired
    public McpStockTool(StockTool stockTool) {
        this.stockTool = stockTool;
        this.toolDefinition = buildToolDefinition();
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
        String ticker = (String) arguments.get("ticker");
        
        if (ticker == null || ticker.trim().isEmpty()) {
            return Map.of("error", "股票代码参数不能为空");
        }
        
        return stockTool.get_stock(ticker);
    }
    
    @Override
    public McpTool getToolDefinition() {
        return toolDefinition;
    }
    
    private McpTool buildToolDefinition() {
        // 构建输入参数Schema
        McpToolInputSchema inputSchema = McpToolInputSchema.builder()
            .type("object")
            .properties(Map.of(
                "ticker", PropertySchema.builder()
                    .type("string")
                    .description("股票代码，如：AAPL（苹果）、TSLA（特斯拉）、MSFT（微软）等")
                    .build()
            ))
            .required(List.of("ticker"))
            .build();
        
        // 构建工具定义
        return McpTool.builder()
            .name("get_stock")
            .description("""
                查询股票的实时价格信息。
                
                功能：
                - 查询指定股票代码的实时价格
                - 返回股票代码、当前价格、涨跌幅信息
                
                返回数据包括：
                - ticker: 股票代码
                - price: 当前价格（美元）
                - change: 涨跌幅（百分比）
                
                这些信息可用于回答关于该股票的后续问题，如投资建议、价格走势等。
                """)
            .inputSchema(inputSchema)
            .metadata(Map.of(
                "category", "finance",
                "provider", "mock",
                "version", "1.0",
                "note", "当前为模拟数据，生产环境需接入真实股票API"
            ))
            .build();
    }
}








