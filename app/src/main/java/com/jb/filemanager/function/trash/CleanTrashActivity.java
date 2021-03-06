package com.jb.filemanager.function.trash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jb.filemanager.BaseActivity;
import com.jb.filemanager.R;
import com.jb.filemanager.TheApplication;
import com.jb.filemanager.function.scanframe.clean.event.CleanCheckedFileSizeEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanNoneCheckedEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanProgressDoneEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanScanDoneEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanScanFileSizeEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanScanPathEvent;
import com.jb.filemanager.function.scanframe.clean.event.CleanStateEvent;
import com.jb.filemanager.function.trash.adapter.CleanListAdapter;
import com.jb.filemanager.function.trash.presenter.CleanTrashPresenter;
import com.jb.filemanager.function.trash.presenter.Contract;
import com.jb.filemanager.function.trash.view.floatingelv.FloatingGroupExpandableListView;
import com.jb.filemanager.function.trash.view.floatingelv.WrapperExpandableListAdapter;
import com.jb.filemanager.util.ConvertUtils;
import com.jb.filemanager.util.WindowUtil;
import com.jb.filemanager.util.imageloader.IconLoader;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xiaoyu on 2016/11/9 13:46.<br>
 * 清理完成之后的动画进入在line<br>
 * {$date}
 */

public class CleanTrashActivity extends BaseActivity implements Contract.ICleanMainView {

    private CleanTrashPresenter mPresenter = new CleanTrashPresenter(this);
    private boolean mIsScanning;

    private TextView mTvScanProgress;
    private TextView mTvChosenSize;
    private TextView mTvTrashPath;
    private FloatingGroupExpandableListView mCleanTrashExpandableListView;
    private ImageView mIvCleanButton;
    private CleanListAdapter mAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_trash);
        IconLoader.ensureInitSingleton(TheApplication.getAppContext());
        IconLoader.getInstance().bindServicer(this);
        com.jb.filemanager.util.imageloader.ImageLoader.getInstance(TheApplication.getAppContext());
        initView();
        initData();
    }

    private void initView() {
        mTvScanProgress = (TextView) findViewById(R.id.tv_scan_progress);
        mTvChosenSize = (TextView) findViewById(R.id.tv_chosen_size);
        mTvTrashPath = (TextView) findViewById(R.id.tv_trash_path);
        mCleanTrashExpandableListView = (FloatingGroupExpandableListView) findViewById(R.id.clean_trash_expandable_list_view);
        mIvCleanButton = (ImageView) findViewById(R.id.iv_clean_button);

        mAdapter = new CleanListAdapter(mPresenter.getDataGroup(), this);
        mCleanTrashExpandableListView.setAdapter(new WrapperExpandableListAdapter(mAdapter));
    }

    private void initData() {
        if (!TheApplication.getGlobalEventBus().isRegistered(mSubscriber)) {
            TheApplication.getGlobalEventBus().register(mSubscriber);
        }
        mIsScanning = true;
        mPresenter.enterCleanMainFragment();
    }

    private void setTotalCheckedSizeText() {
        long checkedSize = CleanCheckedFileSizeEvent.getJunkFileAllSize(true);
        String sizes = ConvertUtils.formatFileSize(checkedSize);
        mTvChosenSize.setText(sizes);
    }

    private void keepScreenOn(boolean keep) {
        WindowUtil.keepScreenOn(getWindow(), keep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IconLoader.getInstance().unbindServicer(this);
        if (TheApplication.getGlobalEventBus().isRegistered(mSubscriber)) {
            TheApplication.getGlobalEventBus().unregister(mSubscriber);
        }
    }

    @Override
    public void onFileScanning() {
        setTotalCheckedSizeText();
    }

    @Override
    public void onFileScanFinish() {
        setTotalCheckedSizeText();
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void expandGroup(int index) {
        mCleanTrashExpandableListView.expandGroup(index);
    }

    @Override
    public void onDeleteStart() {
        mCleanTrashExpandableListView.setEnabled(false);
    }

    @Override
    public void updateProgress(float process) {
        mTvScanProgress.setText(process + "% has been done");
    }

    @Override
    public void onDeleteFinish() {
        mAdapter.notifyDataSetChanged();
    }

    private Object mSubscriber = new Object() {

        /**
         * 扫描到选中的文件的大小
         *
         * @param event e
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanCheckedFileSizeEvent event) {
//            Logger.i(TAG, "CleanCheckedFileSizeEvent 扫描到选中的文件的大小");
            setTotalCheckedSizeText();
        }

        /**
         * 扫描到的文件的大小
         *
         * @param event e
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanScanFileSizeEvent event) {
            //Logger.i(TAG, "CleanScanFileSizeEvent 扫描到的文件的大小");
            mAdapter.notifyDataSetChanged();
        }

        /**
         * 扫描完成事件
         *
         * @param event event
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanScanDoneEvent event) {
            //Logger.i(TAG, "CleanScanDoneEvent 扫描完成事件" + event.name());
            mPresenter.updateProgressState();
            if (CleanScanDoneEvent.isAllDoneWithoutMemory()) {
                keepScreenOn(false);
            }
        }

        /**
         * 一旦扫显示了SD卡的扫描路径，就不再显示系统缓存扫描的路径
         */
        private boolean mIsSDCardPathShow = false;

        /**
         * 监听SD卡文件扫描路径事件
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(final CleanScanPathEvent event) {
            //Logger.i(TAG, "CleanScanPathEvent 监听SD卡文件扫描路径事件");
            if (event.equals(CleanScanPathEvent.SDCard)) {
                mIsSDCardPathShow = true;
            } else if (event.equals(CleanScanPathEvent.SysCache)
                    && mIsSDCardPathShow) {
                return;
            }
            TheApplication.postRunOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvTrashPath.setText(event.getPath());
                }
            });
        }

        /**
         * 监听扫描完成动画的结束事件
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanProgressDoneEvent event) {
            if (CleanProgressDoneEvent.isAllDoneWithoutMemory()) {
                mIsScanning = false;
                mPresenter.onScanFinish();
                boolean isAllEmpty = mPresenter.removeEmptyGroup();
                //mAdapter.notifyDataSetChanged();
                if (isAllEmpty) {
                    // 没有可以删除的文件，直接跳转到结果页
                    mIvCleanButton.setVisibility(View.GONE);

                    // TODO: 2017/6/27 add by --miwo 处理跳转
                    /*Intent intent = new Intent(CleanTrashActivity.this, CommonFinishActivity.class);
                    intent.putExtra(CommonFinishActivity.COMMON_FINISH_ENTRANCE_KEY, CommonFinishActivity.COMMON_FINISH_ENTRANCE_TRASH);
                    intent.putExtra(CommonFinishActivity.COMMON_FINISH_IS_BEST, true);
                    intent.putExtra(CommonFinishActivity.COMMON_FINISH_BEST_HAS_SCAN, true);
                    intent.putExtra(CommonFinishActivity.COMMON_FINISH_ICON_RESOURCE_KEY, R.drawable.ic_finish_icon_trash);
                    intent.putExtra(CommonFinishActivity.COMMON_FINISH_RESULT_DESC_LINE_1_KEY, getString(R.string.junk_cleaned));
                    startActivity(intent);
                    overridePendingTransition(R.anim.scale_in, R.anim.nothing);

                    finish();*/
                    Toast.makeText(CleanTrashActivity.this, "Had nothing to show and no where to go", Toast.LENGTH_SHORT).show();
                } else {
                    showScanResult();
                }
                CleanProgressDoneEvent.cleanAllDone();
            }
        }

        private void showScanResult() {
//            Logger.i(TAG, "showScanResult");
            mTvTrashPath.setText("");
            mIsScanning = false;
            mPresenter.updateDefaultCheckedState();
            mPresenter.setAllProgressFinish();
            // 展开指定的组
            mPresenter.expandAssignGroup();
            updateCleanBtnEnable();
            mAdapter.notifyDataSetChanged();
            setTotalCheckedSizeText();
        }

        /**
         * 更新清理按钮的可用状态
         */
        private void updateCleanBtnEnable() {
            //mCleanBtn.setEnabled(!mPresenter.isNoneGroupChecked());
        }

        /**
         * 监听状态改变的事件
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanStateEvent event) {
            if ((event.equals(CleanStateEvent.DELETE_FINISH) || event
                    .equals(CleanStateEvent.DELETE_SUSPEND))) {
                // 完成删除或者停止删除都跳转到结果页
                //startDoneActivity(CleanConstants.DONE_ACTIVITY_INTENT_EXTRA_NORMAL);
            }
        }

        /**
         * 监听文件选中状态改变
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(CleanNoneCheckedEvent event) {
//            Logger.e("SCROLL", "监听到文件选中状态的改变");
//            setTotalCheckedSizeText();
        }
    };
}