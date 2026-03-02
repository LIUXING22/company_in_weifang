package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.AttendanceRecord;

import java.util.Date;
import java.util.List;

/**
 * 创建一个mapper，用来操作考勤打卡数据库表的处理
 */
public interface AttendanceRecordMapper {
    int insert(AttendanceRecord record);
    AttendanceRecord selectByUnique(String userId, Long columnId, Date statDate);
    // 其他方法按需添加
}