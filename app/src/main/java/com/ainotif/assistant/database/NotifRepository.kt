package com.ainotif.assistant.database

import com.ainotif.assistant.ui.log.LogCollector

class NotifRepository(private val dao: NotifDao, private val actionDao: ActionDao) {
    suspend fun insert(pkg: String, title: String?, text: String?, category: String, postedAt: Long): Int {
        return dao.insertAndGetId(NotifEntity(
            pkg = pkg,
            title = title,
            text = text,
            category = category,
            postedAt = postedAt
        ))
    }

    fun getAll() = dao.getAll()

    suspend fun getNotifById(notifId: Int): NotifEntity? = dao.getById(notifId)

    suspend fun getActionByNotifId(notifId: Int): ActionEntity? = actionDao.getByNotifId(notifId)

    suspend fun recordAction(notifId: Int, choice: String, delayMin: Long?, hour: Int?, weekday: Int?, suggestedChoice: String? = null) {
        actionDao.insert(ActionEntity(
            notifId = notifId,
            choice = choice,
            delayMin = delayMin,
            hour = hour,
            weekday = weekday,
            openedAfter = null,
            suggestedChoice = suggestedChoice
        ))
        dao.updateStatus(notifId, choice)
    }

    suspend fun markOpened(notifId: Int, opened: Boolean) {
        val action = actionDao.getByNotifId(notifId)
        if (action?.openedAfter == null) {
            actionDao.updateOpenedAfter(notifId, opened)
            dao.updateStatus(notifId, if (opened) "opened" else "dismissed")
            LogCollector.info("notifId=$notifId openedAfter=$opened")
        }
    }

    fun getOpenedActionsWithCategory(): List<ActionWithCategory> = actionDao.getOpenedActionsWithCategory()

    suspend fun getAllActions(): List<ActionEntity> = actionDao.getAllActions()

    suspend fun getAllActionsNonDebug(): List<ActionEntity> = actionDao.getAllActionsNonDebug()

    suspend fun getUnsyncedNotifs(): List<NotifEntity> = dao.getUnsynced()

    suspend fun getUnsyncedActions(): List<ActionEntity> = actionDao.getUnsynced()

    suspend fun markNotifSynced(notifId: Int) = dao.markSynced(notifId)

    suspend fun markActionSynced(notifId: Int) = actionDao.markSynced(notifId)
}
