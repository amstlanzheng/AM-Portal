package com.am.system.service;

import com.am.common.exception.BusinessException;
import com.am.common.result.ResultCode;
import com.am.system.entity.DailyTip;
import com.am.system.mapper.DailyTipMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 每日小贴士：后台维护，首页随机展示
 */
@Service
public class DailyTipService extends ServiceImpl<DailyTipMapper, DailyTip> {

    /** 后台列表：按创建时间倒序 */
    public List<DailyTip> listAll() {
        return list(new LambdaQueryWrapper<DailyTip>().orderByDesc(DailyTip::getId));
    }

    /** 首页随机取一条已启用的贴士，无数据返回 null */
    public DailyTip randomEnabled() {
        List<DailyTip> tips = list(new LambdaQueryWrapper<DailyTip>().eq(DailyTip::getIsEnabled, true));
        if (tips.isEmpty()) {
            return null;
        }
        return tips.get(ThreadLocalRandom.current().nextInt(tips.size()));
    }

    public void add(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        DailyTip tip = new DailyTip();
        tip.setContent(content.trim());
        tip.setIsEnabled(true);
        save(tip);
    }
}
