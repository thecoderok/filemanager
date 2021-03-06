package com.jb.filemanager.function.applock.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.jb.filemanager.R;
import com.jb.filemanager.function.applock.fragment.IntruderHoriGalleryFragment;

/**
 * Created by nieyh on 2017/1/7.
 */

public class IntruderHoriGalleryActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_main);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.add(R.id.activity_intruder_main_content, new IntruderHoriGalleryFragment());
        tx.commit();
    }

    public static void gotoIntruderHoriGallery(Context context) {
        Intent i = new Intent(context, IntruderHoriGalleryActivity.class);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }

}
