#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园学时管理系统 - Python功能测试脚本
测试内容：
  1. API功能测试（模拟提交申请、审核流程）
  2. 数据合法性校验测试
  3. 边界值测试
  4. 统计查询测试

运行方式：python test_system.py
"""

import json
import sys
import os

# ========== 测试计数器 ==========
passed = 0
failed = 0

def check(name, condition):
    global passed, failed
    if condition:
        print(f"  ✓ {name}")
        passed += 1
    else:
        print(f"  ✗ {name} [失败]")
        failed += 1

# ========== 数据模型模拟 ==========
class User:
    def __init__(self, student_id, name, password, role, user_id=None):
        self.student_id = student_id
        self.name = name
        self.password = password
        self.role = role
        self.id = user_id or hash(student_id)

class Application:
    def __init__(self, applicant, activity_name, activity_type, hours,
                 description="", attachment_url=None):
        self.applicant = applicant
        self.activity_name = activity_name
        self.activity_type = activity_type
        self.hours = hours
        self.description = description
        self.attachment_url = attachment_url
        self.status = "pending"
        self.reviewer = None
        self.review_comment = None

    def to_dict(self):
        return {
            "applicant": self.applicant.name,
            "activity_name": self.activity_name,
            "activity_type": self.activity_type,
            "hours": self.hours,
            "status": self.status,
            "review_comment": self.review_comment
        }

# ========== 功能函数 ==========

VALID_TYPES = {"academic", "sports", "volunteer", "competition", "lecture", "other"}
ALLOWED_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx"}

def validate_application(user, activity_name, activity_type, hours, attachment_url=None):
    """模拟后端校验逻辑"""
    errors = []

    # 权限校验
    if user is None:
        errors.append("用户未登录")
        return errors
    if user.role != "student":
        errors.append(f"非学生用户({user.role})不能提交申请")
        return errors  # 直接返回，不必继续

    # 活动名称
    if not activity_name or not activity_name.strip():
        errors.append("活动名称不能为空")
    elif len(activity_name.strip()) > 200:
        errors.append("活动名称不能超过200个字符")

    # 活动类型
    if activity_type not in VALID_TYPES:
        errors.append(f"无效活动类型: {activity_type}")

    # 学时数
    if hours is None:
        errors.append("学时数不能为空")
    elif hours < 0.5:
        errors.append(f"学时数不能小于0.5(当前:{hours})")
    elif hours > 50.0:
        errors.append(f"学时数不能大于50(当前:{hours})")
    elif hours % 0.5 > 0.001:
        errors.append("学时数必须是0.5的倍数")

    # 附件
    if attachment_url:
        ext = os.path.splitext(attachment_url)[1].lower()
        if ext not in ALLOWED_EXTENSIONS:
            errors.append(f"不支持的附件格式: {ext}")

    return errors


def submit_application(user, activity_name, activity_type, hours,
                       description="", attachment_url=None):
    """提交学时申请"""
    errors = validate_application(user, activity_name, activity_type, hours, attachment_url)
    if errors:
        for e in errors:
            print(f"  [校验失败] {e}")
        return None
    app = Application(user, activity_name, activity_type, hours, description, attachment_url)
    print(f"  [申请成功] {user.name} | {activity_name} | {hours}小时 | {activity_type}")
    return app


def review_application(app, reviewer, approved, comment=""):
    """审核申请"""
    if reviewer.role != "admin":
        print("  [权限校验失败] 只有管理员才能审核")
        return None
    if app.status != "pending":
        print(f"  [审核失败] 当前状态非待审核: {app.status}")
        return None
    app.status = "approved" if approved else "rejected"
    app.reviewer = reviewer
    app.review_comment = comment
    print(f"  [审核完成] {reviewer.name} | 结果: {'通过' if approved else '驳回'} | {comment}")
    return app


# ========== 测试入口 ==========
print("=" * 60)
print("  校园学时管理系统 - Python测试套件")
print("=" * 60)

# 准备测试用户
student = User("2023001", "张三", "password", "student")
student2 = User("2023002", "李四", "password", "student")
admin = User("admin001", "管理员", "password", "admin")
teacher = User("T001", "王老师", "password", "teacher")

# ---------------------------------------------------
print("\n----- 测试组1: 提交申请（正常场景）-----")

app1 = submit_application(student, "Python编程讲座", "lecture", 2.0,
                          "参加学院Python数据分析讲座", "cert.pdf")
check("正常提交成功", app1 is not None)

app2 = submit_application(student, "校级运动会800米", "sports", 3.0)
check("体育类提交成功", app2 is not None)

# ---------------------------------------------------
print("\n----- 测试组2: 边界值测试 -----")

# 最小值
app3 = submit_application(student, "晨跑打卡", "sports", 0.5)
check("最小学时(0.5)提交成功", app3 is not None)

# 最大值
app4 = submit_application(student, "全国数学建模竞赛一等奖", "competition", 50.0,
                          "参加国赛并获得省一等奖")
check("最大学时(50)提交成功", app4 is not None)

# 超出最大值
result_min = submit_application(student, "小活动", "other", 0.1)
check("小于0.5应拒绝", result_min is None)

# 超过最大值
result_max = submit_application(student, "国际会议", "academic", 55.0)
check("超过50应拒绝", result_max is None)

# 学时不是0.5的倍数
result_half = submit_application(student, "测试", "other", 1.2)
check("学时非0.5倍数应拒绝", result_half is None)

# ---------------------------------------------------
print("\n----- 测试组3: 异常输入测试 -----")

# 空活动名
result_empty_name = submit_application(student, "", "lecture", 2.0)
check("空活动名应拒绝", result_empty_name is None)

# 无效活动类型
result_bad_type = submit_application(student, "测试", "hiking", 2.0)
check("无效活动类型应拒绝", result_bad_type is None)

# 不支持附件格式
result_bad_attach = submit_application(student, "报告", "academic", 2.0,
                                       "", "report.exe")
check("不支持附件格式应拒绝", result_bad_attach is None)

# 权限不足（管理员提交）
result_admin_submit = submit_application(admin, "测试", "lecture", 2.0)
check("管理员不能提交申请", result_admin_submit is None)

# 权限不足（老师提交）
result_teacher_submit = submit_application(teacher, "讲课", "academic", 2.0)
check("非学生用户不能提交", result_teacher_submit is None)

# ---------------------------------------------------
print("\n----- 测试组4: 审核流程测试 -----")

app_review = submit_application(student2, "机器学习研讨会", "academic", 6.0)
if app_review:
    # 管理员审核通过
    reviewed = review_application(app_review, admin, True, "活动符合要求，通过")
    check("审核通过", reviewed is not None and reviewed.status == "approved")

    # 重复审核应拒绝
    re_review = review_application(app_review, admin, False, "再次审核")
    check("已审核申请不能再次审核", re_review is None)

# 非管理员审核应拒绝
app_review2 = submit_application(student, "志愿活动", "volunteer", 4.0)
if app_review2:
    bad_review = review_application(app_review2, student, True, "自审")
    check("非管理员审核应拒绝", bad_review is None)

# ---------------------------------------------------
print("\n----- 测试组5: 数据校验工具测试 -----")

# 学号校验
check("正确学号通过", len(validate_application(student, "测试", "lecture", 1.0)) == 0)

# 学时边界
check("学时0非法", len(validate_application(student, "测试", "lecture", 0.0)) > 0)
check("学时50.5非法", len(validate_application(student, "测试", "lecture", 50.5)) > 0)

# 空用户
check("空用户应拒绝", len(validate_application(None, "测试", "lecture", 1.0)) > 0)

# ---------------------------------------------------
print("\n----- 测试组6: 统计查询模拟 -----")

applications = [a for a in [app1, app2, app3, app4, app_review, app_review2] if a is not None]
print(f"  当前系统共有 {len(applications)} 条申请记录")

# 按状态统计
stats = {"pending": 0, "approved": 0, "rejected": 0}
for a in applications:
    stats[a.status] = stats.get(a.status, 0) + 1
print(f"  待审核: {stats['pending']} | 已通过: {stats['approved']} | 已驳回: {stats['rejected']}")
check("统计结果正确", stats["pending"] + stats["approved"] + stats["rejected"] == len(applications))

# ---------------------------------------------------
print("\n----- 测试总结 -----")
print(f"  通过: {passed}  |  失败: {failed}  |  总计: {passed + failed}")
print("=" * 60)

# 输出JSON格式结果
result = {
    "test_suite": "校园学时管理系统 - Python测试",
    "passed": passed,
    "failed": failed,
    "total": passed + failed,
    "success_rate": f"{passed/(passed+failed)*100:.1f}%" if (passed+failed) > 0 else "0%"
}
print(f"\nJSON结果: {json.dumps(result, ensure_ascii=False, indent=2)}")

# 保存结果到文件
with open("test_results_python.json", "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print("\n结果已保存到 test_results_python.json")
