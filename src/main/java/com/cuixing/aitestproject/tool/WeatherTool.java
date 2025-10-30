package com.cuixing.aitestproject.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeatherTool {
    // 你的API_HOST, httpClient, generateJwt, convertToPinyin 等字段/方法复制进来
    private final String API_HOST = "qx5ctuuyux.re.qweatherapi.com"; // 你的Host
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("获取指定城市的实时天气数据。参数city为城市名(如鹰潭、北京)，date为日期(可选)。返回温度、天气描述、湿度、风力等详细信息。这些信息可用于回答后续问题，如适合什么活动、穿什么衣服等。")
    public Map<String, Object> get_weather(String city, String date) throws Exception {
        Map<String, Object> data = new HashMap<>();
            if (city == null || city.trim().isEmpty()) {
                data.put("error", "未提供城市名称");
                return data;
            }
            city = city.trim();

            // 生成JWT Token
            String jwtToken = generateJwt();

            // 转换中文城市名为拼音（无音调、小写），以提高查询准确性
            String queryCity = convertToPinyin(city);

            // 第一步：调用geo API获取location code，使用拼音查询
            String geoUrl = String.format("https://%s/geo/v2/city/lookup?location=%s&range=cn", API_HOST, queryCity);
            System.out.println("调用城市查找API URL (拼音): " + geoUrl + " (原城市: " + city + ")");

            String locationCode = null;
            String matchedCityName = null;
            try {
                HttpGet geoGet = new HttpGet(geoUrl);
                geoGet.setHeader("Authorization", "Bearer " + jwtToken);
                geoGet.setHeader("Content-Type", "application/json");
                geoGet.setHeader("Accept-Encoding", "gzip, deflate"); // 支持压缩

                String geoResponseStr = httpClient.execute(geoGet, response -> {
                    try (java.io.InputStream inputStream = response.getEntity().getContent();
                         java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();
                        return baos.toString(StandardCharsets.UTF_8.name());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                System.out.println("城市查找API响应: " + geoResponseStr);

                JsonNode geoJson = objectMapper.readTree(geoResponseStr);
                if ("200".equals(geoJson.path("code").asText())) {
                    JsonNode locations = geoJson.path("location");
                    if (locations.isArray() && locations.size() > 0) {
                        JsonNode location = locations.get(0); // 取第一个匹配结果（按相关性和Rank排序）
                        if (!location.isMissingNode()) {
                            locationCode = location.path("id").asText();
                            matchedCityName = location.path("name").asText();
                            data.put("matched_city", matchedCityName);
                            data.put("query_used", queryCity); // 调试：记录使用的查询词
                        }
                    }
                } else {
                    // 如果拼音失败，尝试原中文查询作为 fallback
                    String fallbackGeoUrl = String.format("https://%s/geo/v2/city/lookup?location=%s&range=cn", API_HOST, city);
                    System.out.println("拼音查询失败，尝试中文 fallback URL: " + fallbackGeoUrl);
                    // ... 重复 HTTP 调用逻辑，解析 geoJson
                    // 如果仍失败，data.put("error", ...);
                    data.put("error", "城市查找失败 (拼音/中文): " + geoJson.path("message").asText() + " (code: " + geoJson.path("code").asText() + ")");
                    return data;
                }
            } catch (Exception e) {
                data.put("error", "城市查找API异常: " + e.getMessage());
                e.printStackTrace();
                return data;
            }

            if (locationCode == null) {
                data.put("error", "未找到城市: " + city + " (拼音: " + queryCity + ") (尝试模糊搜索或提供更多细节，如上级行政区)");
                return data;
            }

            // 第二步：使用location code调用天气API /v7/weather/now
            String weatherUrl = String.format("https://%s/v7/weather/now?location=%s", API_HOST, locationCode);

            System.out.println("调用天气API URL: " + weatherUrl);

            try {
                HttpGet get = new HttpGet(weatherUrl);
                get.setHeader("Authorization", "Bearer " + jwtToken);
                get.setHeader("Content-Type", "application/json");
                get.setHeader("Accept-Encoding", "gzip, deflate");

                String responseStr = httpClient.execute(get, response -> {
                    try (java.io.InputStream inputStream = response.getEntity().getContent();
                         java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = inputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.flush();
                        return baos.toString(StandardCharsets.UTF_8.name());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                System.out.println("和风天气API响应: " + responseStr);

                JsonNode weatherJson = objectMapper.readTree(responseStr);
                if ("200".equals(weatherJson.path("code").asText())) {
                    JsonNode now = weatherJson.path("now");
                    data.put("temp", now.path("temp").asDouble()); // 实时温度（°C）
                    data.put("feelsLike", now.path("feelsLike").asInt()); // 体感温度
                    data.put("windDir", now.path("windDir").asText()); // 风向
                    data.put("windScale", now.path("windScale").asText()); // 风力（级）
                    data.put("humidity", now.path("humidity").asInt()); // 相对湿度（%）
                    data.put("precip", now.path("precip").asDouble()); // 降水量（mm）
                    data.put("pressure", now.path("pressure").asDouble()); // 大气压强（hPa）
                    data.put("vis", now.path("vis").asInt()); // 能见度（km）
                    data.put("dew", now.path("dew").asDouble()); // 露点温度（°C）
                    data.put("cloud", now.path("cloud").asInt()); // 云量（%）
                    data.put("text", now.path("text").asText()); // 天气描述（如“多云”）
                    data.put("obsTime", now.path("obsTime").asText()); // 观测时间（注意5-20min延迟）
                    data.put("date", date);
                    data.put("city", matchedCityName != null ? matchedCityName : city);
                } else {
                    data.put("error", "天气API调用失败: " + weatherJson.path("message").asText() + " (code: " + weatherJson.path("code").asText() + ")");
                }
            } catch (Exception e) {
                data.put("error", "天气API异常: " + e.getMessage());
                e.printStackTrace();
            }
        return data;
    }

    // 新增：中文转拼音方法（无音调、小写，空格连接多字）
    private String convertToPinyin(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return chinese;
        }
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            // toPinyin 返回 String[]，连接成字符串（e.g., "鹰潭" -> "ying tan"，但去空格 "yingtan"）
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(0), format);
            StringBuilder pinyin = new StringBuilder(pinyinArray[0]);
            for (int i = 1; i < chinese.length(); i++) {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), format);
                pinyin.append(pinyinArray[0]); // 无空格连接
            }
            return pinyin.toString();
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.error("拼音转换失败: " + e.getMessage());
            return chinese; // fallback 到原中文
        }
    }
    private String generateJwt() throws Exception {
        // Private key
        String privateKeyString = "-----BEGIN PRIVATE KEY-----\n" +
                "MC4CAQAwBQYDK2VwBCIEIBq7tTE452SbvFSuzWUYWdYOeHj5XCq9Du+JEPc8Qqft\n" +
                "-----END PRIVATE KEY-----\n";
        privateKeyString = privateKeyString.trim().replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").trim();
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec encoded = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = new EdDSAPrivateKey(encoded);

        // Header
        String headerJson = "{\"alg\": \"EdDSA\", \"kid\": \"KG5A6DQGTF\"}";

        // Payload
        long iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30;
        long exp = iat + 900;
        String payloadJson = "{\"sub\": \"2JDXGB3DNY\", \"iat\": " + iat + ", \"exp\": " + exp + "}";

        // Base64url header+payload
        String headerEncoded = Base64.getUrlEncoder().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String data = headerEncoded + "." + payloadEncoded;

        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);

        // Sign
        final Signature s = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        s.initSign(privateKey);
        s.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signature = s.sign();

        String signatureString = Base64.getUrlEncoder().encodeToString(signature);

        System.out.println("Signature: \n" + signatureString);

        // Print Token
        String jwt = data + "." + signatureString;
        System.out.println("JWT: \n" + jwt);
        return jwt;
    }
}