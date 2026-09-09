package com.solunis.schedule.data.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.solunis.schedule.data.database.AppDatabase
import com.solunis.schedule.data.database.entity.CourseBaseBean
import com.solunis.schedule.data.database.entity.CourseDetailBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiToolExecutor(private val database: AppDatabase) {

    companion object {
        const val TAG = "AiToolExecutor"
    }

    private val gson = Gson()
    private val courseDao = database.courseDao()
    private val tableDao = database.tableDao()

    suspend fun execute(name: String, argsJson: String): String = withContext(Dispatchers.IO) {
        try {
            val args = gson.fromJson(argsJson, JsonObject::class.java) ?: JsonObject()
            Log.d(TAG, "execute tool=$name args=$args")
            when (name) {
                "get_schedule" -> getSchedule(args)
                "add_course" -> addCourse(args)
                "update_course" -> updateCourse(args)
                "delete_course" -> deleteCourse(args)
                "batch_import_courses" -> batchImport(args)
                else -> """{"error":"未知工具: $name"}"""
            }
        } catch (e: Exception) {
            Log.e(TAG, "tool error", e)
            """{"error":"${e.message}"}"""
        }
    }

    private suspend fun getSchedule(args: JsonObject): String {
        val tableId = if (args.has("tableId")) args.get("tableId").asInt else getDefaultTableId()
        if (tableId == 0) return """{"courses":[],"message":"暂无课表"}"""

        val courses = courseDao.getCoursesByTableIdSync(tableId)
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
        if (tableId == 0) return """{"error":"没有默认课表"}"""

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
        return """{"success":true,"message":"已添加「$name」","courseId":$nextId}"""
    }

    private suspend fun updateCourse(args: JsonObject): String {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        val courses = courseDao.getCoursesByTableIdSync(tableId)
        val course = courses.firstOrNull { it.id == courseId }
            ?: return """{"error":"课程ID=$courseId 不存在"}"""

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
        return """{"success":true,"message":"已更新「$newName」"}"""
    }

    private suspend fun deleteCourse(args: JsonObject): String {
        val courseId = args.get("courseId").asInt
        val tableId = getDefaultTableId()
        courseDao.deleteCourseBase(courseId, tableId)
        return """{"success":true,"message":"已删除课程ID=$courseId"}"""
    }

    private suspend fun batchImport(args: JsonObject): String {
        val tableId = getDefaultTableId()
        if (tableId == 0) return """{"error":"没有默认课表"}"""

        val coursesArray = args.getAsJsonArray("courses")
        courseDao.deleteAllByTable(tableId)

        var count = 0
        coursesArray.forEach { elem ->
            val obj = elem.asJsonObject
            val id = count + 1
            courseDao.insertCourseBase(CourseBaseBean(
                id = id,
                courseName = obj.get("name").asString,
                color = "${id % 7}",
                tableId = tableId
            ))
            courseDao.insertCourseDetail(CourseDetailBean(
                id = id,
                day = obj.get("day").asInt,
                room = if (obj.has("room")) obj.get("room").asString else "",
                teacher = if (obj.has("teacher")) obj.get("teacher").asString else "",
                startNode = obj.get("startNode").asInt,
                step = if (obj.has("step")) obj.get("step").asInt else 2,
                startWeek = if (obj.has("startWeek")) obj.get("startWeek").asInt else 1,
                endWeek = if (obj.has("endWeek")) obj.get("endWeek").asInt else 20,
                type = 0,
                tableId = tableId
            ))
            count++
        }
        return """{"success":true,"message":"已导入${count}门课程","count":$count}"""
    }

    private suspend fun getDefaultTableId(): Int {
        val table = tableDao.getDefaultTableSync()
        return table?.id ?: tableDao.getAllTablesSync().firstOrNull()?.id ?: 0
    }
}
