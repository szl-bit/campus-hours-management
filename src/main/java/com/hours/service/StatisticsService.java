package com.hours.service;

/**
 * 学时汇总统计服务
 * 按类型统计、按班级统计、导出数据
 */
public class StatisticsService {

    /**
     * 按活动类型统计学生的学时分布
     * JDBC / DAO 层查询示例（伪代码）
     *
     * SQL:
     * SELECT
     *   u.student_id,
     *   u.name,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'academic'     THEN a.hours END), 0) AS academic_hours,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'sports'       THEN a.hours END), 0) AS sports_hours,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'volunteer'    THEN a.hours END), 0) AS volunteer_hours,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'competition'  THEN a.hours END), 0) AS competition_hours,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'lecture'      THEN a.hours END), 0) AS lecture_hours,
     *   COALESCE(SUM(CASE WHEN a.activity_type = 'other'        THEN a.hours END), 0) AS other_hours,
     *   COALESCE(SUM(a.hours), 0) AS total_hours
     * FROM user u
     * LEFT JOIN application a ON u.id = a.applicant_id AND a.status = 'approved'
     * WHERE u.role = 'student'
     *   AND u.class_name = ?  -- 按班级筛选
     * GROUP BY u.id, u.student_id, u.name
     * ORDER BY total_hours DESC;
     */
    public String getStatisticsByClassSQL() {
        return "SELECT u.student_id, u.name, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'academic' THEN a.hours END), 0) AS academic_hours, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'sports' THEN a.hours END), 0) AS sports_hours, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'volunteer' THEN a.hours END), 0) AS volunteer_hours, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'competition' THEN a.hours END), 0) AS competition_hours, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'lecture' THEN a.hours END), 0) AS lecture_hours, " +
               "  COALESCE(SUM(CASE WHEN a.activity_type = 'other' THEN a.hours END), 0) AS other_hours, " +
               "  COALESCE(SUM(a.hours), 0) AS total_hours " +
               "FROM user u " +
               "LEFT JOIN application a ON u.id = a.applicant_id AND a.status = 'approved' " +
               "WHERE u.role = 'student' AND u.class_name = ? " +
               "GROUP BY u.id, u.student_id, u.name " +
               "ORDER BY total_hours DESC";
    }

    /**
     * 按班级汇总平均学时
     *
     * SQL:
     * SELECT
     *   u.class_name,
     *   COUNT(DISTINCT u.id) AS student_count,
     *   COALESCE(AVG(hr.total_hours), 0) AS avg_hours,
     *   COALESCE(MAX(hr.total_hours), 0) AS max_hours,
     *   COALESCE(MIN(hr.total_hours), 0) AS min_hours
     * FROM user u
     * LEFT JOIN hours_record hr ON u.id = hr.user_id
     * WHERE u.role = 'student'
     * GROUP BY u.class_name
     * ORDER BY avg_hours DESC;
     */
    public String getClassSummarySQL() {
        return "SELECT u.class_name, " +
               "  COUNT(DISTINCT u.id) AS student_count, " +
               "  COALESCE(AVG(hr.total_hours), 0) AS avg_hours, " +
               "  COALESCE(MAX(hr.total_hours), 0) AS max_hours, " +
               "  COALESCE(MIN(hr.total_hours), 0) AS min_hours " +
               "FROM user u " +
               "LEFT JOIN hours_record hr ON u.id = hr.user_id " +
               "WHERE u.role = 'student' " +
               "GROUP BY u.class_name " +
               "ORDER BY avg_hours DESC";
    }

    /**
     * 导出全部已通过学时的数据
     *
     * SQL:
     * SELECT
     *   u.student_id AS "学号",
     *   u.name       AS "姓名",
     *   u.class_name AS "班级",
     *   a.activity_name AS "活动名称",
     *   a.activity_type AS "活动类型",
     *   a.hours      AS "学时数",
     *   a.reviewed_at  AS "审核时间"
     * FROM application a
     * JOIN user u ON a.applicant_id = u.id
     * WHERE a.status = 'approved'
     * ORDER BY u.class_name, u.student_id, a.reviewed_at;
     */
    public String getExportDataSQL() {
        return "SELECT u.student_id AS \"学号\", u.name AS \"姓名\", " +
               " u.class_name AS \"班级\", a.activity_name AS \"活动名称\", " +
               " a.activity_type AS \"活动类型\", a.hours AS \"学时数\", " +
               " a.reviewed_at AS \"审核时间\" " +
               "FROM application a " +
               "JOIN user u ON a.applicant_id = u.id " +
               "WHERE a.status = 'approved' " +
               "ORDER BY u.class_name, u.student_id, a.reviewed_at";
    }
}
