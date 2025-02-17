package com.ruoyi.project.device.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.xss.Xss;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.aspectj.lang.annotation.Excel.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 设备对象 iot_device
 *
 * @author zhuzhen
 * @date 2025/2/17 15:25
 * @description: 详细说明
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private static final long serialVersionUID = 1L;

    /** 设备id */
    private Long id;

    /** 设备名称 */
    @Xss(message = "设备名称不能包含脚本字符")
    @Excel(name = "设备名称", cellType = ColumnType.STRING )
    private String name;

    /** 设备编号 */
    @Excel(name = "设备编号", cellType = ColumnType.STRING )
    private String code;

    /** 首次开机时间 */
    @Excel(name = "首次开机时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstOnlineTime;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
