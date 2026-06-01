package com.example.timereminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.timereminder.alarm.AlarmScheduler
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.data.repository.TagRepository
import com.example.timereminder.ui.screen.edit.ReminderEditScreen
import com.example.timereminder.ui.screen.edit.ReminderEditViewModel
import com.example.timereminder.ui.screen.list.ReminderListScreen
import com.example.timereminder.ui.screen.list.ReminderListViewModel
import com.example.timereminder.ui.screen.tags.TagManageScreen
import com.example.timereminder.ui.screen.tags.TagManageViewModel

/**
 * 导航路由定义
 */
object Routes {
    const val REMINDER_LIST = "reminder_list"
    const val REMINDER_EDIT = "reminder_edit/{reminderId}"
    const val REMINDER_NEW = "reminder_edit/new"
    const val TAG_MANAGE = "tag_manage"

    fun editRoute(reminderId: Long) = "reminder_edit/$reminderId"
}

@Composable
fun TimerReminderNavGraph(
    navController: NavHostController,
    reminderRepository: ReminderRepository,
    tagRepository: TagRepository,
    alarmScheduler: AlarmScheduler
) {
    NavHost(
        navController = navController,
        startDestination = Routes.REMINDER_LIST
    ) {
        // 提醒列表
        composable(Routes.REMINDER_LIST) {
            val viewModel: ReminderListViewModel = viewModel(
                factory = ReminderListViewModel.Factory(reminderRepository, tagRepository, alarmScheduler)
            )
            ReminderListScreen(
                viewModel = viewModel,
                onAddReminder = {
                    navController.navigate(Routes.REMINDER_NEW)
                },
                onEditReminder = { id ->
                    navController.navigate(Routes.editRoute(id))
                },
                onManageTags = {
                    navController.navigate(Routes.TAG_MANAGE)
                }
            )
        }

        // 添加新提醒
        composable(Routes.REMINDER_NEW) {
            val viewModel: ReminderEditViewModel = viewModel(
                factory = ReminderEditViewModel.Factory(
                    reminderRepository, tagRepository, alarmScheduler, null
                )
            )
            ReminderEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 编辑提醒
        composable(
            route = Routes.REMINDER_EDIT,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: return@composable
            val viewModel: ReminderEditViewModel = viewModel(
                factory = ReminderEditViewModel.Factory(
                    reminderRepository, tagRepository, alarmScheduler, reminderId
                )
            )
            ReminderEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 标签管理
        composable(Routes.TAG_MANAGE) {
            val viewModel: TagManageViewModel = viewModel(
                factory = TagManageViewModel.Factory(tagRepository)
            )
            TagManageScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
