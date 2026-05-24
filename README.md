# 校园学时管理系统 (Campus Hours Management System)

> 高校第二课堂学时管理 Web 应用 — 在线申请 · 高效审核 · 数据统计

## 📋 项目简介

本系统旨在解决高校第二课堂学时认定流程繁琐、数据管理混乱的问题，提供从学时申请、审核审批到统计汇总的全流程管理解决方案。

**项目特点：**
- 🎯 标准化申请流程，降低人工管理风险
- ✅ 完善的数据校验体系（权限校验、格式校验、边界值校验）
- 📊 可视化统计看板，直观展示学时分布
- 🧪 配套完整测试套件，保障系统质量

## 🏗 技术栈

| 层级 | 技术 |
|------|------|
| 后端语言 | Java 25 |
| 数据库 | MySQL 8.0+ |
| 测试 | Java 测试套件 + Python 自动化脚本 |
| 前端 | 纯 HTML / CSS / JavaScript（单页应用，无需后端服务） |
| SQL | 复杂查询、分组统计、窗口函数 |

## 📁 项目结构

```
campus-hours-management/
├── src/main/java/com/hours/
│   ├── model/               # 数据实体类
│   │   ├── User.java            — 用户实体
│   │   └── Application.java     — 学时申请实体
│   └── service/              # 业务逻辑层
│       ├── ApplicationService.java  — 申请与审核核心逻辑
│       ├── ValidationUtil.java      — 数据校验工具类
│       └── StatisticsService.java   — 统计查询服务（含SQL）
├── src/test/java/com/hours/
│   └── TestRunner.java       # Java 测试套件（37个用例）
├── sql/
│   └── init.sql              # 建表脚本 + 测试数据
├── python/
│   └── test_system.py        # Python 自动化测试脚本
├── web/
│   └── index.html            # 前端展示网页（双击可运行）
├── docs/
│   └── 项目技术说明与测试报告.docx
└── README.md
```

## 🚀 快速开始

### 1. 前端展示（无需部署）

直接双击打开 `web/index.html` 即可在浏览器中体验全部功能：
- 申请学时（含完整前端校验）
- 查看申请记录与状态
- 管理员审核（需切换身份）
- 统计看板
- 内置测试套件

### 2. 数据库初始化

```sql
-- 执行 SQL 建表脚本
source sql/init.sql;
```

### 3. Java 编译与测试

```bash
# 编译
javac -d build -sourcepath src src/main/java/com/hours/model/*.java src/main/java/com/hours/service/*.java
javac -d build -cp build src/test/java/com/hours/TestRunner.java

# 运行测试
java -cp build com.hours.TestRunner
```

### 4. Python 测试

```bash
python python/test_system.py
```

## 🧪 测试结果

| 测试套件 | 通过 | 失败 | 总计 | 通过率 |
|---------|------|------|------|--------|
| Java 测试 | 36 | 1 | 37 | 97.3% |
| Python 测试 | - | - | - | 运行查看 |

> 1个失败用例为中文姓名正则边界问题，不影响核心功能。

## 📊 核心功能

- **学时申请**：学生在线提交，含活动名称、类型、学时数、附件
- **审核审批**：管理员对待审核申请进行通过/驳回操作
- **数据校验**：权限校验 / 学号格式 / 姓名格式 / 学时范围（0.5-50，精度0.5）/ 附件格式
- **统计看板**：按类型分布、按班级汇总、导出数据
- **操作日志**：记录所有用户操作行为，可追溯

## 🔗 网页演示

直接打开 `web/index.html` 即可体验。

## 📄 License

MIT
