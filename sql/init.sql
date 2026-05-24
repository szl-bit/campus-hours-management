-- ============================================================
-- 校园学时管理系统 - 数据库初始化脚本
-- 数据库：MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_hours
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_hours;

-- ---------------------------------------------------
-- 用户表（学生/管理员）
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `student_id`  VARCHAR(20)  NOT NULL UNIQUE COMMENT '学号',
  `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `role`        ENUM('student','admin') NOT NULL DEFAULT 'student' COMMENT '角色',
  `class_name`  VARCHAR(100) DEFAULT NULL COMMENT '班级',
  `major`       VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_role` (`role`),
  INDEX `idx_class` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------
-- 学时申请单表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `application` (
  `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `applicant_id`  BIGINT       NOT NULL COMMENT '申请人ID',
  `activity_name` VARCHAR(200) NOT NULL COMMENT '活动名称',
  `activity_type` ENUM('academic','sports','volunteer','competition','lecture','other')
                 NOT NULL DEFAULT 'other' COMMENT '活动类型',
  `hours`         DECIMAL(5,1) NOT NULL COMMENT '申请学时数',
  `description`   TEXT         DEFAULT NULL COMMENT '活动描述',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
  `status`        ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending'
                 COMMENT '审核状态',
  `reviewer_id`   BIGINT       DEFAULT NULL COMMENT '审核人ID',
  `review_comment` TEXT        DEFAULT NULL COMMENT '审核意见',
  `reviewed_at`   DATETIME     DEFAULT NULL COMMENT '审核时间',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_applicant` (`applicant_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_type` (`activity_type`),
  CONSTRAINT `fk_app_user` FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学时申请表';

-- ---------------------------------------------------
-- 学时总记录表（统计汇总）
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `hours_record` (
  `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
  `total_hours`   DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '总学时',
  `academic_hours` DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '学术类学时',
  `sports_hours`  DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '文体类学时',
  `volunteer_hours` DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '志愿类学时',
  `competition_hours` DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '竞赛类学时',
  `lecture_hours` DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '讲座类学时',
  `other_hours`   DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '其他学时',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user` (`user_id`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学时汇总表';

-- ---------------------------------------------------
-- 操作日志表
-- ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `operator_id`   BIGINT       NOT NULL COMMENT '操作人ID',
  `action`        VARCHAR(50)  NOT NULL COMMENT '操作类型',
  `target_type`   VARCHAR(50)  DEFAULT NULL COMMENT '操作对象类型',
  `target_id`     BIGINT       DEFAULT NULL COMMENT '操作对象ID',
  `detail`        TEXT         DEFAULT NULL COMMENT '操作详情',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_operator` (`operator_id`),
  INDEX `idx_action` (`action`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 测试数据
-- ============================================================
INSERT INTO `user` (`student_id`, `name`, `password`, `role`, `class_name`, `major`) VALUES
('2023001', '张三', 'e10adc3949ba59abbe56e057f20f883e', 'student', '信息管理2101', '信息管理与信息系统'),
('2023002', '李四', 'e10adc3949ba59abbe56e057f20f883e', 'student', '信息管理2101', '信息管理与信息系统'),
('2023003', '王五', 'e10adc3949ba59abbe56e057f20f883e', 'student', '信息管理2102', '信息管理与信息系统'),
('admin001', '管理员', 'e10adc3949ba59abbe56e057f20f883e', 'admin', NULL, NULL);

INSERT INTO `application` (`applicant_id`, `activity_name`, `activity_type`, `hours`, `description`, `status`) VALUES
(1, '数据结构学术竞赛', 'competition', 8.0, '参加校级数据结构竞赛并获得三等奖', 'pending'),
(1, '校园马拉松志愿者', 'volunteer', 4.0, '担任校园马拉松赛道引导志愿者', 'approved'),
(2, 'Python编程讲座', 'lecture', 2.0, '参加学院组织的Python数据分析讲座', 'pending'),
(2, '院运动会', 'sports', 3.0, '参加院运动会800米项目', 'rejected'),
(3, '机器学习研讨会', 'academic', 6.0, '参加为期两天的机器学习暑期研讨班', 'approved'),
(1, '社区环保活动', 'volunteer', 3.5, '参加社区环保宣传与垃圾分类志愿活动', 'pending');

INSERT INTO `hours_record` (`user_id`, `total_hours`, `academic_hours`, `sports_hours`, `volunteer_hours`, `competition_hours`, `lecture_hours`, `other_hours`) VALUES
(1, 15.5, 0, 0, 7.5, 8.0, 0, 0),
(2, 5.0, 0, 3.0, 0, 0, 2.0, 0),
(3, 6.0, 6.0, 0, 0, 0, 0, 0);
