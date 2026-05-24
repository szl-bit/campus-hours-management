package com.hours.service;

import java.util.regex.Pattern;

/**
 * 数据校验工具类
 * 包含：学号格式校验、学时格式校验、附件合规性检查
 */
public class ValidationUtil {

    /** 学号正则：8-20位字母数字组合 */
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,20}$");

    /** 姓名正则：2-20个中文字符或英文 */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5]{2,20}$|^[A-Za-z ]{2,50}$");

    /** 最大附件大小（单位：MB） */
    private static final long MAX_ATTACHMENT_SIZE_MB = 10;

    /**
     * 校验学号格式
     * 规则：8-20位字母数字
     */
    public static boolean isValidStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            System.err.println("[格式校验] 学号不能为空");
            return false;
        }
        if (!STUDENT_ID_PATTERN.matcher(studentId.trim()).matches()) {
            System.err.println("[格式校验] 学号格式不正确（需8-20位字母数字组合）: " + studentId);
            return false;
        }
        return true;
    }

    /**
     * 校验姓名格式
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("[格式校验] 姓名不能为空");
            return false;
        }
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            System.err.println("[格式校验] 姓名格式不正确: " + name);
            return false;
        }
        return true;
    }

    /**
     * 校验学时数有效性
     * 规则：0.5 <= hours <= 50，精度为0.5的倍数
     */
    public static boolean isValidHours(Double hours) {
        if (hours == null) {
            System.err.println("[格式校验] 学时数不能为空");
            return false;
        }
        if (hours < 0.5 || hours > 50.0) {
            System.err.println("[格式校验] 学时数超出范围（0.5~50）: " + hours);
            return false;
        }
        // 检查精度：0.5的倍数
        double remainder = hours % 0.5;
        if (Math.abs(remainder) > 0.001) {
            System.err.println("[格式校验] 学时数必须是0.5的倍数: " + hours);
            return false;
        }
        return true;
    }

    /**
     * 校验附件大小（模拟）
     * 真实场景中通过读取文件大小判断
     */
    public static boolean isValidAttachmentSize(long fileSizeBytes) {
        long maxBytes = MAX_ATTACHMENT_SIZE_MB * 1024 * 1024;
        if (fileSizeBytes > maxBytes) {
            System.err.println("[附件校验] 附件大小超过限制（最大" + MAX_ATTACHMENT_SIZE_MB + "MB）");
            return false;
        }
        return true;
    }

    /**
     * 校验附件文件名
     * 规则：允许的扩展名
     */
    public static boolean isValidAttachmentFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("[附件校验] 未上传附件");
            return false;
        }
        String lower = fileName.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            System.err.println("[附件校验] 不支持的附件格式: " + fileName);
            return false;
        }
        return true;
    }

    /**
     * 综合校验：全部通过返回true
     */
    public static ValidationResult validateApplication(String studentId, String name,
                                                        Double hours, String attachmentFileName,
                                                        Long attachmentSizeBytes) {
        ValidationResult result = new ValidationResult();

        result.setStudentIdValid(isValidStudentId(studentId));
        result.setNameValid(isValidName(name));
        result.setHoursValid(isValidHours(hours));

        if (attachmentFileName != null && !attachmentFileName.isEmpty()) {
            result.setAttachmentNameValid(isValidAttachmentFileName(attachmentFileName));
            if (attachmentSizeBytes != null) {
                result.setAttachmentSizeValid(isValidAttachmentSize(attachmentSizeBytes));
            }
        } else {
            result.setAttachmentNameValid(true);  // 附件是可选的
            result.setAttachmentSizeValid(true);
        }

        result.setAllValid(result.isStudentIdValid() && result.isNameValid()
                          && result.isHoursValid() && result.isAttachmentNameValid()
                          && result.isAttachmentSizeValid());
        return result;
    }

    /**
     * 校验结果封装
     */
    public static class ValidationResult {
        private boolean studentIdValid;
        private boolean nameValid;
        private boolean hoursValid;
        private boolean attachmentNameValid;
        private boolean attachmentSizeValid;
        private boolean allValid;

        public boolean isStudentIdValid() { return studentIdValid; }
        public void setStudentIdValid(boolean v) { this.studentIdValid = v; }

        public boolean isNameValid() { return nameValid; }
        public void setNameValid(boolean v) { this.nameValid = v; }

        public boolean isHoursValid() { return hoursValid; }
        public void setHoursValid(boolean v) { this.hoursValid = v; }

        public boolean isAttachmentNameValid() { return attachmentNameValid; }
        public void setAttachmentNameValid(boolean v) { this.attachmentNameValid = v; }

        public boolean isAttachmentSizeValid() { return attachmentSizeValid; }
        public void setAttachmentSizeValid(boolean v) { this.attachmentSizeValid = v; }

        public boolean isAllValid() { return allValid; }
        public void setAllValid(boolean v) { this.allValid = v; }

        public String getSummary() {
            if (allValid) return "✅ 全部校验通过";
            StringBuilder sb = new StringBuilder("❌ 校验失败: ");
            if (!studentIdValid) sb.append("学号 | ");
            if (!nameValid) sb.append("姓名 | ");
            if (!hoursValid) sb.append("学时 | ");
            if (!attachmentNameValid) sb.append("附件格式 | ");
            if (!attachmentSizeValid) sb.append("附件大小 | ");
            String s = sb.toString();
            return s.endsWith(" | ") ? s.substring(0, s.length() - 3) : s;
        }
    }
}
