package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import com.shatyuka.zhiliao.Helper.JacksonHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Arrays
import java.util.Locale
import java.util.Optional
import java.util.regex.Pattern
import java.util.stream.Collectors

class HotListFilter : BaseHook() {

    private lateinit var feedsHotListFragment2: Class<*>
    private lateinit var rankFeedList: Class<*>
    private lateinit var ZHObjectList: Class<*>
    private lateinit var ZHObjectListDataField: Field
    private lateinit var rankFeedModule: Class<*>
    private lateinit var rankFeed: Class<*>
    private lateinit var rankFeedContent: Class<*>
    private lateinit var response: Class<*>
    private lateinit var response_bodyField: Field
    private lateinit var retAndArgTypeQqResponseMethodList: List<Method>
    private var templateCardModel: Class<*>? = null
    private lateinit var templateCardModel_dataField: Field
    private lateinit var basePagingFragment: Class<*>

    private companion object {
        val QUESTION_URL_PATTERN: Pattern = Pattern.compile("zhihu\\.com/question/")

        // 买热搜的钱也算研发资金吗?
        val SHIT_AD_KEY_LIST =
            setOf("red mi", "redmi", "xiaomi", "xiao mi", "红米", "小米", "雷军", "su7", "su 7")
    }


    override fun getName(): String {
        return "热榜和底部推荐过滤"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        basePagingFragment =
            classLoader.loadClass("com.zhihu.android.app.ui.fragment.paging.BasePagingFragment")
        feedsHotListFragment2 =
            classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsHotListFragment2")
        rankFeedList = classLoader.loadClass("com.zhihu.android.api.model.RankFeedList")
        ZHObjectList = classLoader.loadClass("com.zhihu.android.api.model.ZHObjectList")
        ZHObjectListDataField = ZHObjectList.getDeclaredField("data")
        ZHObjectListDataField.isAccessible = true

        rankFeedModule = classLoader.loadClass("com.zhihu.android.api.model.RankFeedModule")
        rankFeed = classLoader.loadClass("com.zhihu.android.api.model.RankFeed")
        rankFeedContent = classLoader.loadClass("com.zhihu.android.api.model.RankFeedContent")

        response = classLoader.loadClass("retrofit2.Response")
        response_bodyField = Arrays.stream(response.declaredFields)
            .filter { field: Field -> field.type == Any::class.java }.findFirst().get()
        response_bodyField.isAccessible = true

        retAndArgTypeQqResponseMethodList =
            Arrays.stream(feedsHotListFragment2.getDeclaredMethods())
                .filter { method: Method -> method.returnType == response }
                .filter { method: Method -> method.parameterCount == 1 }
                .filter { method: Method -> method.getParameterTypes()[0] == response }
                .collect(Collectors.toList())

        try {
            templateCardModel = classLoader.loadClass("com.zhihu.android.bean.TemplateCardModel")
            templateCardModel_dataField = templateCardModel!!.getField("data")
            templateCardModel_dataField.isAccessible = true
        } catch (e: Exception) {
            logE(e)
        }
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
            return
        }

        hookAllMethods(feedsHotListFragment2, "postRefreshSucceed", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                preProcessRankFeed(param.args[0])
                filterRankFeed(param.args[0])
            }
        })

        hookAllMethods(basePagingFragment, "postLoadMoreSucceed", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.thisObject.javaClass == feedsHotListFragment2) {
                    filterRankFeed(param.args[0])
                }
            }
        })

        for (processRankList in retAndArgTypeQqResponseMethodList) {
            XposedBridge.hookMethod(processRankList, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val rankFeedList = response_bodyField[param.args[0]] ?: return
                    val rankFeedListData = ZHObjectListDataField[rankFeedList] as List<*>
                    if (rankFeedListData.isEmpty()) {
                        return
                    }

                    // 热榜全部展示, 不折叠
                    XposedHelpers.setObjectField(rankFeedList, "display_num", rankFeedListData.size)
                }
            })
        }
    }

    private fun filterRankFeed(rankFeedListInstance: Any?) {
        if (rankFeedListInstance == null) {
            return
        }
        val rankListData = ZHObjectListDataField[rankFeedListInstance] as MutableList<*>
        if (rankListData.isEmpty()) {
            return
        }
        rankListData.removeIf { feed ->
            try {
                return@removeIf preFilter(feed) || isAd(feed as Any) || shouldFilterEveryoneSeeRankFeed(
                    feed
                )
            } catch (e: Exception) {
                logE(e)
                return@removeIf false
            }
        }
    }

    private fun preFilter(rankFeedInstance: Any?): Boolean {
        return rankFeedInstance == null || rankFeedInstance.javaClass == rankFeedModule
    }

    private fun shouldFilterEveryoneSeeRankFeed(rankFeedInstance: Any?): Boolean {
        if (rankFeedInstance == null || rankFeedInstance.javaClass != templateCardModel) {
            return false
        }
        val data = templateCardModel_dataField[rankFeedInstance]
        val target = JacksonHelper.JsonNode_get.invoke(data, "target")
        if (Helper.regex_title != null) {
            val title = JacksonHelper.JsonNode_get.invoke(
                JacksonHelper.JsonNode_get.invoke(target, "title_area"), "text"
            )?.toString()
            if (Helper.regex_title.matcher(title as CharSequence).find()) {
                return true
            }
        }
        if (Helper.regex_author != null) {
            val author = JacksonHelper.JsonNode_get.invoke(
                JacksonHelper.JsonNode_get.invoke(
                    target, "author_area"
                ), "name"
            )?.toString()
            if (Helper.regex_author.matcher(author as CharSequence).find()) {
                return true
            }
        }
        if (Helper.regex_content != null) {
            // not full content
            if (Helper.regex_content.matcher(
                    JacksonHelper.JsonNode_get.invoke(
                        JacksonHelper.JsonNode_get.invoke(
                            target, "excerpt_area"
                        ), "text"
                    )?.toString() ?: ""
                ).find()
            ) {
                return true
            }
        }
        return false
    }

    private fun preProcessRankFeed(rankFeed: Any) {
        XposedHelpers.setObjectField(rankFeed, "head_zone", null)
        XposedHelpers.setObjectField(rankFeed, "headZones", emptyList<Any>())
    }

    private fun isAd(rankFeedInstance: Any): Boolean {
        return !isZhihuQuestionUrl(rankFeedInstance)
    }

    private fun isShitAd(rankFeedInstance: Any): Boolean {
        if (rankFeedInstance.javaClass == rankFeed) {
            try {
                val target = XposedHelpers.getObjectField(rankFeedInstance, "target")
                val titleArea = XposedHelpers.getObjectField(target, "titleArea")
                val title = XposedHelpers.getObjectField(titleArea, "text") as String
                for (key in SHIT_AD_KEY_LIST) {
                    if (title.lowercase(Locale.getDefault()).contains(key)) {
                        return true
                    }
                }
            } catch (e: Exception) {
                logE(e)
            }
        }
        return false
    }

    private fun isZhihuQuestionUrl(rankFeedInstance: Any): Boolean {
        if (rankFeedInstance.javaClass == rankFeed) {
            try {
                val target = XposedHelpers.getObjectField(rankFeedInstance, "target")
                val linkAreaInstance = XposedHelpers.getObjectField(target, "linkArea")
                val url = Optional.ofNullable(
                    XposedHelpers.getObjectField(
                        linkAreaInstance, "url"
                    ) as String
                ).orElse("")
                return QUESTION_URL_PATTERN.matcher(url).find()
            } catch (e: Exception) {
                logE(e)
            }
        }
        return true
    }
}
