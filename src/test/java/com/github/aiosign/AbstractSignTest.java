package com.github.aiosign;

import com.github.aiosign.client.SignClient;
import com.github.aiosign.client.support.DefaultSignClient;
import com.github.aiosign.module.request.TokenRequest;
import com.github.aiosign.module.response.TokenResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * 基础的测试类，实例化请求客户端
 *
 * @author modificial
 * @description
 * @since 2020/5/19
 */
public abstract class AbstractSignTest {

    protected SignClient signClient;

    /**
     * 初始化客户端
     */
    @Before
    public void init() {
        signClient = new DefaultSignClient("http://192.168.17.23:32286", "728283996928757760",
                "GgorGhRTdTSWSubpPV");
    }

    /**
     * 获取签名值
     */
    @Test
    public void getToken() {
        TokenRequest tokenRequest = new TokenRequest("728283996928757760", "GgorGhRTdTSWSubpPV");
        TokenResponse response = signClient.execute(tokenRequest);
        if (response.isSuccess()) {
            System.out.println("获取token成功，响应值为" + response.getData());
        }
    }
}
