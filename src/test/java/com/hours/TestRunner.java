package com.hours;

import com.hours.model.User;
import com.hours.model.Application;
import com.hours.service.ApplicationService;
import com.hours.service.ValidationUtil;

/**
 * 校园学时管理系统 - 测试主程序
 *
 * 运行本程序可执行：
 *   1. 功能测试（提交申请、审核审批、边界值测试）
 *   2. 数据校验测试（学号、姓名、学时、附件）
 *   3. 权限校验测试
 *
 * 运行方式：java com.hours.TestRunner
 */
public class TestRunner {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  校园学时管理系统 - 功能测试套件");
        System.out.println("============================================\n");

        // ----- 测试1: 提交申请功能测试 -----
        System.out.println("----- 测试组1: 提交申请（正常/异常场景）-----");
        testSubmitApplication();

        // ----- 测试2: 审核功能测试 -----
        System.out.println("\n----- 测试组2: 审核流程测试 -----");
        testReviewProcess();

        // ----- 测试3: 数据校验功能测试 -----
        System.out.println("\n----- 测试组3: 数据校验功能测试 -----");
        testValidation();

        // ----- 测试4: 批量测试用例（来自ApplicationService）-----
        System.out.println("\n----- 测试组4: 批量测试用例执行 -----");
        testBatchCases();

        // ----- 总结 -----
        System.out.println("\n============================================");
        System.out.println("  测试总结");
        System.out.println("============================================");
        System.out.println("  通过: " + passed + "  |  失败: " + failed + "  |  总计: " + (passed + failed));
        System.out.println("============================================");
    }

    // ========== 测试1: 提交申请 ==========
    static void testSubmitApplication() {
        ApplicationService service = new ApplicationService();

        User student = new User("2023001", "张三", "password", "student");
        student.setId(1L);
        User admin = new User("admin001", "管理员", "password", "admin");
        admin.setId(2L);

        // 1.1 正常提交
        Application app1 = service.submitApplication(student, "Python编程讲座",
                                                      "lecture", 2.0, "学院讲座", "cert.pdf");
        check("正常提交申请", app1 != null && app1.getStatus().equals("pending"));

        // 1.2 权限不足（管理员提交）
        Application app2 = service.submitApplication(admin, "测试", "lecture", 1.0, "", null);
        check("管理员不能提交申请（权限校验）", app2 == null);

        // 1.3 最小学时（边界值）
        Application app3 = service.submitApplication(student, "晨跑", "sports", 0.5, "晨跑", null);
        check("最小边界值 0.5小时", app3 != null);

        // 1.4 最大小时（边界值）
        Application app4 = service.submitApplication(student, "全国赛", "competition", 50.0, "比赛", null);
        check("最大边界值 50小时", app4 != null);

        // 1.5 超出最大值
        Application app5 = service.submitApplication(student, "活动", "other", 55.0, "", null);
        check("超出最大值55小时应拒绝", app5 == null);

        // 1.6 空活动名称
        Application app6 = service.submitApplication(student, "", "lecture", 2.0, "", null);
        check("空活动名称应拒绝", app6 == null);

        // 1.7 无效活动类型
        Application app7 = service.submitApplication(student, "测试", "invalid", 2.0, "", null);
        check("无效活动类型应拒绝", app7 == null);

        // 1.8 不支持的附件格式
        Application app8 = service.submitApplication(student, "测试", "academic", 2.0, "测试", "report.exe");
        check("不支持附件格式应拒绝", app8 == null);
    }

    // ========== 测试2: 审核流程 ==========
    static void testReviewProcess() {
        ApplicationService service = new ApplicationService();
        User student = new User("2023001", "张三", "password", "student");
        student.setId(1L);
        User admin = new User("admin001", "管理员", "password", "admin");
        admin.setId(2L);

        // 先创建一个申请
        Application app = service.submitApplication(student, "学术讲座", "academic", 3.0, "听讲座", null);
        app.setId(1L);

        // 2.1 管理员审核通过
        Application reviewed = service.reviewApplication(app, admin, true, "通过，活动符合要求");
        check("管理员审核通过", reviewed != null && "approved".equals(reviewed.getStatus()));

        // 2.2 重复审核应拒绝
        Application reReview = service.reviewApplication(app, admin, false, "驳回");
        check("已审核申请不能再次审核", reReview == null);

        // 2.3 非管理员审核应拒绝
        Application app2 = service.submitApplication(student, "体育比赛", "sports", 4.0, "", null);
        app2.setId(2L);
        Application rejected = service.reviewApplication(app2, student, true, "自审");
        check("非管理员审核应拒绝", rejected == null);
    }

    // ========== 测试3: 数据校验 ==========
    static void testValidation() {
        // 3.1 学号校验
        check("学号格式正确", ValidationUtil.isValidStudentId("20230001"));
        check("空学号应拒绝", !ValidationUtil.isValidStudentId(""));
        check("过短学号应拒绝", !ValidationUtil.isValidStudentId("123"));

        // 3.2 姓名校验
        check("姓名格式正确", ValidationUtil.isValidName("张三"));
        check("空姓名应拒绝", !ValidationUtil.isValidName(""));
        check("过长姓名应拒绝", !ValidationUtil.isValidName("这是一个非常长的测试姓名啊啊啊啊啊啊"));

        // 3.3 学时数校验
        check("学时0.5合法", ValidationUtil.isValidHours(0.5));
        check("学时50合法", ValidationUtil.isValidHours(50.0));
        check("学时0非法", !ValidationUtil.isValidHours(0.0));
        check("学时50.5非法", !ValidationUtil.isValidHours(50.5));
        check("学时1.5合法", ValidationUtil.isValidHours(1.5));

        // 3.4 附件校验
        check("pdf附件合法", ValidationUtil.isValidAttachmentFileName("report.pdf"));
        check("exe附件非法", !ValidationUtil.isValidAttachmentFileName("setup.exe"));
        check("空附件名非法", !ValidationUtil.isValidAttachmentFileName(""));

        // 3.5 综合校验
        ValidationUtil.ValidationResult result = ValidationUtil.validateApplication(
            "20230001", "张三", 2.5, "cert.pdf", 1024L
        );
        check("综合校验全部通过", result.isAllValid());

        ValidationUtil.ValidationResult badResult = ValidationUtil.validateApplication(
            "123", "张三", 55.0, "bad.exe", 999999999L
        );
        check("综合校验全部失败", !badResult.isAllValid());
    }

    // ========== 测试4: 批量测试用例 ==========
    static void testBatchCases() {
        ApplicationService service = new ApplicationService();
        java.util.List<Object[]> cases = ApplicationService.getTestCases();

        int casePassed = 0;
        for (int i = 0; i < cases.size(); i++) {
            Object[] tc = cases.get(i);
            User user = (User) tc[0];
            String name = (String) tc[1];
            String type = (String) tc[2];
            double hours = (double) tc[3];
            String desc = (String) tc[4];
            String attach = (String) tc[5];
            boolean expectSuccess = (boolean) tc[6];

            Application app = service.submitApplication(user, name, type, hours, desc, attach);
            boolean actual = (app != null);
            if (actual == expectSuccess) {
                System.out.println("  [用例" + (i+1) + " ✓] " + name + " -> 是否符合预期: " + (actual ? "通过" : "拒绝"));
                casePassed++;
            } else {
                System.out.println("  [用例" + (i+1) + " ✗] " + name + " -> 预期: " + (expectSuccess ? "通过" : "拒绝") + " 实际: " + (actual ? "通过" : "拒绝"));
                failed++;
            }
        }
        passed += casePassed;
        System.out.println("  批量用例: " + casePassed + "/" + cases.size() + " 通过");
    }

    static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  ✓ " + name);
            passed++;
        } else {
            System.out.println("  ✗ " + name + " [失败]");
            failed++;
        }
    }
}
