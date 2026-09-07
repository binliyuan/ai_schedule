package com.solunis.schedule.mcp

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object McpTools {

    fun allTools(): List<McpToolDef> = listOf(
        getSchedule(),
        addCourse(),
        updateCourse(),
        deleteCourse(),
        batchImportCourses()
    )

    private fun getSchedule() = McpToolDef(
        name = "get_schedule",
        description = "获取当前课表的所有课程数据。返回课程列表，包含课程名、时间、教室等信息。",
        inputSchema = schema {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("tableId", prop("integer", "课表ID，不填则使用默认课表"))
            })
        }
    )

    private fun addCourse() = McpToolDef(
        name = "add_course",
        description = "添加一门课程到课表。需要提供课程名称、星期几、第几节课开始、连续几节等信息。",
        inputSchema = schema {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("name", prop("string", "课程名称"))
                add("day", prop("integer", "星期几 (1=周一, 7=周日)"))
                add("startNode", prop("integer", "第几节课开始 (从1开始)"))
                add("step", prop("integer", "连续几节课 (默认2)"))
                add("room", prop("string", "上课教室"))
                add("teacher", prop("string", "授课老师"))
                add("startWeek", prop("integer", "开始周 (默认1)"))
                add("endWeek", prop("integer", "结束周 (默认20)"))
            })
            add("required", JsonArray().apply {
                add("name"); add("day"); add("startNode")
            })
        }
    )

    private fun updateCourse() = McpToolDef(
        name = "update_course",
        description = "修改课表中已有的课程信息。需要提供课程ID，以及要修改的字段。",
        inputSchema = schema {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("courseId", prop("integer", "课程ID"))
                add("name", prop("string", "新的课程名称"))
                add("room", prop("string", "新的上课教室"))
                add("teacher", prop("string", "新的授课老师"))
            })
            add("required", JsonArray().apply { add("courseId") })
        }
    )

    private fun deleteCourse() = McpToolDef(
        name = "delete_course",
        description = "从课表中删除一门课程。",
        inputSchema = schema {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("courseId", prop("integer", "要删除的课程ID"))
            })
            add("required", JsonArray().apply { add("courseId") })
        }
    )

    private fun batchImportCourses() = McpToolDef(
        name = "batch_import_courses",
        description = "批量导入课程到课表。用于从图片识别结果导入，或批量添加多门课程。会清除原有课程后导入。",
        inputSchema = schema {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("courses", JsonObject().apply {
                    addProperty("type", "array")
                    addProperty("description", "课程列表")
                    add("items", JsonObject().apply {
                        addProperty("type", "object")
                        add("properties", JsonObject().apply {
                            add("name", prop("string", "课程名称"))
                            add("day", prop("integer", "星期几 (1-7)"))
                            add("startNode", prop("integer", "第几节课开始"))
                            add("step", prop("integer", "连续几节"))
                            add("room", prop("string", "教室"))
                            add("teacher", prop("string", "老师"))
                            add("startWeek", prop("integer", "开始周"))
                            add("endWeek", prop("integer", "结束周"))
                        })
                    })
                })
            })
            add("required", JsonArray().apply { add("courses") })
        }
    )

    private fun schema(block: JsonObject.() -> Unit): JsonObject =
        JsonObject().apply(block)

    private fun prop(type: String, description: String) = JsonObject().apply {
        addProperty("type", type)
        addProperty("description", description)
    }
}
