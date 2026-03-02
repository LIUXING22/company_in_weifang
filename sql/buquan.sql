USE `ry-vue`;
CREATE TABLE `sys_dingtalk_user` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `userid` varchar(64) NOT NULL COMMENT '钉钉用户ID',
                                   `name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
                                   `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
                                   `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
                                   `dept_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
                                   `job_number` varchar(50) DEFAULT NULL COMMENT '工号',
                                   `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                                   `position` varchar(100) DEFAULT NULL COMMENT '职位',
                                   `active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
                                   `sync_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后同步时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钉钉用户信息表';