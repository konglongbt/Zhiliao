package com.shatyuka.zhiliao.hooks

import android.view.View
import android.view.ViewGroup
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import java.lang.reflect.Method

class RedDot : BaseHook() {

    private var FeedsTabsFragment: Class<*>? = null
    private var NotiMsgModel: Class<*>? = null
    private var ViewModel: Class<*>? = null
    private var BottomNavMenuItemView_setUnreadCount: Method? = null
    private var BottomNavMenuItemViewForIconOnly_setUnreadCount: Method? = null
    private var BaseBottomNavMenuItemView_setNavBadge: Method? = null
    private var IconWithDotAndCountView_setUnreadCount: Method? = null
    private var CountDotView_setUnreadCount: Method? = null
    private var BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility: Method? = null
    private var RevisitView_getCanShowRedDot: Method? = null
    private var customTabView: Class<*>? = null

    override fun getName(): String {
        return "不显示小红点"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        FeedsTabsFragment =
            classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment")

        try {
            NotiMsgModel =
                classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel")
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            ViewModel =
                classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.help.tabhelp.model.ViewModel")
        } catch (e: Exception) {
            logE(e.message)
        }
        try {
            BottomNavMenuItemView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView"),
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logE(e.message)
        }
        try {
            BottomNavMenuItemViewForIconOnly_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly"),
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            BaseBottomNavMenuItemView_setNavBadge = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BaseBottomNavMenuItemView"),
                classLoader.loadClass("com.zhihu.android.bottomnav.api.model.NavBadge")
            )
        } catch (e: Exception) {
            logE(e)
        }
        try {
            IconWithDotAndCountView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.community_base.view.icon.IconWithDotAndCountView"),
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            CountDotView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.notification.widget.CountDotView"),
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility =
                Helper.getMethodByParameterTypes(
                    classLoader.loadClass("com.zhihu.android.recentlyviewed.ui.viewholder.BaseFeedFollowAvatarViewHolder"),
                    View::class.java,
                    Boolean::class.javaPrimitiveType
                )
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            RevisitView_getCanShowRedDot =
                classLoader.loadClass("com.zhihu.android.app.feed.ui2.tab.RevisitView")
                    .getDeclaredMethod("getCanShowRedDot")
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            customTabView =
                classLoader.loadClass("com.zhihu.android.app.feed.explore.view.CustomTabContainerView\$CustomTabView")
        } catch (e: Exception) {
            logE(e.message)
        }

    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_reddot", false)
        ) {
            return
        }

        XposedBridge.hookAllMethods(
            FeedsTabsFragment, "onUnReadCountLoaded", returnConstant(null)
        )
        if (BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility != null) {
            hookMethod(
                BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility, returnConstant(null)
            )
        }
        if (BottomNavMenuItemView_setUnreadCount != null) {
            hookMethod(
                BottomNavMenuItemView_setUnreadCount, returnConstant(null)
            )
        }
        if (BottomNavMenuItemViewForIconOnly_setUnreadCount != null) {
            hookMethod(
                BottomNavMenuItemViewForIconOnly_setUnreadCount, returnConstant(null)
            )
        }
        if (BaseBottomNavMenuItemView_setNavBadge != null) {
            hookMethod(
                BaseBottomNavMenuItemView_setNavBadge, returnConstant(null)
            )
        }
        if (NotiMsgModel != null) {
            XposedHelpers.findAndHookMethod(
                NotiMsgModel, "getUnreadCount", returnConstant(0)
            )
        }

        if (IconWithDotAndCountView_setUnreadCount != null) {
            hookMethod(
                IconWithDotAndCountView_setUnreadCount, returnConstant(null)
            )
        }
        if (CountDotView_setUnreadCount != null) {
            hookMethod(CountDotView_setUnreadCount, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    val obj = param.thisObject as View
                    obj.visibility = View.GONE
                    return null
                }
            })
        }
        if (ViewModel != null) {
            findAndHookConstructor(ViewModel, View::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (param.args[0] is ViewGroup) {
                        val view = param.args[0] as ViewGroup
                        if (view.childCount == 2) {
                            // red_parent
                            view.visibility = View.GONE
                        }
                    }
                }
            })
        }
        if (RevisitView_getCanShowRedDot != null) {
            hookMethod(RevisitView_getCanShowRedDot, returnConstant(false))
        }

        if (customTabView != null) {
            val setNumText = Helper.getMethodByParameterTypes(customTabView, String::class.java)
            if (setNumText != null) {
                hookMethod(setNumText, DO_NOTHING)
            } else {
                logE(NoSuchMethodException("no CustomTabView#void(String)"))
            }
        }
    }
}
