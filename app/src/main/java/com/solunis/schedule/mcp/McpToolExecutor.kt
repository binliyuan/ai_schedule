package com.solunis.schedule.mcp

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import kotlinx.coroutines.runBlocking

class McpToolExecutor(private val database: AppDatabase) {

    private val gson = Gson()
    private val courseDao = database.courseDao()
    private val tableDao = database.tableDao()

    fun execute(toolName: String, arguments: JsonObject): McpToolCallResult {
        return try {
            when (toolName) {
                "get_schedule" -> getSchedule(arguments)
                "add_course" -> addCourse(arguments)
                "update_course" -> updateCourse(arguments)
                "delete_course" -> deleteCourse(arguments)
                "batch_import_courses" -> batchImportCourses(arguments)
                else -> McpToolCallResult(
                    content = listOf(McpContent(text = "未知工具: $toolName")),
                    isError = true
                )
            }
        } catch (e: Exception) {
            McpToolCallResult(
                content = listOf(McpContent(text = "执行错误: ${e.message}")),
                isError = true
            )
        }
    }

    private fun getSchedule(args: JsonObject): McpToolCallResult = runBlocking {
        val tableId = if (args.has("tableId")) args.get("tableId").asInt else getDefaultTableId()
        if (tableId == 0) {
            return@runBlocking McpToolCallResult(content = listOf(McpContent(text = "暂无课表数据")))
        }

        val courses = courseDao.getCoursesByTableIdSync(tableId)
        val result = courses.map { c ->
            mapOf(
                "id" to c.id,
                "name" to c.courseName,
                "day" to c.day,
                "startNode" to c.startNode,
                "step" to c.step,
                "room" to c.room,
                "teacher" to c.teacher,
                "startWeek" to c.startWeek,
                "endWeek" to c.endWeek,
                "color" to c.color
            )
        }

        McpToolCallResult(content = listOf(McpContent(text = gson.toJson(mapOf(
            "tableId" to tableId,
            "courseCount" to courses.size,
            "courses" to result
        )))))
    }

    private fun addCourse(args: JsonObject): McpToolCallResult = runBlocking {
        val tableId = getDefaultTableId()
        if (tableId == 0) {
            return@runBlocking McpToolCallResult(
                content = listOf(McpContent(text = "没有默认课表，请先创建")),
                isError = true
            )
        }

        val name = args.get("name").asString
        val day = args.get("day").asInt
        val startNode = args.get("startNode").asInt
        val step = if (args.has("step")) args.get("step").asInt else 2
        val room = if (args.has("room")) args.get("room").asString else ""
        val teacher = if (args.has("teacher")) args.get("teacher").asString else ""
        val startWeek = if (args.has("startWeek")) args.get("startWeek").asInt else 1
        val endWeek = if (args.has("endWeek")) args.get("endWeek").asInt else 20

        val nextId = (courseDao.getCoursesByTableIdSync(tableId).maxOfOrNull { it.id } ?: 0) + 1

        courseDao.insertCourseBase(CourseBaseBean(id = nextId, courseName = name, color = "${nextId % 7}", tableId = tableId))
        courseDao.insertCourseDetail(CourseDetailBean(
            id = nextId, day = day, room = room, teacher = teacher,
            startNode = startNode, step = step, startWeek = startWeek,
            endWeek = endWeek, type = 0, tableId = tableId
        ))

        McpToolCallResult(content = listOf(McpContent(text = "已添加课程「$name」(ID=$nextId) 周$day 第${startNode}节 $room")))
    }

    private fun updateCourse(args: JsonObject): McpToolCallResult = runBlocking {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        val courses = courseDao.getCoursesByTableIdSync(tableId)
        val course = courses.firstOrNull { it.id == courseId }
            ?: return@runBlocking McpToolCallResult(
                content = listOf(McpContent(text = "课程 ID=$courseId 不存在")),
                isError = true
            )

        val newName = if (args.has("name")) args.get("name").asString else course.courseName
        val newRoom = if (args.has("room")) args.get("room").asString else course.room
        val newTeacher = if (args.has("teacher")) args.get("teacher").asString else course.teacher

        courseDao.updateCourseBase(CourseBaseBean(id = courseId, courseName = newName, color = course.color, tableId = tableId))
        courseDao.updateCourseDetail(CourseDetailBean(
            id = courseId, day = course.day, room = newRoom, teacher = newTeacher,
            startNode = course.startNode, step = course.step,
            startWeek = course.startWeek, endWeek = course.endWeek,
            type = course.type, tableId = tableId
        ))

        McpToolCallResult(content = listOf(McpContent(text = "已更新课程「$newName」(ID=$courseId)")))
    }

    private fun deleteCourse(args: JsonObject): McpToolCallResult = runBlocking {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        courseDao.deleteCourseBase(courseId, tableId)
        McpToolCallResult(content = listOf(McpContent(text = "已删除课程 ID=$courseId")))
    }

    private fun batchImportCourses(args: JsonObject): McpToolCallResult = runBlocking {
        val tableId = getDefaultTableId()
        if (tableId == 0) {
            return@runBlocking McpToolCallResult(
                content = listOf(McpContent(text = "没有默认课表")),
                isError = true
            )
        }

        val coursesArray = args.getAsJsonArray("courses")
        courseDao.deleteAllByTable(tableId)

        var count = 0
        coursesArray.forEach { element ->
            val obj = element.asJsonObject
            val id = count + 1
            val name = obj.get("name").asString
            val day = obj.get("day").asInt
            val startNode = obj.get("startNode").asInt
            val step = if (obj.has("step")) obj.get("step").asInt else 2
            val room = if (obj.has("room")) obj.get("room").asString else ""
            val teacher = if (obj.has("teacher")) obj.get("teacher").asString else ""
            val startWeek = if (obj.has("startWeek")) obj.get("startWeek").asInt else 1
            val endWeek = if (obj.has("endWeek")) obj.get("endWeek").asInt else 20

            courseDao.insertCourseBase(CourseBaseBean(id = id, courseName = name, color = "${id % 7}", tableId = tableId))
            courseDao.insertCourseDetail(CourseDetailBean(
                id = id, day = day, room = room, teacher = teacher,
                startNode = startNode, step = step, startWeek = startWeek,
                endWeek = endWeek, type = 0, tableId = tableId
            ))
            count++
        }

        McpToolCallResult(content = listOf(McpContent(text = "已批量导入 $count 门课程到课表(tableId=$tableId)")))
    }

    private suspend fun getDefaultTableId(): Int {
        val table = tableDao.getDefaultTableSync()
        return table?.id ?: tableDao.getAllTablesSync().firstOrNull()?.id ?: 0
    }
}
