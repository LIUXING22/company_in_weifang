package com.ruoyi.quartz.task;

import com.ruoyi.system.service.AttendanceSyncService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component("attendanceTask")
public class AttendanceTask {
    @Resource
    private AttendanceSyncService attendanceSyncService;

    public void syncYesterday() {
        // 示例：同步昨天一天的数据
        Date toDate = new Date();
        Date fromDate = DateUtils.addDays(toDate, -1);
        String userId = "manager4220"; // 可从配置读取
        List<String> columnIds = Arrays.asList("129339038"); // 从配置读取

        attendanceSyncService.syncAttendance(userId, columnIds, fromDate, toDate);
    }
}