package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.DingTalkConfig;
import com.ruoyi.system.service.DingTalkTokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class DingTalkTokenServiceImpl implements DingTalkTokenService {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private DingTalkConfig dingTalkConfig;

    private static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";

    public String getAccessToken() {
        Map<String, String> params = new HashMap<>();
        params.put("appkey", dingTalkConfig.getAppkey());      // 改回 appkey
        params.put("appsecret", dingTalkConfig.getAppsecret()); // 改回 appsecret
        Map<String, Object> response = restTemplate.getForObject(TOKEN_URL + "?appkey={appkey}&appsecret={appsecret}", Map.class, params);
        if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
            return (String) response.get("access_token");
        } else {
            throw new RuntimeException("获取钉钉access_token失败：" + response);
        }
    }



}

