package com.solunis.schedule.data.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object AiToolDefs {

    fun allTools(): List<ToolDef> = listOf(
        getScheduleTool(),
        addCourseTool(),
        updateCourseTool(),
        deleteCourseTool(),
        batchImportTool()
    )

    private fun getScheduleTool() = ToolDef(function = ToolFunction(
        name = "get_schedule",
        description = "获取当前课表的所有课程数据",
        parameters = obj {
            addProperty("type", "object")
            add("properties", obj {
                add("tableId", prop("integer", "课表ID，不填则使用默认课表"))
            })
        }
    ))

    private fun addCourseTool() = ToolDef(function = ToolFunction(
        name = "add_course",
        description = "添加一门课程到课表",
        parameters = obj {
            addProperty("type", "object")
            add("properties", obj {
                add("name", prop("string", "课程名称"))
                add("day", prop("integer", "星期几 (1=周一, 7=周日)"))
                add("startNode", prop("integer", "第几节课开始 (从1开始)"))
                add("step", prop("integer", "连续几节课，默认2"))
                add("room", prop("string", "上课教室"))
                add("teacher", prop("string", "授课老师"))
                add("startWeek", prop("integer", "开始周，默认1"))
                add("endWeek", prop("integer", "结束周，默认20"))
            })
            add("required", arr("name", "day", "startNode"))
        }
    ))

    private fun updateCourseTool() = ToolDef(function = ToolFunction(
        name = "update_course",
        description = "修改课表中已有的课程信息",
        parameters = obj {
            addProperty("type", "object")
            add("properties", obj {
                add("courseId", prop("integer", "课程ID"))
                add("name", prop("string", "新课程名称"))
                add("room", prop("string", "新教室"))
                add("teacher", prop("string", "新老师"))
            })
            add("required", arr("courseId"))
        }
    ))

    private fun deleteCourseTool() = ToolDef(function = ToolFunction(
        name = "delete_course",
        description = "从课表中删除一门课程",
        parameters = obj {
            addProperty("type", "object")
            add("properties", obj {
                add("courseId", prop("integer", "要删除的课程ID"))
            })
            add("required", arr("courseId"))
        }
    ))

    private fun batchImportTool() = ToolDef(function = ToolFunction(
        name = "batch_import_courses",
        description = "批量导入课程到课表（会清除原有课程）。用于从课表图片识别后批量写入。",
        parameters = obj {
            addProperty("type", "object")
            add("properties", obj {
                add("courses", obj {
                    addProperty("type", "array")
                    addProperty("description", "课程列表")
                    add("items", obj {
                        addProperty("type", "object")
                        add("properties", obj {
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
            add("required", arr("courses"))
        }
    ))

    private fun obj(block: JsonObject.() -> Unit) = JsonObject().apply(block)
    private fun prop(type: String, desc: String) = JsonObject().apply {
        addProperty("type", type)
        addProperty("description", desc)
    }
    private fun arr(vararg items: String) = JsonArray().apply { items.forEach { add(it) } }
}
