package com.cuixing.aitestproject.mcp.tools;

import com.cuixing.aitestproject.mcp.core.McpToolExecutor;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema.PropertySchema;
import com.cuixing.aitestproject.tool.WeatherTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP 天气工具 - 包装现有 WeatherTool
 */
@Component
public class McpWeatherTool implements McpToolExecutor {
    
    private final WeatherTool weatherTool;
    private final McpTool toolDefinition;
    
    @Autowired
    public McpWeatherTool(WeatherTool weatherTool) {
        this.weatherTool = weatherTool;
        this.toolDefinition = buildToolDefinition();
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
        String city = (String) arguments.get("city");
        String date = (String) arguments.getOrDefault("date", null);
        
        if (city == null || city.trim().isEmpty()) {
            return Map.of("error", "城市参数不能为空");
        }
        
        return weatherTool.get_weather(city, date);
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
                "city", PropertySchema.builder()
                    .type("string")
                    .description("城市名称，如：北京、上海、鹰潭等")
                    .build(),
                "date", PropertySchema.builder()
                    .type("string")
                    .description("日期（可选），格式：YYYY-MM-DD")
                    .build()
            ))
            .required(List.of("city"))
            .build();
        
        // 构建工具定义
        return McpTool.builder()
            .name("get_weather")
            .description("""
                获取指定城市的实时天气数据。
                
                功能：
                - 查询城市的实时天气信息
                - 返回温度、天气描述、湿度、风力等详细信息
                - 支持中国主要城市查询
                
                返回数据包括：
                - temp: 实时温度（°C）
                - feelsLike: 体感温度（°C）
                - text: 天气描述（如"晴"、"多云"等）
                - humidity: 相对湿度（%）
                - windDir: 风向
                - windScale: 风力等级
                - precip: 降水量（mm）
                - pressure: 大气压强（hPa）
                - vis: 能见度（km）
                - cloud: 云量（%）
                
                这些信息可用于回答后续问题，如适合什么活动、穿什么衣服等。
                """)
            .inputSchema(inputSchema)
            .metadata(Map.of(
                "category", "weather",
                "provider", "qweather",
                "version", "1.0"
            ))
            .build();
    }
}








