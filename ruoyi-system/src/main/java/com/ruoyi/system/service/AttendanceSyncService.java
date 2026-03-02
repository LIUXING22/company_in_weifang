package com.ruoyi.system.service;

import java.util.Date;
import java.util.List;

public interface AttendanceSyncService {
    public int syncAttendance(String userId, List<String> columnIdList, Date fromDate, Date toDate);

    public List<String> getAllUserIds();

    /**
     * 递归获取所有部门ID（包括子部门）
     */
    List<Long> getAllDeptIds(String accessToken, Long deptId);


    /**
     * 获取指定部门下的所有用户ID（处理分页）
     */
    List<String> getUserIdsByDept(String accessToken, Long deptId);
}
