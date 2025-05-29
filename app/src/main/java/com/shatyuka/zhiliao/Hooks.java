package com.shatyuka.zhiliao;

import android.widget.Toast;

import com.shatyuka.zhiliao.hooks.AnswerAd;
import com.shatyuka.zhiliao.hooks.AnswerListAd;
import com.shatyuka.zhiliao.hooks.Article;
import com.shatyuka.zhiliao.hooks.AutoRefresh;
import com.shatyuka.zhiliao.hooks.CardViewFeatureShortFilter;
import com.shatyuka.zhiliao.hooks.CardViewMixShortFilter;
import com.shatyuka.zhiliao.hooks.Cleaner;
import com.shatyuka.zhiliao.hooks.ColorMode;
import com.shatyuka.zhiliao.hooks.CommentAd;
import com.shatyuka.zhiliao.hooks.ConfigFetcher;
import com.shatyuka.zhiliao.hooks.CustomFilter;
import com.shatyuka.zhiliao.hooks.CustomFilterV2;
import com.shatyuka.zhiliao.hooks.ExternLink;
import com.shatyuka.zhiliao.hooks.FeedAd;
import com.shatyuka.zhiliao.hooks.FeedTopHotBanner;
import com.shatyuka.zhiliao.hooks.FollowButton;
import com.shatyuka.zhiliao.hooks.FollowButtonFeatureUI;
import com.shatyuka.zhiliao.hooks.FullScreen;
import com.shatyuka.zhiliao.hooks.HotBanner;
import com.shatyuka.zhiliao.hooks.HotListFilter;
import com.shatyuka.zhiliao.hooks.IHook;
import com.shatyuka.zhiliao.hooks.LaunchAd;
import com.shatyuka.zhiliao.hooks.MineHybridView;
import com.shatyuka.zhiliao.hooks.NavButton;
import com.shatyuka.zhiliao.hooks.NavRes;
import com.shatyuka.zhiliao.hooks.NextAnswer;
import com.shatyuka.zhiliao.hooks.PanelBubble;
import com.shatyuka.zhiliao.hooks.RedDot;
import com.shatyuka.zhiliao.hooks.SearchAd;
import com.shatyuka.zhiliao.hooks.ShareAd;
import com.shatyuka.zhiliao.hooks.Tag;
import com.shatyuka.zhiliao.hooks.ThirdPartyLogin;
import com.shatyuka.zhiliao.hooks.TopstoryBanner;
import com.shatyuka.zhiliao.hooks.TopTabs;
import com.shatyuka.zhiliao.hooks.VIPBanner;
import com.shatyuka.zhiliao.hooks.WebView;
import com.shatyuka.zhiliao.hooks.ZhihuPreference;

public class Hooks {
    static final IHook[] HOOKS = {
            new ZhihuPreference(),
            new LaunchAd(),
            new CustomFilter(),
            new FeedAd(),
            new AnswerListAd(),
            new AnswerAd(),
            new CommentAd(),
            new ShareAd(),
            new NextAnswer(),
            new RedDot(),
            new ExternLink(),
            new VIPBanner(),
            new NavButton(),
            new HotBanner(),
            new ColorMode(),
            new Article(),
            new Tag(),
            new SearchAd(),
            new ThirdPartyLogin(),
            new NavRes(),
            new WebView(),
            new Cleaner(),
            new FeedTopHotBanner(),
            new MineHybridView(),
            new FollowButton(),
            new FollowButtonFeatureUI(),
            new CardViewFeatureShortFilter(),
            new CardViewMixShortFilter(),
            new HotListFilter(),
            new AutoRefresh(),
            new PanelBubble(),
            new FullScreen(),
            new TopTabs(),
            new TopstoryBanner(),
            new CustomFilterV2(),
            new ConfigFetcher(),
    };

    public static void init(final ClassLoader classLoader) {
        for (IHook hook : HOOKS) {
            try {
                hook.init(classLoader);
                hook.hook();
            } catch (Throwable e) {
                Helper.toastIfUpdated(hook.getName() + "功能加载失败，可能不支持当前版本知乎: " + Helper.zhihuPackageInfo.versionName, Toast.LENGTH_LONG);
                Helper.logE(hook.getClass().getSimpleName(), e);
            }
        }
        afterHooksInit();
    }

    private static void afterHooksInit() {
        Helper.savePackageInfo();
    }
}
