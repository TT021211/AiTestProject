package com.cuixing.aitestproject.mcp.tools;

import com.cuixing.aitestproject.mcp.core.McpToolExecutor;
import com.cuixing.aitestproject.mcp.model.McpTool;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema;
import com.cuixing.aitestproject.mcp.model.McpToolInputSchema.PropertySchema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP 计算器工具示例
 * 演示如何扩展新工具
 */
@Component
public class McpCalculatorTool implements McpToolExecutor {
    
    private final McpTool toolDefinition;
    
    public McpCalculatorTool() {
        this.toolDefinition = buildToolDefinition();
    }
    
    @Override
    public Object execute(String toolName, Map<String, Object> arguments) throws Exception {
        // 获取参数
        double a = Double.parseDouble(arguments.get("a").toString());
        double b = Double.parseDouble(arguments.get("b").toString());
        String operation = (String) arguments.get("operation");
        
        // 执行计算
        double result = switch (operation) {
            case "add" -> a + b;
            case "subtract" -> a - b;
            case "multiply" -> a * b;
            case "divide" -> {
                if (b == 0) {
                    throw new IllegalArgumentException("除数不能为0");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("不支持的操作: " + operation);
        };
        
        // 返回结果
        return Map.of(
            "operation", operation,
            "a", a,
            "b", b,
            "result", result,
            "expression", String.format("%.2f %s %.2f = %.2f", a, getOperatorSymbol(operation), b, result)
        );
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
                "a", PropertySchema.builder()
                    .type("number")
                    .description("第一个数字")
                    .build(),
                "b", PropertySchema.builder()
                    .type("number")
                    .description("第二个数字")
                    .build(),
                "operation", PropertySchema.builder()
                    .type("string")
                    .description("运算类型：add(加), subtract(减), multiply(乘), divide(除)")
                    .enumValues(List.of("add", "subtract", "multiply", "divide"))
                    .build()
            ))
            .required(List.of("a", "b", "operation"))
            .build();
        
        // 构建工具定义
        return McpTool.builder()
            .name("calculator")
            .description("""
                执行基本数学计算。
                
                功能：
                - 加法（add）
                - 减法（subtract）
                - 乘法（multiply）
                - 除法（divide）
                
                返回数据包括：
                - operation: 运算类型
                - a: 第一个数字
                - b: 第二个数字
                - result: 计算结果
                - expression: 完整表达式
                
                示例：calculator(a=10, b=5, operation=add) => 15
                """)
            .inputSchema(inputSchema)
            .metadata(Map.of(
                "category", "math",
                "version", "1.0",
                "author", "MCP Team"
            ))
            .build();
    }
    
    private String getOperatorSymbol(String operation) {
        return switch (operation) {
            case "add" -> "+";
            case "subtract" -> "-";
            case "multiply" -> "×";
            case "divide" -> "÷";
            default -> operation;
        };
    }
}








