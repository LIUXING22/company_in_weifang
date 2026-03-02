package com.ruoyi.system.domain;

/**
 * 存储钉钉的个人登录信息，用来调用我个人用户下的接口调用权限
 */


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkConfig {
    private String appkey;
    private String appsecret;
    // getter/setter
}
