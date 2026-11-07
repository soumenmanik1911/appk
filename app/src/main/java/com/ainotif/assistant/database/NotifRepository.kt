package com.ainotif.assistant.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotifRepository(private val dao: NotifDao) {
    suspend fun insert(pkg: String, title: String?, text: String?, category: String, postedAt: Long) {
        dao.insert(NotifEntity(
            pkg = pkg,
            title = title,
            text = text,
            category = category,
            postedAt = postedAt
        ))
    }

    fun getAll() = dao.getAll()
}
