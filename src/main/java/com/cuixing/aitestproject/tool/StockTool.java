package com.cuixing.aitestproject.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StockTool {
    @Tool("查询股票的实时价格信息。参数ticker为股票代码(如AAPL)。返回股票代码、当前价格、涨跌幅。可用于回答后续关于该股票的问题。")
    public Map<String, Object> get_stock(String ticker) {
        // 模拟API：真实用Yahoo Finance或Alpha Vantage
        Map<String, Object> data = new HashMap<>();
        data.put("ticker", ticker);
        data.put("price", 150.5); // e.g., 当前价
        data.put("change", "+2.3%");
        return data;
    }
}