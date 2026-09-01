package com.solunis.schedule.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (url.contains("/schedule/sync")) {
            Log.d("MockInterceptor", "Intercepting schedule sync request")
            val json = MOCK_SCHEDULE_JSON
            return Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

        return chain.proceed(request)
    }

    companion object {
        private val MOCK_SCHEDULE_JSON = """
        {
          "semester": "2024-2025-1",
          "startDate": "2024-09-02",
          "maxWeek": 20,
          "courses": [
            {"id":1,"name":"高等数学","color":"0","day":1,"room":"A301","teacher":"张教授","startNode":1,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":2,"name":"大学物理","color":"1","day":1,"room":"B202","teacher":"李教授","startNode":5,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":3,"name":"英语听力","color":"4","day":1,"room":"C105","teacher":"王老师","startNode":9,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":4,"name":"线性代数","color":"3","day":2,"room":"A205","teacher":"赵教授","startNode":3,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":5,"name":"计算机","color":"4","day":2,"room":"D401","teacher":"刘老师","startNode":7,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":1,"name":"高等数学","color":"0","day":3,"room":"A301","teacher":"张教授","startNode":1,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":6,"name":"程序设计","color":"2","day":3,"room":"E302","teacher":"陈老师","startNode":5,"step":3,"startWeek":1,"endWeek":20,"type":0},
            {"id":7,"name":"体育","color":"5","day":3,"room":"操场","teacher":"周老师","startNode":9,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":2,"name":"大学物理","color":"1","day":4,"room":"B202","teacher":"李教授","startNode":1,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":8,"name":"思想政治","color":"6","day":4,"room":"F101","teacher":"吴老师","startNode":3,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":9,"name":"英语写作","color":"4","day":4,"room":"C203","teacher":"王老师","startNode":7,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":4,"name":"线性代数","color":"3","day":5,"room":"A205","teacher":"赵教授","startNode":1,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":6,"name":"程序设计","color":"2","day":5,"room":"E302","teacher":"陈老师","startNode":3,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":10,"name":"实验物理","color":"1","day":5,"room":"G201","teacher":"李教授","startNode":7,"step":3,"startWeek":1,"endWeek":20,"type":0},
            {"id":11,"name":"摄影","color":"5","day":6,"room":"H102","teacher":"孙老师","startNode":3,"step":2,"startWeek":1,"endWeek":20,"type":0},
            {"id":12,"name":"自习","color":"3","day":7,"room":"图书馆","teacher":"","startNode":5,"step":2,"startWeek":1,"endWeek":20,"type":0}
          ]
        }
        """.trimIndent()
    }
}
