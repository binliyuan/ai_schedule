package com.solunis.schedule.data.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiToolExecutor(private val database: AppDatabase) {

    companion object {
        const val TAG = "Tool"
    }

    private val gson = Gson()
    private val courseDao = database.courseDao()
    private val tableDao = database.tableDao()

    suspend fun execute(name: String, argsJson: String): String = withContext(Dispatchers.IO) {
        AiLogger.section(TAG, "TOOL: $name")
        AiLogger.i(TAG, "Input: $argsJson")
        try {
            val args = gson.fromJson(argsJson, JsonObject::class.java) ?: JsonObject()
            val result = when (name) {
                "get_schedule" -> getSchedule(args)
                "add_course" -> addCourse(args)
                "update_course" -> updateCourse(args)
                "delete_course" -> deleteCourse(args)
                "batch_import_courses" -> batchImport(args)
                else -> {
                    AiLogger.w(TAG, "Unknown tool: $name")
                    """{"error":"未知工具: $name"}"""
                }
            }
            AiLogger.i(TAG, "Output: ${AiLogger.truncate(result, 500)}")
            result
        } catch (e: Exception) {
            AiLogger.e(TAG, "EXCEPTION in $name: ${e.message}", e)
            """{"error":"${e.message}"}"""
        }
    }

    private suspend fun getSchedule(args: JsonObject): String {
        val tableId = if (args.has("tableId")) args.get("tableId").asInt else getDefaultTableId()
        AiLogger.d(TAG, "get_schedule: tableId=$tableId")

        if (tableId == 0) {
            AiLogger.w(TAG, "get_schedule: no table found")
            return """{"courses":[],"message":"暂无课表"}"""
        }

        val courses = courseDao.getCoursesByTableIdSync(tableId)
        AiLogger.i(TAG, "get_schedule: found ${courses.size} courses in table $tableId")

        val list = courses.map { mapOf(
            "id" to it.id, "name" to it.courseName, "day" to it.day,
            "startNode" to it.startNode, "step" to it.step,
            "room" to it.room, "teacher" to it.teacher,
            "startWeek" to it.startWeek, "endWeek" to it.endWeek
        )}
        return gson.toJson(mapOf("tableId" to tableId, "count" to courses.size, "courses" to list))
    }

    private suspend fun addCourse(args: JsonObject): String {
        val tableId = getDefaultTableId()
        if (tableId == 0) {
            AiLogger.e(TAG, "add_course: no default table")
            return """{"error":"没有默认课表"}"""
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
        AiLogger.i(TAG, "add_course: 「$name」 id=$nextId day=$day node=$startNode step=$step room=$room week=$startWeek-$endWeek")

        try {
            courseDao.insertCourseBase(CourseBaseBean(id = nextId, courseName = name, color = "${nextId % 7}", tableId = tableId))
            AiLogger.d(TAG, "add_course: insertBase OK")
        } catch (e: Exception) {
            AiLogger.e(TAG, "add_course: insertBase FAILED", e)
            return """{"error":"写入课程基础数据失败: ${e.message}"}"""
        }

        try {
            courseDao.insertCourseDetail(CourseDetailBean(
                id = nextId, day = day, room = room, teacher = teacher,
                startNode = startNode, step = step, startWeek = startWeek,
                endWeek = endWeek, type = 0, tableId = tableId
            ))
            AiLogger.d(TAG, "add_course: insertDetail OK")
        } catch (e: Exception) {
            AiLogger.e(TAG, "add_course: insertDetail FAILED", e)
            return """{"error":"写入课程详情失败: ${e.message}"}"""
        }

        AiLogger.i(TAG, "add_course: SUCCESS 「$name」 id=$nextId")
        return """{"success":true,"message":"已添加「$name」","courseId":$nextId}"""
    }

    private suspend fun updateCourse(args: JsonObject): String {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        val courses = courseDao.getCoursesByTableIdSync(tableId)
        val course = courses.firstOrNull { it.id == courseId }

        if (course == null) {
            AiLogger.e(TAG, "update_course: id=$courseId not found")
            return """{"error":"课程ID=$courseId 不存在"}"""
        }

        val newName = if (args.has("name")) args.get("name").asString else course.courseName
        val newRoom = if (args.has("room")) args.get("room").asString else course.room
        val newTeacher = if (args.has("teacher")) args.get("teacher").asString else course.teacher
        AiLogger.i(TAG, "update_course: id=$courseId → name=$newName room=$newRoom teacher=$newTeacher")

        try {
            courseDao.updateCourseBase(CourseBaseBean(id = courseId, courseName = newName, color = course.color, tableId = tableId))
            courseDao.updateCourseDetail(CourseDetailBean(
                id = courseId, day = course.day, room = newRoom, teacher = newTeacher,
                startNode = course.startNode, step = course.step,
                startWeek = course.startWeek, endWeek = course.endWeek,
                type = course.type, tableId = tableId
            ))
            AiLogger.i(TAG, "update_course: SUCCESS 「$newName」")
        } catch (e: Exception) {
            AiLogger.e(TAG, "update_course: FAILED", e)
            return """{"error":"更新失败: ${e.message}"}"""
        }

        return """{"success":true,"message":"已更新「$newName」"}"""
    }

    private suspend fun deleteCourse(args: JsonObject): String {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        AiLogger.i(TAG, "delete_course: id=$courseId table=$tableId")

        try {
            courseDao.deleteCourseBase(courseId, tableId)
            AiLogger.i(TAG, "delete_course: SUCCESS")
        } catch (e: Exception) {
            AiLogger.e(TAG, "delete_course: FAILED", e)
            return """{"error":"删除失败: ${e.message}"}"""
        }

        return """{"success":true,"message":"已删除课程ID=$courseId"}"""
    }

    private suspend fun batchImport(args: JsonObject): String {
        val tableId = getDefaultTableId()
        if (tableId == 0) {
            AiLogger.e(TAG, "batch_import: no default table")
            return """{"error":"没有默认课表"}"""
        }

        val coursesArray = args.getAsJsonArray("courses")
        AiLogger.i(TAG, "batch_import: ${coursesArray.size()} courses → table $tableId")

        try {
            courseDao.deleteAllByTable(tableId)
            AiLogger.d(TAG, "batch_import: cleared table $tableId")
        } catch (e: Exception) {
            AiLogger.e(TAG, "batch_import: clear FAILED", e)
            return """{"error":"清除旧数据失败: ${e.message}"}"""
        }

        var count = 0
        var failCount = 0
        coursesArray.forEach { elem ->
            val obj = elem.asJsonObject
            val id = count + 1
            val name = obj.get("name").asString
            val day = obj.get("day").asInt
            val startNode = obj.get("startNode").asInt

            try {
                courseDao.insertCourseBase(CourseBaseBean(
                    id = id, courseName = name, color = "${id % 7}", tableId = tableId
                ))
                courseDao.insertCourseDetail(CourseDetailBean(
                    id = id, day = day,
                    room = if (obj.has("room")) obj.get("room").asString else "",
                    teacher = if (obj.has("teacher")) obj.get("teacher").asString else "",
                    startNode = startNode,
                    step = if (obj.has("step")) obj.get("step").asInt else 2,
                    startWeek = if (obj.has("startWeek")) obj.get("startWeek").asInt else 1,
                    endWeek = if (obj.has("endWeek")) obj.get("endWeek").asInt else 20,
                    type = 0, tableId = tableId
                ))
                AiLogger.d(TAG, "batch_import: [$id] 「$name」 day=$day node=$startNode OK")
                count++
            } catch (e: Exception) {
                AiLogger.e(TAG, "batch_import: [$id] 「$name」 FAILED", e)
                failCount++
            }
        }

        AiLogger.i(TAG, "batch_import: DONE — $count success, $failCount failed")

        return if (failCount == 0) {
            """{"success":true,"message":"已导入${count}门课程","count":$count}"""
        } else {
            """{"success":true,"message":"导入${count}门成功，${failCount}门失败","count":$count,"failed":$failCount}"""
        }
    }

    private suspend fun getDefaultTableId(): Int {
        val table = tableDao.getDefaultTableSync()
        val id = table?.id ?: tableDao.getAllTablesSync().firstOrNull()?.id ?: 0
        AiLogger.d(TAG, "defaultTableId=$id")
        return id
    }
}
