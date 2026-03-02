package com.ruoyi.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.AttendanceRecord;
import com.ruoyi.system.mapper.AttendanceRecordMapper;
import com.ruoyi.system.service.AttendanceSyncService;
import com.ruoyi.system.service.DingTalkTokenService;
import com.ruoyi.system.service.AttendanceSyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.util.*;

@Service
public class AttendanceSyncServiceImpl implements AttendanceSyncService {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private DingTalkTokenService dingTalkTokenService;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    private static final String API_URL = "https://oapi.dingtalk.com/topapi/attendance/getcolumnval"; // 注意拼写正确


    @Transactional(rollbackFor = Exception.class)
    public int syncAttendance(String userId, List<String> columnIdList, Date fromDate, Date toDate) {
        // 1. 获取 access_token
        String accessToken = dingTalkTokenService.getAccessToken();
        String url = API_URL + "?access_token=" + accessToken;

        // 2. 构造请求体（严格遵循钉钉文档）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userid", userId);
        requestBody.put("column_id_list", String.join(",", columnIdList)); // 关键修改
        requestBody.put("from_date", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", fromDate));
        requestBody.put("to_date", DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", toDate));

        // 可选：打印请求体，便于调试
        try {
            System.out.println("请求体JSON: " + new ObjectMapper().writeValueAsString(requestBody));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 发送请求
        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(url, requestBody, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("调用钉钉接口网络异常", e);
        }

        // 4. 解析响应
        if (response == null) {
            throw new RuntimeException("钉钉接口返回空");
        }

        // 兼容错误码字段
        Integer errcode = null;
        if (response.containsKey("errcode")) {
            errcode = (Integer) response.get("errcode");
        } else if (response.containsKey("errorcode")) {
            errcode = (Integer) response.get("errorcode");
        }
        if (errcode == null || errcode != 0) {
            throw new RuntimeException("钉钉接口业务错误：" + response);
        }

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        if (result == null) {
            return 0;
        }
        List<Map<String, Object>> columnValsList = (List<Map<String, Object>>) result.get("column_vals");
        if (columnValsList == null) {
            return 0;
        }

        int count = 0;
        for (Map<String, Object> columnItem : columnValsList) {
            Long columnId = ((Number) columnItem.get("id")).longValue();
            List<Map<String, Object>> dailyVals = (List<Map<String, Object>>) columnItem.get("column_vals");
            if (dailyVals != null) {
                for (Map<String, Object> daily : dailyVals) {
                    String dateStr = (String) daily.get("date");
                    String value = (String) daily.get("value");
                    Date statDate = DateUtils.parseDate(dateStr);

                    // 去重
                    AttendanceRecord exist = attendanceRecordMapper.selectByUnique(userId, columnId, statDate);
                    if (exist == null) {
                        AttendanceRecord record = new AttendanceRecord();
                        record.setUserId(userId);
                        record.setColumnId(columnId);
                        record.setStatDate(statDate);
                        record.setColumnValue(value);
                        attendanceRecordMapper.insert(record);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public List<String> getAllUserIds() {
        String accessToken = dingTalkTokenService.getAccessToken();
        List<String> allUserIds = new ArrayList<>();

        // 1. 获取根部门ID（通常为1，也可从配置读取）
        Long rootDeptId = 1L;

        // 2. 获取所有部门ID（包含子部门）
        List<Long> allDeptIds = getAllDeptIds(accessToken, rootDeptId);

        // 3. 遍历部门，获取每个部门下的用户ID（分页）
        for (Long deptId : allDeptIds) {
            List<String> userIds = getUserIdsByDept(accessToken, deptId);
            allUserIds.addAll(userIds);
            // 避免请求过快，休眠一小段时间（可选）
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return allUserIds;
    }

    /**
     * 递归获取所有部门ID（包括子部门）
     */
    public List<Long> getAllDeptIds(String accessToken, Long deptId) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);

        String url = "https://oapi.dingtalk.com/topapi/v2/department/listsub?access_token=" + accessToken;
        Map<String, Object> request = new HashMap<>();
        request.put("dept_id", deptId);
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
            // 如果 result 直接是 List
            List<Map<String, Object>> deptList = (List<Map<String, Object>>) response.get("result");
            if (deptList != null) {
                for (Map<String, Object> dept : deptList) {
                    Long childId = ((Number) dept.get("dept_id")).longValue();
                    result.addAll(getAllDeptIds(accessToken, childId)); // 递归
                }
            }
        } else {
            throw new RuntimeException("获取子部门失败：" + response);
        }
        return result;
    }
    /**
     * 获取指定部门下的所有用户ID（处理分页）
     */
    public List<String> getUserIdsByDept(String accessToken, Long deptId) {
        List<String> userIds = new ArrayList<>();
        Long cursor = 0L;
        Integer size = 100; // 每页最大100

        while (true) {
            String url = "https://oapi.dingtalk.com/topapi/v2/user/list?access_token=" + accessToken;
            Map<String, Object> request = new HashMap<>();
            request.put("dept_id", deptId);
            request.put("cursor", cursor);
            request.put("size", size);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
                Map<String, Object> resultMap = (Map<String, Object>) response.get("result");
                if (resultMap != null) {
                    List<Map<String, Object>> userList = (List<Map<String, Object>>) resultMap.get("list");
                    if (userList != null) {
                        for (Map<String, Object> user : userList) {
                            String userId = (String) user.get("userid");
                            if (userId != null) {
                                userIds.add(userId);
                            }
                        }
                    }
                    // 检查是否有下一页
                    Boolean hasMore = (Boolean) resultMap.get("has_more");
                    if (!hasMore) {
                        break;
                    }
                    // 更新cursor
                    cursor = ((Number) resultMap.get("next_cursor")).longValue();
                } else {
                    break;
                }
            } else {
                throw new RuntimeException("获取部门用户失败：" + response);
            }
        }
        return userIds;
    }
}