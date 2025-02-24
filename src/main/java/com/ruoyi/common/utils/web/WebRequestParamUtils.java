package com.ruoyi.common.utils.web;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * web请求参数工具类
 *
 * @author zhuzhen
 * @date 2025/2/24 15:35
 * @description: 详细说明
 */
public class WebRequestParamUtils {
    /**
     * 检查日期参数格式是否为空
     * @param baseEntity 请求参数实体
     */
    public static void validateDateMissing(BaseEntity baseEntity) {
        String beginTime = baseEntity.getParams() != null ? String.valueOf(baseEntity.getParams().get("beginTime")) : null;
        String endTime = baseEntity.getParams() != null ? String.valueOf(baseEntity.getParams().get("endTime")) : null;
        if (StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime)) {
            throw new ServiceException("日期参数不能为空");
        }
    }

    /**
     * 检查日期参数格式，以及跨越天数
     * @param baseEntity 请求参数实体
     * @param diffDaysThreshold 相差天数最大阈值
     */
    public static void validateDateSpan(BaseEntity baseEntity, int diffDaysThreshold) {
        String beginTime = baseEntity.getParams() != null ? String.valueOf(baseEntity.getParams().get("beginTime")) : null;
        String endTime = baseEntity.getParams() != null ? String.valueOf(baseEntity.getParams().get("endTime")) : null;

        if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
            int diffDays = 0;
            try {
                diffDays = DateUtils.differentDaysByDateFormatStr(beginTime, endTime);
            } catch (Exception e) {
                throw new ServiceException("日期格式错误");
            }
            if (diffDays > diffDaysThreshold) {
                throw new ServiceException("开始日期和结束日期跨越天数不超过" + diffDaysThreshold + "天");
            }
        }
    }
}
