package com.ruoyi.system.service;

import com.ruoyi.common.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//这里有循环依赖，需要解决
@RunWith(SpringRunner.class)
@SpringBootTest(classes = com.ruoyi.RuoYiApplication.class) // 指向启动类
public class AttendanceSyncServiceTest {
    @Resource
    private AttendanceSyncService attendanceSyncService;

    @Test
    public void testSyncAttendance() {
        String userId = "manager4220";
        List<String> columnIds = Arrays.asList("129339038");
        Date fromDate = DateUtils.parseDate("2026-02-16 12:12:12");
        Date toDate = DateUtils.parseDate("2026-02-17 12:13:12");

        int count = attendanceSyncService.syncAttendance(userId, columnIds, fromDate, toDate);
        System.out.println("同步数据条数：" + count);
        // 可以添加断言验证数据库有数据
    }


    @Test
    public void testGetAllUserIds() {
        List<String> userIds = attendanceSyncService.getAllUserIds();
        System.out.println("总用户数：" + userIds.size());
        // 可以输出前10个用户ID
        userIds.stream().limit(10).forEach(System.out::println);
    }
}