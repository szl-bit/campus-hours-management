package com.hours.service;

import com.hours.model.Application;
import com.hours.model.User;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 学时申请业务逻辑层
 * 包含：学时申请校验、权限校验、数据格式校验、审核流程
 */
public class ApplicationService {

    /** 最大单次申请学时数 */
    private static final double MAX_HOURS_PER_APPLICATION = 50.0;

    /** 最小单次申请学时数 */
    private static final double MIN_HOURS_PER_APPLICATION = 0.5;

    /** 活动名称最大长度 */
    private static final int MAX_ACTIVITY_NAME_LENGTH = 200;

    /** 允许的活动类型 */
    private static final Set<String> VALID_ACTIVITY_TYPES = new HashSet<>(Arrays.asList(
        "academic", "sports", "volunteer", "competition", "lecture", "other"
    ));

    /** 附件允许的扩展名 */
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = new HashSet<>(Arrays.asList(
        "pdf", "jpg", "jpeg", "png", "doc", "docx"
    ));

    /**
     * 提交学时申请 — 包含完整的数据校验逻辑
     * @param applicant 申请人
     * @param activityName 活动名称
     * @param activityType 活动类型
     * @param hours 申请学时
     * @param description 活动描述
     * @param attachmentUrl 附件路径（可选）
     * @return Application 申请对象（校验失败时返回null并输出错误）
     */
    public Application submitApplication(User applicant, String activityName,
                                          String activityType, double hours,
                                          String description, String attachmentUrl) {
        // ---------- 权限校验 ----------
        if (applicant == null) {
            System.err.println("[权限校验失败] 用户未登录，无法提交申请");
            return null;
        }
        if (!"student".equals(applicant.getRole())) {
            System.err.println("[权限校验失败] 非学生用户（" + applicant.getRole() + "）不能提交学时申请");
            return null;
        }

        // ---------- 活动名称校验 ----------
        if (activityName == null || activityName.trim().isEmpty()) {
            System.err.println("[格式校验失败] 活动名称不能为空");
            return null;
        }
        if (activityName.trim().length() > MAX_ACTIVITY_NAME_LENGTH) {
            System.err.println("[格式校验失败] 活动名称长度不能超过" + MAX_ACTIVITY_NAME_LENGTH + "个字符");
            return null;
        }

        // ---------- 活动类型校验 ----------
        if (activityType == null || !VALID_ACTIVITY_TYPES.contains(activityType)) {
            System.err.println("[格式校验失败] 无效的活动类型: " + activityType +
                               "，允许类型: " + VALID_ACTIVITY_TYPES);
            return null;
        }

        // ---------- 学时数校验（边界值） ----------
        if (hours < MIN_HOURS_PER_APPLICATION) {
            System.err.println("[格式校验失败] 申请学时数不能小于" + MIN_HOURS_PER_APPLICATION + "小时（当前: " + hours + "）");
            return null;
        }
        if (hours > MAX_HOURS_PER_APPLICATION) {
            System.err.println("[格式校验失败] 单次申请学时数不能超过" + MAX_HOURS_PER_APPLICATION + "小时（当前: " + hours + "）");
            return null;
        }

        // ---------- 附件校验（可选，如有则校验格式） ----------
        if (attachmentUrl != null && !attachmentUrl.trim().isEmpty()) {
            String ext = getFileExtension(attachmentUrl);
            if (ext == null || !ALLOWED_ATTACHMENT_EXTENSIONS.contains(ext.toLowerCase())) {
                System.err.println("[附件校验失败] 不支持的附件格式: " + ext +
                                   "，允许格式: " + ALLOWED_ATTACHMENT_EXTENSIONS);
                return null;
            }
        }

        // 全部校验通过，创建申请
        Application app = new Application();
        app.setApplicantId(applicant.getId());
        app.setActivityName(activityName.trim());
        app.setActivityType(activityType);
        app.setHours(hours);
        app.setDescription(description != null ? description.trim() : "");
        app.setAttachmentUrl(attachmentUrl);
        app.setStatus("pending");
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        System.out.println("[申请成功] 申请人: " + applicant.getName() +
                           " | 活动: " + activityName +
                           " | 学时: " + hours +
                           " | 类型: " + activityType);

        return app;
    }

    /**
     * 审核审批 — 权限校验 + 状态变更
     */
    public Application reviewApplication(Application app, User reviewer,
                                          boolean approved, String comment) {
        if (reviewer == null || !"admin".equals(reviewer.getRole())) {
            System.err.println("[权限校验失败] 只有管理员才能审核申请");
            return null;
        }
        if (app == null) {
            System.err.println("[审核失败] 申请不存在");
            return null;
        }
        if (!"pending".equals(app.getStatus())) {
            System.err.println("[审核失败] 该申请已审核，状态为: " + app.getStatus());
            return null;
        }

        app.setStatus(approved ? "approved" : "rejected");
        app.setReviewerId(reviewer.getId());
        app.setReviewComment(comment);
        app.setReviewedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        System.out.println("[审核完成] 审核人: " + reviewer.getName() +
                           " | 申请ID: " + app.getId() +
                           " | 结果: " + (approved ? "通过 ✓" : "驳回 ✗") +
                           " | 意见: " + (comment != null ? comment : "无"));

        return app;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) return null;
        return fileName.substring(lastDot + 1);
    }

    // ========== 测试数据集（供测试用例使用） ==========

    /**
     * 获取测试用例数据集
     */
    public static List<Object[]> getTestCases() {
        User student = new User("2023001", "张三", "password", "student");
        student.setId(1L);
        User admin = new User("admin001", "管理员", "password", "admin");
        admin.setId(4L);
        User teacher = new User("T001", "老师", "password", "teacher");

        return Arrays.asList(
            // 正常场景
            new Object[]{student, "Python编程讲座", "lecture", 2.0, "参加学院Python讲座", null, true},
            // 边界值：最小学时
            new Object[]{student, "晨跑锻炼", "sports", 0.5, "晨跑打卡活动", null, true},
            // 边界值：最大学时
            new Object[]{student, "全国数学建模竞赛", "competition", 50.0, "参加国赛获省一等奖", null, true},
            // 异常：空活动名称
            new Object[]{student, "", "lecture", 2.0, "测试", null, false},
            // 异常：超出最大学时
            new Object[]{student, "国际学术会议", "academic", 55.0, "参加IEEE会议", null, false},
            // 异常：小于最大学时
            new Object[]{student, "小活动", "other", 0.1, "小型活动", null, false},
            // 异常：无效活动类型
            new Object[]{student, "测试活动", "invalid_type", 2.0, "测试", null, false},
            // 异常：附件格式不支持
            new Object[]{student, "学术报告", "academic", 3.0, "听学术报告", "report.exe", false},
            // 异常：用户权限不足（非学生）
            new Object[]{teacher, "讲课", "academic", 2.0, "给本科生上课", null, false},
            // 异常：空活动名称（边界测试）
            new Object[]{student, null, "lecture", 2.0, "测试", null, false}
        );
    }
}
