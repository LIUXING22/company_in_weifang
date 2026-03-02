package com.ruoyi.system.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkConfig {
    private String appkey;
    private String appsecret;

    public String getAppkey() { return appkey; }
    public void setAppkey(String appkey) { this.appkey = appkey; }
    public String getAppsecret() { return appsecret; }
    public void setAppsecret(String appsecret) { this.appsecret = appsecret; }
}