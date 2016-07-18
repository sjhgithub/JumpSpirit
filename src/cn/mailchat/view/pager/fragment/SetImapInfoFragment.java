package cn.mailchat.view.pager.fragment;

import cn.mailchat.R;
import cn.mailchat.view.pager.base.BaseTabFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @copyright © 35.com
 * @file name ：SetImapInfoFragment.java
 * @author ：zhangjx
 * @create Data ：2015-6-4下午5:32:11
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-6-4下午5:32:11
 * @Modified by：zhangjx
 * @Description :开启imap方法帮助页
 */
public class SetImapInfoFragment extends BaseTabFragment  {

    protected static final String TAG = SetImapInfoFragment.class.getSimpleName();
    public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
    private int mCatalog;

    protected int getLayoutRes() {
        return R.layout.fragment_setimapinfo_tab;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mCatalog = args.getInt(BUNDLE_KEY_CATALOG);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ImageView onPageSelected=(ImageView)view.findViewById(R.id.imgView);
        switch (mCatalog) {
            case SetImapInfoTab.CATALOG_QQ:
                onPageSelected.setBackgroundResource(R.drawable.bg_qq_setinfo);
                break;
            case SetImapInfoTab.CATALOG_163:
                onPageSelected.setBackgroundResource(R.drawable.bg_163_setinfo);
                break;
            case SetImapInfoTab.CATALOG_SINA:
                onPageSelected.setBackgroundResource(R.drawable.bg_sina_setinfo);
                break;
        }

    }
}
