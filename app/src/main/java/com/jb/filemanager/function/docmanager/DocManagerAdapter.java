package com.jb.filemanager.function.docmanager;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jb.filemanager.R;
import com.jb.filemanager.TheApplication;
import com.jb.filemanager.commomview.GroupSelectBox;
import com.jb.filemanager.function.applock.adapter.AbsAdapter;
import com.jb.filemanager.function.trash.adapter.ItemCheckBox;
import com.jb.filemanager.util.ConvertUtils;
import com.jb.filemanager.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 * Author lqf
 * Email: liqf@m15.cn
 * Date: 2017/7/4 11:08
 */

public class DocManagerAdapter extends AbsAdapter<DocGroupBean> {

    private static final String TAG = "mCheckedCount";
    private int mCheckedCount;
    private OnItemChosenListener mOnItemChosenListener;
    private ArrayList<DocChildBean> mCheckedFile;

    public DocManagerAdapter(List<DocGroupBean> groups) {
        super(groups);
        mCheckedFile = new ArrayList<>();
    }

    @Override
    public View onGetGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_applock_group, parent, false);
        GroupItemViewHolder viewHolder = new GroupItemViewHolder(convertView);
        final DocGroupBean appGroupBean = mGroups.get(groupPosition);
        viewHolder.mTvGroupTitle.setText(appGroupBean.mGroupTitle);
        viewHolder.mTvGroupTitleItemCount.setText(TheApplication.getAppContext().getString(R.string.item_count, appGroupBean.getchildrenSize()));
        viewHolder.mSelectBox
                .setImageSource(R.drawable.select_none,
                        R.drawable.select_multi,
                        R.drawable.select_all);

        viewHolder.mSelectBox.setState(appGroupBean.mSelectState);
        viewHolder.mSelectBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appGroupBean.mSelectState == GroupSelectBox.SelectState.ALL_SELECTED) {
                    appGroupBean.mSelectState = GroupSelectBox.SelectState.NONE_SELECTED;
                    for (DocChildBean childBean : appGroupBean.getChildren()) {
                        childBean.mIsChecked = false;
                    }
                } else {
                    appGroupBean.mSelectState = GroupSelectBox.SelectState.ALL_SELECTED;
                    for (DocChildBean childBean : appGroupBean.getChildren()) {
                        childBean.mIsChecked = true;
                    }
                }
                handleCheckedCount();
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    @Override
    public View onGetChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (isLastChild && groupPosition == getGroupCount() - 1) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_child_with_bottom_space, parent, false);
        } else if (isLastChild) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_child_with_bottom_space_10, parent, false);
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_child, parent, false);
        }
        ChildItemViewHolder viewHolder = new ChildItemViewHolder(convertView);
        final DocGroupBean appGroupBean = mGroups.get(groupPosition);
        final DocChildBean child = appGroupBean.getChild(childPosition);
        if (child.mFileType == DocChildBean.TYPE_DOC){
            viewHolder.mIvAppIcon.setImageResource(R.drawable.file_type_doc);
        }else if (child.mFileType == DocChildBean.TYPE_PDF){
            viewHolder.mIvAppIcon.setImageResource(R.drawable.file_type_pdf);
        }else if (child.mFileType == DocChildBean.TYPE_TXT){
            viewHolder.mIvAppIcon.setImageResource(R.drawable.file_type_txt);
        }else {
            viewHolder.mIvAppIcon.setImageResource(R.drawable.unknown_icon);
        }

        viewHolder.mTvAppName.setText(child.mDocName);
        viewHolder.mTvAppSize.setText(ConvertUtils.formatFileSize(Long.parseLong(child.mDocSize)));
        viewHolder.mItemCheckBox.setImageRes(R.drawable.select_none,
                R.drawable.select_all);
        viewHolder.mItemCheckBox.setChecked(child.mIsChecked);
        viewHolder.mItemCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                child.mIsChecked = !child.mIsChecked;
                updateGroupState(appGroupBean);
            }
        });
        return convertView;
    }

    private void updateGroupState(DocGroupBean appGroupBean) {
        boolean isAllSelect = true;
        boolean isPartSelect = false;
        for (DocChildBean childBean : appGroupBean.getChildren()) {
            isAllSelect = isAllSelect && childBean.mIsChecked;
            isPartSelect = isPartSelect || childBean.mIsChecked;
        }
        if (isAllSelect) {
            appGroupBean.mSelectState = GroupSelectBox.SelectState.ALL_SELECTED;
        } else if (isPartSelect) {
            appGroupBean.mSelectState = GroupSelectBox.SelectState.MULT_SELECTED;
        } else {
            appGroupBean.mSelectState = GroupSelectBox.SelectState.NONE_SELECTED;
        }
        handleCheckedCount();
        notifyDataSetChanged();
    }

    public void handleCheckedCount() {
        mCheckedCount = 0;
        mCheckedFile.clear();
        for (DocGroupBean groupBean : mGroups) {
            List<DocChildBean> children = groupBean.getChildren();
            for (DocChildBean childBean : children) {
                if (childBean.mIsChecked) {
                    mCheckedFile.add(childBean);
                    mCheckedCount++;
                }
            }
            if (mOnItemChosenListener != null) {
                mOnItemChosenListener.onItemChosen(mCheckedCount);
            }
        }
        Logger.d(TAG, ":    " + mCheckedCount);
    }

    public void setListData(List<DocGroupBean> groups, boolean keepUserCheck) {
        if (keepUserCheck) {
            handleUserChecked(groups);
        }
        mGroups.clear();
        mGroups.addAll(groups);
//        mCheckedCount = groups.get(0).getchildrenSize();//默认用户应用都选中
        notifyDataSetChanged();
    }

    private void handleUserChecked(List<DocGroupBean> groups) {
        //保留用户之前的选中的部分
        for (DocGroupBean groupBean : groups) {
            List<DocChildBean> children = groupBean.getChildren();
            int chosenCount = 0;
            for (int i = 0; i < children.size(); i++) {
                DocChildBean childBean = children.get(i);
                for (DocChildBean child : mCheckedFile) {
                    if (child.mDocPath.equals(childBean.mDocPath)) {
                        Logger.d(TAG, "发现目标" + childBean.mDocPath);
                        childBean.mIsChecked = true;
                        chosenCount++;
                        break;
                    }
                }
            }
            if (chosenCount == 0) {
                groupBean.mSelectState = GroupSelectBox.SelectState.NONE_SELECTED;
            } else if (chosenCount == groupBean.getchildrenSize()) {
                groupBean.mSelectState = GroupSelectBox.SelectState.ALL_SELECTED;
            } else {
                groupBean.mSelectState = GroupSelectBox.SelectState.MULT_SELECTED;
            }
        }
    }

    public void setOnItemChosenListener(@NonNull OnItemChosenListener itemChosnListener) {
        this.mOnItemChosenListener = itemChosnListener;
    }

    interface OnItemChosenListener {
        void onItemChosen(int chosenCount);
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private static class GroupItemViewHolder {
        View mView;
        TextView mTvGroupTitle;
        TextView mTvGroupTitleItemCount;
        GroupSelectBox mSelectBox;

        GroupItemViewHolder(View convertView) {
            mView = convertView;
            mSelectBox = (GroupSelectBox) convertView.findViewById(R.id.activity_applock_group_selectbox);
            mTvGroupTitle = (TextView) convertView.findViewById(R.id.activity_applock_group_title);
            mTvGroupTitleItemCount = (TextView) convertView.findViewById(R.id.activity_applock_group_title_item_count);
        }
    }

    private static class ChildItemViewHolder {
        ImageView mIvAppIcon;
        TextView mTvAppName;
        TextView mTvAppSize;
        ItemCheckBox mItemCheckBox;

        ChildItemViewHolder(View convertView) {
            mIvAppIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
            mTvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
            mTvAppSize = (TextView) convertView.findViewById(R.id.tv_app_size);
            mItemCheckBox = (ItemCheckBox) convertView.findViewById(R.id.icb_child_check);
        }
    }
}
