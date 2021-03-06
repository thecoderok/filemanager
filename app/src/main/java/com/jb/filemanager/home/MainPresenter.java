package com.jb.filemanager.home;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.jb.filemanager.R;
import com.jb.filemanager.util.FileUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by bill wang on 2017/6/21.
 *
 */

@SuppressWarnings("StatementWithEmptyBody")
class MainPresenter implements MainContract.Presenter {

    public static final int MAIN_STATUS_NORMAL = 0;
    public static final int MAIN_STATUS_SELECT = 1;
    public static final int MAIN_STATUS_CUT = 2;
    public static final int MAIN_STATUS_COPY = 3;

    private MainContract.View mView;
    private MainContract.Support mSupport;

    private boolean mIsInSearchMode;
    private int mCurrentTab = 0;

    private long mExitTime;

    private int mStatus = MAIN_STATUS_NORMAL;

    private ArrayList<File> mSelectedFiles = new ArrayList<>();
    private String mCurrentPath;

    MainPresenter(MainContract.View view, MainContract.Support support) {
        mView = view;
        mSupport = support;
    }

    @Override
    public void onCreate(Intent intent) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        mView = null;
        mSupport = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onClickBackButton(boolean systemBack) {
        if (mView != null) {
            if (systemBack) {
                if (mIsInSearchMode) {
                    mIsInSearchMode = false;
                    mView.showNormalStatus(mCurrentTab);
                } else {
                    if (System.currentTimeMillis() - mExitTime > 2000) {
                        mExitTime = System.currentTimeMillis();
                        Toast.makeText(mSupport.getContext(), R.string.main_double_click_exit_app_tips, Toast.LENGTH_SHORT).show();
                    } else {
                        mView.finishActivity();
                    }
                }
            } else {
                // 首页没有非系统返回
            }
        }
    }

    @Override
    public void onPressHomeKey() {
        // nothing to do
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void onSwitchTab(int pos) {
        if (mView != null) {
            mCurrentTab = pos;
            mView.showNormalStatus(mCurrentTab);
        }
    }

    @Override
    public void onClickDrawerButton() {
        if (mView != null) {
            mView.openDrawer(MainDrawer.CLI_OPEN);
        }
    }

    @Override
    public void onClickActionSearchButton() {
        if (mView != null) {
            mIsInSearchMode = true;
            mView.showSearchStatus();
        }
    }

    @Override
    public void onClickActionMoreButton() {
        if (mView != null) {
            mView.showActionMoreOperatePopWindow();
        }
    }

    @Override
    public void onClickActionNewFolderButton() {
        if (mView != null) {
            mView.showNewFolderDialog();
        }
    }

    @Override
    public void onClickActionSortByButton() {
        if (mView != null) {
            mView.showSortByDialog();
        }
    }

    @Override
    public void onClickOperateCutButton() {
        if (mView != null) {
            mStatus = MAIN_STATUS_CUT;
            mView.updateView();
        }
    }

    @Override
    public void onClickOperateCopyButton() {
        if (mView != null) {
            mStatus = MAIN_STATUS_COPY;
            mView.updateView();
        }
    }

    @Override
    public void onClickOperateDeleteButton() {
        if (mView != null) {
            mView.showDeleteConfirmDialog();
        }
    }

    @Override
    public void onClickOperateMoreButton() {
        if (mView != null) {
            if (mSelectedFiles != null) {
                int size = mSelectedFiles.size();
                // 这里size 只可能是大于等于1，没有选中文件的时候应该不会出现底部操作栏
                switch (size) {
                    case 1:
                        mView.showBottomMoreOperatePopWindow(false);
                        break;
                    default:
                        mView.showBottomMoreOperatePopWindow(true);
                        break;
                }
            }
        }
    }

    @Override
    public void onClickOperateDetailButton() {
        if (mView != null) {
            if (mSelectedFiles != null && mSelectedFiles.size() == 1) {
                mView.showDetailSingleFile(mSelectedFiles.get(0));
            } else {
                mView.showDetailMultiFile(mSelectedFiles);
            }
        }
    }

    @Override
    public void onClickOperateRenameButton() {
        if (mView != null) {
            if (mSelectedFiles != null && mSelectedFiles.size() == 1) {
                mView.showNewFolderDialog();
            }
        }
    }

    @Override
    public void onClickOperateCancelButton() {
        if (mView != null) {
            resetStatus();
            mView.updateView();
        }
    }

    @Override
    public void onClickOperatePasteButton() {
        if (mView != null) {
            ArrayList<File> copyList = new ArrayList<>(mSelectedFiles);
            if (mStatus == MAIN_STATUS_COPY) {
                FileUtil.copyFilesToDest(copyList, mCurrentPath);
            } else if (mStatus == MAIN_STATUS_CUT) {
                FileUtil.cutFilesToDest(copyList, mCurrentPath);
            }

            resetStatus();
            mView.updateView();
        }
    }

    @Override
    public boolean onClickConfirmCreateFolderButton(String name) {
        // TODO 判断输入格式是否符合规定
        return FileUtil.createFolder(name);
    }

    @Override
    public boolean onClickConfirmDeleteButton() {
        boolean result = false;
        if (mView != null) {
            ArrayList<File> deleteFailedFiles = FileUtil.deleteSelectedFiles(mSelectedFiles);
            result = (deleteFailedFiles == null || deleteFailedFiles.size() == 0);

            resetStatus();
            mView.updateView();
        }
        return result;
    }

    @Override
    public boolean onClickConfirmRenameButton(String name) {
        // TODO 判断输入格式是否符合规定
        boolean result = false;
        if (!TextUtils.isEmpty(mCurrentPath) && mSelectedFiles != null && mSelectedFiles.size() == 1) {
            result = FileUtil.renameSelectedFile(mSelectedFiles.get(0), mCurrentPath + File.separator + name);
        }
        return result;
    }

    @Override
    public boolean isSelected(File file) {
        boolean result = false;
        if (file != null) {
            try {
                result = mSelectedFiles.contains(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void addOrRemoveSelected(File file) {
        switch (mStatus) {
            case MAIN_STATUS_NORMAL:
            case MAIN_STATUS_SELECT:
                if (file != null) {
                    try {
                        if (mSelectedFiles.contains(file)) {
                            mSelectedFiles.remove(file);
                        } else {
                            mSelectedFiles.add(file);
                        }

                        if (mSelectedFiles.size() > 0) {
                            mStatus = MAIN_STATUS_SELECT;
                        } else {
                            mStatus = MAIN_STATUS_NORMAL;
                        }

                        mView.updateView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public ArrayList<File> getSelectedFiles() {
        return mSelectedFiles;
    }

    @Override
    public void updateCurrentPath(String path) {
        mCurrentPath = path;
    }

    @Override
    public String getCurrentPath() {
        return mCurrentPath;
    }


    private void resetStatus() {
        mSelectedFiles.clear();
        mStatus = MAIN_STATUS_NORMAL;
    }
}
