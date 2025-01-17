package com.github.aiosign.client.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.aiosign.base.*;
import com.github.aiosign.client.SignClient;
import com.github.aiosign.enums.ContentType;
import com.github.aiosign.module.request.TokenRequest;
import com.github.aiosign.module.response.TokenResponse;
import com.github.aiosign.utils.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.aiosign.utils.WebUtils.downLoadFromUrl;

/**
 * @author WeiShuai
 * @Date 2020/11/22 0022 下午 03:45
 */
@Data
@Slf4j
public abstract class ProxyAbstractSignClient implements SignClient {
    /**
     * 默认连接超时时间
     */
    public static final Integer CONNECT_TIME_OUT = 40000;
    /**
     * 默认读取超时时间
     */
    public static final Integer READ_TIME_OUT = 40000;
    /**
     * 网关根地址 【必填】
     */
    private String rootUri;
    /**
     * 连接超时时间 【选填】
     */
    private Integer connectTimeOut;
    /**
     * 读取数据超时时间 【选填】
     */
    private Integer readTimeOut;
    /**
     * 代理的主机地址 【选填】
     */
    private String proxyHost;

    /**
     * 代理的端口号 【选填】
     */
    private int proxyPort;
    /**
     * 应用id 【必填】
     */
    private String appId;
    /**
     * 应用秘钥
     */
    private String appSecret;

    /**
     * 是否需要校验返回结果
     */
    private boolean checkResult;
    /**
     * urlTokenKey
     */
    private String urlTokenKey = "access_token";


    /**
     * 构造器
     *
     * @param rootUri     网关地址前缀
     * @param proxyHost   代理主机
     * @param proxyPort   代理端口
     * @param appId       应用id
     * @param appSecret   应用秘钥
     * @param checkResult a boolean.
     */
    public ProxyAbstractSignClient(String rootUri, String proxyHost, Integer proxyPort, String appId, String appSecret, boolean checkResult) {
        this(rootUri, CONNECT_TIME_OUT, READ_TIME_OUT, proxyHost, proxyPort, appId, appSecret, checkResult);
    }


    public ProxyAbstractSignClient(String rootUri, Integer connectTimeOut, Integer readTimeOut, String proxyHost, int proxyPort, String appId, String appSecret, boolean checkResult) {
        this.rootUri = rootUri;
        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.appId = appId;
        this.appSecret = appSecret;
        this.checkResult = checkResult;
    }


    /**
     * 不使用代理,可设置连接时间的构造器
     *
     * @param rootUri           网关地址前缀
     * @param appId             应用id
     * @param appSecret         应用秘钥
     * @param connectTimeOut    网络连接超时时间
     * @param readTimeOut       网络读取超时时间(推荐与connectTimeOut一致)
     */
    public ProxyAbstractSignClient(String rootUri, String appId, String appSecret,  Integer connectTimeOut, Integer readTimeOut) {
        this(rootUri, connectTimeOut, readTimeOut,null, 0, appId, appSecret, false);
    }

    /**
     * 不使用代理的构造器
     *
     * @param rootUri   网关地址前缀
     * @param appId     应用id
     * @param appSecret 应用秘钥
     */
    public ProxyAbstractSignClient(String rootUri, String appId, String appSecret) {
        this(rootUri, null, 0, appId, appSecret, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 执行http请求，并返回数据
     */
    @Override
    public <T extends AbstractSignResponse> T execute(AbstractSignRequest<T> signRequest) {
        Assert.notNull(signRequest, "请求对象不能为空");
        RequestInfo<T> requestInfo = Optional.ofNullable(signRequest.getRequestInfo()).orElseThrow(() -> new IllegalArgumentException("参数不能为空"));
        //对参数进行检验
        requestInfo.check();
        try {
            //发起请求，解析结果
            T t = requestForResult(requestInfo);
            return t;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("调用api发生错误");
    }

    @Override
    public <T extends AbstractSignResponse> byte[] executeDownload(AbstractSignRequest<T> signRequest) {
        Assert.notNull(signRequest, "请求对象不能为空");
        RequestInfo<T> requestInfo = Optional.ofNullable(signRequest.getRequestInfo()).orElseThrow(() -> new IllegalArgumentException("参数不能为空"));
        //对参数进行检验
        requestInfo.check();
        try {
            //发起请求，解析结果
            return requestForBytes(requestInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("调用api发生错误");
    }

    /**
     * 执行相关组合业务
     */
    @Override
    public <T extends AbstractSignResponse> T execute(AbstractComposeRequest<T> composeRequest) {
        return composeRequest.execute(this);
    }

    /**
     * 下载文件
     *
     * @param fileId       文件id
     * @param outputStream 输出流
     */
    @Override
    public void download(String baseUri, String fileId, OutputStream outputStream) throws FileNotFoundException {
        String apiUrl = rootUri + baseUri;
        String token = TokenManager.getToken(this);
        String uriBuild = URLUtil.appendUrl(apiUrl, new HashMap<String, Object>(2) {{
            put(urlTokenKey, token);
            put("fileId", fileId);
        }});
        // 更变为代理数据
        uriBuild = builderCustomParams(uriBuild);
        downLoadFromUrl(uriBuild, outputStream, connectTimeOut, readTimeOut);
    }

    /**
     * 下载文件
     *
     * @param fileId       文件id
     */
    @Override
    public byte[] download(String baseUri, String fileId) throws FileNotFoundException {
        String apiUrl = rootUri + baseUri;
        String token = TokenManager.getToken(this);
        String uriBuild = URLUtil.appendUrl(apiUrl, new HashMap<String, Object>(2) {{
            put(urlTokenKey, token);
            put("fileId", fileId);
        }});
        return downLoadFromUrl(uriBuild, connectTimeOut, readTimeOut);
    }

    /**
     * 更变url参数
     *
     * @param apiUrl 原始url
     * @return 更变后URL
     */
    public abstract String builderCustomParams(String apiUrl);

    /**
     * 发起请求并获取结果
     *
     * @param requestInfo
     * @return
     * @throws IOException
     */
    private <T extends AbstractSignResponse> T requestForResult(RequestInfo<T> requestInfo) throws IOException {
        String result = null;
        String apiUrl = rootUri + requestInfo.getApiUri();
        //如果需要传递token
        if (requestInfo.isNeedToken()) {
            String token = TokenManager.getToken(this);
            if (StringUtils.isEmpty(token)) {
                throw new RuntimeException("获取token失败");
            }
            apiUrl = URLUtil.appendUrl(apiUrl, new HashMap<String, Object>(1) {{
                put(urlTokenKey, token);
            }});
        }
        // 加载自定义 url参数 及信息
        apiUrl = builderCustomParams(apiUrl);
        // 解析请求方式
        switch (requestInfo.getMethod()) {
            case POST:
                if (requestInfo.getContentType().equals(ContentType.JSON)) {
                    ObjectMapper objectMapper = ObjectMapperHolder.INSTANCE.getInstance();
                    Serializable requestBody = requestInfo.getRequestBody();
                    // 关键代码
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    String body = objectMapper.writeValueAsString(requestBody);
                    String sign = SignUtils.createSign(body, appSecret);
                    result = WebUtils.doPostJson(apiUrl, body, this.connectTimeOut, this.readTimeOut, proxyHost, proxyPort, sign);
                } else if (requestInfo.getContentType().equals(ContentType.FORM_URLENCODED)) {
                    Map<String, String> params = requestInfo.getParams();
                    result = WebUtils.doPost(apiUrl, params, "UTF-8", connectTimeOut, readTimeOut, proxyHost, proxyPort);
                } else if (requestInfo.getContentType().equals(ContentType.MULTIPART)) {
                    Map<String, String> params = requestInfo.getParams();
                    Map<String, FileItem> fileParams = requestInfo.getFileParams();
                    result = WebUtils.doPost(apiUrl, params, fileParams, "UTF-8", connectTimeOut, readTimeOut, proxyHost, proxyPort);
                }
                break;
            case GET:
                Map<String, String> params = requestInfo.getParams();
                result = WebUtils.doGet(apiUrl, params);
                break;
            default:
                break;
        }
        log.info("当前请求地址为{},返回结果为{}", apiUrl, result);
        Assert.hasText(result, "调用api失败");
        ObjectMapper objectMapper = ObjectMapperHolder.INSTANCE.getInstance();
        try {
            objectMapper.readTree(result);
        } catch (IOException e) {
            throw new RuntimeException("返回的数据不是json");
        }
        return objectMapper.readValue(result, requestInfo.getResponseType());
    }

    /**
     * 发起请求并获取字节数组
     *
     * @param requestInfo
     * @return
     * @throws IOException
     */
    private <T extends AbstractSignResponse> byte[] requestForBytes(RequestInfo<T> requestInfo) throws IOException {
        byte[] result = null;
        String apiUrl = rootUri + requestInfo.getApiUri();
        //如果需要传递token
        if (requestInfo.isNeedToken()) {
            String token = TokenManager.getToken(this);
            if (StringUtils.isEmpty(token)) {
                throw new RuntimeException("获取token失败");
            }
            apiUrl = URLUtil.appendUrl(apiUrl, new HashMap<String, Object>(1) {{
                put(urlTokenKey, token);
            }});
        }
        switch (requestInfo.getMethod()) {
            case POST:
                if (requestInfo.getContentType().equals(ContentType.JSON)) {
                    ObjectMapper objectMapper = ObjectMapperHolder.INSTANCE.getInstance();
                    Serializable requestBody = requestInfo.getRequestBody();
                    // 关键代码
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    String body = objectMapper.writeValueAsString(requestBody);
                    String sign = SignUtils.createSign(body, appSecret);
                    result = WebUtils.doPostJson(apiUrl, body, this.connectTimeOut, this.readTimeOut, proxyHost, proxyPort, sign, byte[].class);
                } else if (requestInfo.getContentType().equals(ContentType.FORM_URLENCODED)) {
                    Map<String, String> params = requestInfo.getParams();
                    result = WebUtils.doPost(apiUrl, params, "UTF-8", connectTimeOut, readTimeOut, proxyHost, proxyPort, byte[].class);
                } else if (requestInfo.getContentType().equals(ContentType.MULTIPART)) {
                    Map<String, String> params = requestInfo.getParams();
                    Map<String, FileItem> fileParams = requestInfo.getFileParams();
                    result = WebUtils.doPost(apiUrl, params, fileParams, "UTF-8", connectTimeOut, readTimeOut, proxyHost, proxyPort, byte[].class);
                }
                break;
            case GET:
                Map<String, String> params = requestInfo.getParams();
                result = WebUtils.doGet(apiUrl, params, byte[].class);
                break;
            default:
                break;
        }
        Assert.state(result != null && result.length > 0, "调用api失败");
        log.info("当前请求地址为{},返回字节数组长度为{}", apiUrl, result.length);
        return result;
    }

    /**
     * 重写hashcode方法，只要是appid和appsecret相同，其hashcode值就相同
     *
     * @return
     */
    @Override
    public int hashCode() {
        String key = this.appId + this.appSecret;
        return ObjectUtils.nullSafeHashCode(key);
    }

    /**
     * 重写其equals方法，只要是appid和appsecret相同，就认为两个对象是相等的
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ProxyAbstractSignClient other = (ProxyAbstractSignClient) obj;
        return this.appSecret.equals(other.appSecret) && this.appId.equals(other.appId);
    }

    @Override
    public String getToken() {
        TokenRequest tokenRequest = new TokenRequest(this.getAppId(), this.getAppSecret());
        TokenResponse response = this.execute(tokenRequest);
        if (response.isSuccess()) {
            return response.getData().getAccessToken();
        }
        return null;
    }

    @Override
    public String downloadFileURL(String fileId) {
        return URLUtil.appendUrl(this.rootUri, new HashMap<String, Object>() {{
            put(urlTokenKey, getToken());
            put("fileId", fileId);
        }});
    }
}
