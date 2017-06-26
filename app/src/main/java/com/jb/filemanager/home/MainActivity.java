package com.jb.filemanager.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jb.filemanager.BaseActivity;
import com.jb.filemanager.R;
import com.jb.filemanager.function.privacy.PrivacyGuardActivity;
import com.jb.filemanager.function.splash.SplashActivity;
import com.jb.filemanager.manager.file.FileManager;
import com.jb.filemanager.util.APIUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends PrivacyGuardActivity implements MainContract.View, View.OnClickListener {

    public static final String ACTION_AGREE_PRIVACY = "action_agree_privacy";

    private MainContract.Presenter mPresenter;

    private MainDrawer mDrawer;

    private ImageView mIvActionBarMenu;
    private TextView mTvActionBarTitle;
    private EditText mEtActionBarSearch;
    private ImageView mIvActionBarSearch;
    private ImageView mIvActionBarMore;

    private PhoneStoragePagerAdapter mPagerAdapter;
    private ViewPager mVpPhoneStorage;
    private ViewPager.SimpleOnPageChangeListener mViewPageChangeListener;
    private TabLayout mTlViewPageTab;
    private LinearLayout mLlBottomOperateContainer;

    private View mViewSearchMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mPresenter != null) {
            mPresenter.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }

        if (mVpPhoneStorage != null) {
            if (mViewPageChangeListener != null) {
                mVpPhoneStorage.removeOnPageChangeListener(mViewPageChangeListener);
                mViewPageChangeListener = null;
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onPressedHomeKey() {
        if (mPresenter != null) {
            mPresenter.onPressHomeKey();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPresenter != null) {
            mPresenter.onClickBackButton(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPresenter != null) {
            mPresenter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void agreePrivacy() {
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action) && !action.equals(ACTION_AGREE_PRIVACY)) {
            gotoSplashActivity();
        }

        initView();

        mPresenter = new MainPresenter(this, new MainSupport());
        mPresenter.onCreate(getIntent());

        initDrawer(this);
    }

    // public start


    // public end


    // private start

    private void initView() {
        mIvActionBarMenu = (ImageView) findViewById(R.id.iv_main_action_bar_menu);
        if (mIvActionBarMenu != null) {
            mIvActionBarMenu.setOnClickListener(this);
        }

        mTvActionBarTitle = (TextView) findViewById(R.id.tv_main_action_bar_title);
        if (mTvActionBarTitle != null) {
            mTvActionBarTitle.getPaint().setAntiAlias(true);
        }

        mEtActionBarSearch = (EditText) findViewById(R.id.et_main_action_bar_search);
        if (mEtActionBarSearch != null) {
            mEtActionBarSearch.getPaint().setAntiAlias(true);
            mEtActionBarSearch.setOnClickListener(this);
        }

        mIvActionBarSearch = (ImageView) findViewById(R.id.iv_main_action_bar_search);
        if (mIvActionBarSearch != null) {
            mIvActionBarSearch.setOnClickListener(this);
        }

        mIvActionBarMore = (ImageView) findViewById(R.id.iv_main_action_bar_more);
        if (mIvActionBarMore != null) {
            mIvActionBarMore.setOnClickListener(this);
        }

        // Mask 显示后要屏蔽触摸事件
        mViewSearchMask = findViewById(R.id.view_home_search_mask);
        mViewSearchMask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mVpPhoneStorage = (ViewPager) findViewById(R.id.vp_main_phone_storage);
        if (mVpPhoneStorage != null) {
            mPagerAdapter = new PhoneStoragePagerAdapter(getSupportFragmentManager());
            mVpPhoneStorage.setAdapter(mPagerAdapter);

            mViewPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (mPresenter != null) {
                        mPresenter.onSwitchTab(position);
                    }
                }
            };
            mVpPhoneStorage.addOnPageChangeListener(mViewPageChangeListener);
        }

        mTlViewPageTab = (TabLayout) findViewById(R.id.tl_main_view_pager_tab);
        if (mTlViewPageTab != null && mVpPhoneStorage != null) {
            mTlViewPageTab.setupWithViewPager(mVpPhoneStorage);
        }

        mLlBottomOperateContainer = (LinearLayout) findViewById(R.id.ll_main_bottom_operate_container);
        if (mLlBottomOperateContainer != null) {
            TextView cut = (TextView)mLlBottomOperateContainer.findViewById(R.id.tv_main_bottom_cut);
            if (cut != null) {
                cut.getPaint().setAntiAlias(true);
                cut.setOnClickListener(this);
            }

            TextView copy = (TextView)mLlBottomOperateContainer.findViewById(R.id.tv_main_bottom_copy);
            if (copy != null) {
                copy.getPaint().setAntiAlias(true);
                copy.setOnClickListener(this);
            }

            TextView paste = (TextView)mLlBottomOperateContainer.findViewById(R.id.tv_main_bottom_paste);
            if (paste != null) {
                paste.getPaint().setAntiAlias(true);
                paste.setOnClickListener(this);
            }

            ImageView more = (ImageView)mLlBottomOperateContainer.findViewById(R.id.iv_main_bottom_more);
            if (more != null) {
                more.setOnClickListener(this);
            }
        }

    }

    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }

    private void initDrawer(BaseActivity activity) {
        if (mDrawer == null) {
            mDrawer = new MainDrawer(activity);
        }
        mDrawer.initDrawer();
    }


    // private end



    // implements MainContract.View


    @Override
    public void showNormalStatus(int tabPos) {
        int moreButtonStatus;
        switch (tabPos) {
            case 0:
                moreButtonStatus = View.GONE;
                break;
            case 1:
                moreButtonStatus = View.VISIBLE;
                break;
            default:
                moreButtonStatus = View.GONE;
                break;
        }

        if (mEtActionBarSearch != null) {
            mEtActionBarSearch.setVisibility(View.GONE);
        }

        if (mIvActionBarMenu != null) {
            mIvActionBarMenu.setVisibility(View.VISIBLE);
        }

        if (mTvActionBarTitle != null) {
            mTvActionBarTitle.setVisibility(View.VISIBLE);
        }

        if (mIvActionBarMore != null) {
            mIvActionBarMore.setVisibility(moreButtonStatus);
        }

        if (mViewSearchMask != null) {
            mViewSearchMask.setVisibility(View.GONE);
        }

    }

    @Override
    public void showSearchStatus() {
        if (mEtActionBarSearch != null) {
            mEtActionBarSearch.setVisibility(View.VISIBLE);
        }

        if (mIvActionBarMenu != null) {
            mIvActionBarMenu.setVisibility(View.GONE);
        }

        if (mTvActionBarTitle != null) {
            mTvActionBarTitle.setVisibility(View.GONE);
        }

        if (mIvActionBarMore != null) {
            mIvActionBarMore.setVisibility(View.GONE);
        }

        if (mViewSearchMask != null) {
            mViewSearchMask.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showActionMoreOperatePopWindow() {
        int resId = R.layout.pop_main_action_more;

        // 一个自定义的布局，作为显示的内容
        View contentView = getLayoutInflater().inflate(resId, null);
        TextView newFolder = (TextView) contentView.findViewById(R.id.tv_main_action_more_new_folder);
        TextView sortBy = (TextView) contentView.findViewById(R.id.tv_main_action_more_sort_by);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(APIUtil.getDrawable(this, R.color.white));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(mIvActionBarMore,
                mIvActionBarMore.getWidth(),
                0);

        if (newFolder != null) {
            newFolder.getPaint().setAntiAlias(true);
            newFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    if (mPresenter != null) {
                        mPresenter.onClickActionNewFolderButton();
                    }
                }
            });
        }

        if (sortBy != null) {
            sortBy.getPaint().setAntiAlias(true);
            sortBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    if (mPresenter != null) {
                        mPresenter.onClickActionSortByButton();
                    }
                }
            });
        }
    }

    @Override
    public void showBottomMoreOperatePopWindow(boolean multiSelected) {
        int resId;
        if (multiSelected) {
            resId = R.layout.pop_mutli_file_operate_more;
        } else {
            resId = R.layout.pop_single_file_operate_more;
        }

        // 一个自定义的布局，作为显示的内容
        View contentView = getLayoutInflater().inflate(resId, null);
        TextView details = (TextView) contentView.findViewById(R.id.tv_main_operate_more_detail);
        TextView rename = (TextView) contentView.findViewById(R.id.tv_main_operate_more_rename);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(APIUtil.getDrawable(this, R.color.white));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(mLlBottomOperateContainer,
                mLlBottomOperateContainer.getWidth() - contentView.getWidth(),
                5);

        if (details != null) {
            details.getPaint().setAntiAlias(true);
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPresenter != null) {
                        mPresenter.onClickOperateDetailButton();
                    }
                    popupWindow.dismiss();
                }
            });
        }

        if (rename != null) {
            rename.getPaint().setAntiAlias(true);
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPresenter != null) {
                        mPresenter.onClickOperateRenameButton();
                    }
                    popupWindow.dismiss();
                }
            });
        }
    }

    @Override
    public void showNewFolderDialog() {
        // TODO
        Toast.makeText(this, "显示创建文件夹", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSortByDialog() {
        // TODO
        Toast.makeText(this, "显示排序dialog", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSelectedFileChange() {
        ArrayList<File> selectedFiles = FileManager.getInstance().getSelectedFiles();
        if (selectedFiles != null && selectedFiles.size() > 0) {
            mLlBottomOperateContainer.setVisibility(View.VISIBLE);
        } else {
            mLlBottomOperateContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void openDrawer(int openType) {
        if (mDrawer != null) {
            mDrawer.setOpenType(openType);
            mDrawer.openDrawerWithDelay(0);
        }
    }

    @Override
    public void finishActivity() {
        super.onBackPressed();
    }

    // implements View.OnClickListener
    @Override
    public void onClick(View view) {
        if (mQuickClickGuard.isQuickClick(view.getId())) {
            return;
        }
        switch (view.getId()){
            case R.id.iv_main_action_bar_menu:
                if (mPresenter != null) {
                    mPresenter.onClickDrawerButton();
                }
                break;
            case R.id.iv_main_action_bar_search:
                if (mPresenter != null) {
                    mPresenter.onClickActionSearchButton();
                }
                break;
            case R.id.iv_main_action_bar_more:
                if (mPresenter != null) {
                    mPresenter.onClickActionMoreButton();
                }
                break;
            case R.id.tv_main_bottom_cut:
                if (mPresenter != null) {
                    mPresenter.onClickOperateCutButton();
                }
                break;
            case R.id.tv_main_bottom_copy:
                if (mPresenter != null) {
                    mPresenter.onClickOperateCopyButton();
                }
                break;
            case R.id.tv_main_bottom_paste:
                if (mPresenter != null) {
                    mPresenter.onClickOperatePasteButton();
                }
                break;
            case R.id.iv_main_bottom_more:
                if (mPresenter != null) {
                    mPresenter.onClickOperateMoreButton();
                }
                break;
            default:
                break;
        }
    }





    // private class

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    private class PhoneStoragePagerAdapter extends FragmentPagerAdapter {
        PhoneStoragePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i){
                case 0: {
                    fragment = new CategoryFragment();
//                    Bundle args = new Bundle();
//                    fragment.setArguments(args);
                }
                    break;
                case 1: {
                    fragment = new StorageFragment();
//                    Bundle args = new Bundle();
//                    fragment.setArguments(args);
                }
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String result;
            switch (position){
                case 0:
                    result = getString(R.string.main_category_title);
                    break;
                case 1:
                    result = getString(R.string.main_storage_title);
                    break;
                default:
                    result = "";
                    break;
            }
            return result;
        }
    }
}
