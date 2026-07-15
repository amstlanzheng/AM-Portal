package com.am.system.service;

import com.am.common.constant.CommonConstant;
import com.am.common.util.RedisUtil;
import com.am.system.entity.SysConfig;
import com.am.system.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    private final RedisUtil redisUtil;

    public String getConfig(String key) {
        String cacheKey = CommonConstant.REDIS_CONFIG_PREFIX + key;
        String value = redisUtil.get(cacheKey);
        if (value != null) {
            return value;
        }
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null) {
            redisUtil.set(cacheKey, config.getConfigValue(), 1, TimeUnit.HOURS);
            return config.getConfigValue();
        }
        return null;
    }

    public void setConfig(String key, String value) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            save(config);
        }
        // 刷新缓存
        String cacheKey = CommonConstant.REDIS_CONFIG_PREFIX + key;
        redisUtil.set(cacheKey, value, 1, TimeUnit.HOURS);
    }

    public Map<String, String> getAllConfigs() {
        List<SysConfig> configs = list();
        Map<String, String> map = new HashMap<>();
        configs.forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return map;
    }

    public Map<String, String> getHomePageInfo() {
        String cacheKey = CommonConstant.REDIS_HOME_PAGE_KEY;
        // 从缓存获取首页信息
        Map<String, String> result = new HashMap<>();
        String[] homeKeys = {"site_title", "site_subtitle", "site_description", "site_avatar",
                "site_github", "site_gitee", "site_email", "site_qq", "site_wechat_qrcode", "site_tech_stack"};
        for (String key : homeKeys) {
            String value = getConfig(key);
            if (StringUtils.hasText(value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    public void updateHomePageInfo(Map<String, String> configs) {
        configs.forEach(this::setConfig);
        // 清除首页缓存
        redisUtil.delete(CommonConstant.REDIS_HOME_PAGE_KEY);
    }

    public boolean isAiEnabled() {
        String value = getConfig("ai_enabled");
        return "true".equalsIgnoreCase(value);
    }
}
