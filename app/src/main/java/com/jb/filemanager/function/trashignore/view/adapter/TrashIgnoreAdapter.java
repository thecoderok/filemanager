package com.jb.filemanager.function.trashignore.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jb.filemanager.R;
import com.jb.filemanager.function.scanframe.clean.CleanManager;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreAdBean;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreBean;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreCacheAppBean;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreCachePathBean;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreGroupBean;
import com.jb.filemanager.function.scanframe.clean.ignore.CleanIgnoreResidueBean;
import com.jb.filemanager.function.trash.adapter.AbsAdapter;
import com.jb.filemanager.util.AppUtils;
import com.jb.filemanager.util.imageloader.IconLoader;

import java.util.List;

/**
 * Created by xiaoyu on 2017/2/28 14:03.
 */

public class TrashIgnoreAdapter extends AbsAdapter<CleanIgnoreGroupBean> {
    private CleanManager mCleanManager;
    private Context mContext;

    /**
     * ListView缓存
     *
     * @author chenbenbin
     */
    private class ViewHolder {
        private ImageView mIcon;
        private ImageView mIconSmall;
        private TextView mTitle;
        private TextView mSubTitle;
        private ImageView mRemove;
    }

    public TrashIgnoreAdapter(List<CleanIgnoreGroupBean> groups, Context context) {
        super(groups);
        mCleanManager = CleanManager.getInstance(context);
        mContext = context.getApplicationContext();
    }

    @Override
    public View onGetGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        // 1. 初始化View对象
        ViewHolder holder = null;
        if (convertView != null) {
            holder = (ViewHolder) convertView
                    .getTag(R.layout.activity_clean_ignore_list_group);
        }
        if (holder == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_clean_ignore_list_group, parent, false);
            holder.mIcon = (ImageView) convertView
                    .findViewById(R.id.clean_ignore_list_group_icon);
            holder.mTitle = (TextView) convertView
                    .findViewById(R.id.clean_ignore_list_group_title);
            convertView.setTag(R.layout.activity_clean_ignore_list_group, holder);
        }
        // 2. 初始化界面
        CleanIgnoreGroupBean groupBean = getGroup(groupPosition);
        holder.mIcon.setImageResource(groupBean.getGroupType().getGroupIconId());
        holder.mTitle.setText(groupBean.getGroupType().getNameId());
        return convertView;
    }

    @Override
    public View onGetChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        // 1. 初始化View对象
        ViewHolder holder = null;
        if (convertView != null) {
            holder = (ViewHolder) convertView
                    .getTag(R.layout.activity_clean_ignore_list_item);
        }
        if (holder == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_clean_ignore_list_item, parent, false);
            holder.mIcon = (ImageView) convertView
                    .findViewById(R.id.clean_ignore_list_item_icon);
            holder.mIconSmall = (ImageView) convertView.findViewById(R.id.clean_ignore_list_item_icon_small);
            holder.mTitle = (TextView) convertView
                    .findViewById(R.id.clean_ignore_list_item_title);
            holder.mSubTitle = (TextView) convertView
                    .findViewById(R.id.clean_ignore_list_item_subtitle);
            holder.mRemove = (ImageView) convertView.findViewById(R.id.clean_ignore_list_item_remove);
            convertView.setTag(R.layout.activity_clean_ignore_list_item, holder);
        }
        // 2. 初始化界面
        final CleanIgnoreGroupBean groupBean = getGroup(groupPosition);
        final List<CleanIgnoreBean> children = groupBean.getChildren();
        final CleanIgnoreBean ignoreBean = children.get(childPosition);

        if (ignoreBean instanceof CleanIgnoreCacheAppBean) {
            CleanIgnoreCacheAppBean cacheAppBean = (CleanIgnoreCacheAppBean) ignoreBean;
            IconLoader.getInstance().displayImage(cacheAppBean.getPackageName(), holder.mIcon);
            String appName = AppUtils.getAppName(mContext, cacheAppBean.getPackageName());
            if (!TextUtils.isEmpty(appName) && !appName.equals(cacheAppBean.getPackageName())) {
                holder.mTitle.setText(appName);
            } else {
                holder.mTitle.setText(ignoreBean.getTitle());
            }
            holder.mIconSmall.setVisibility(View.GONE);
            holder.mSubTitle.setVisibility(View.GONE);
        } else if (ignoreBean instanceof CleanIgnoreCachePathBean) {
            CleanIgnoreCachePathBean cachePathBean = (CleanIgnoreCachePathBean) ignoreBean;
            holder.mIcon.setImageResource(R.drawable.subitem_cache);
            IconLoader.getInstance().displayImage(cachePathBean.getPackageName(), holder.mIconSmall);
            holder.mTitle.setText(cachePathBean.getTitle());
            String appName = AppUtils.getAppName(mContext, cachePathBean.getPackageName());
            if (!TextUtils.isEmpty(appName) && !appName.equals(cachePathBean.getPackageName())) {
                holder.mSubTitle.setText(appName);
            } else {
                holder.mSubTitle.setText(cachePathBean.getSubTitle());
            }
            holder.mIconSmall.setVisibility(View.VISIBLE);
            holder.mSubTitle.setVisibility(View.VISIBLE);
        } else if (ignoreBean instanceof CleanIgnoreResidueBean) {
            holder.mSubTitle.setVisibility(View.GONE);
            holder.mIcon.setImageResource(groupBean.getGroupType().getChildIconId());
            holder.mTitle.setText(ignoreBean.getTitle());
            holder.mIconSmall.setVisibility(View.GONE);
        } else if (ignoreBean instanceof CleanIgnoreAdBean) {
            holder.mSubTitle.setVisibility(View.GONE);
            holder.mIcon.setImageResource(groupBean.getGroupType().getChildIconId());
            holder.mTitle.setText(ignoreBean.getTitle());
            holder.mIconSmall.setVisibility(View.GONE);
        }
        holder.mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ignoreBean instanceof CleanIgnoreCacheAppBean) {
                    mCleanManager.removeCacheAppIgnore((CleanIgnoreCacheAppBean) ignoreBean);
                } else if (ignoreBean instanceof CleanIgnoreCachePathBean) {
                    mCleanManager.removeCachePathIgnore((CleanIgnoreCachePathBean) ignoreBean);
                } else if (ignoreBean instanceof CleanIgnoreResidueBean) {
                    mCleanManager.removeResidueIgnore((CleanIgnoreResidueBean) ignoreBean);
                } else if (ignoreBean instanceof CleanIgnoreAdBean) {
                    mCleanManager.removeAdIgnore((CleanIgnoreAdBean) ignoreBean);
                }
                children.remove(ignoreBean);
                if (children.isEmpty()) {
                    removeGroup(groupBean);
                }
                if (mListener != null && mGroups.isEmpty()) {
                    mListener.onRemoveAll();
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private RemoveListener mListener;

    public void setListener(RemoveListener listener) {
        mListener = listener;
    }

    public interface RemoveListener {
        void onRemoveAll();
    }
}