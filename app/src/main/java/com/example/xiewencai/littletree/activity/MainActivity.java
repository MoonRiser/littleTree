package com.example.xiewencai.littletree.activity;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.xiewencai.littletree.R;
import com.example.xiewencai.littletree.adapter.TabFragmentAdapter;
import com.example.xiewencai.littletree.db.Note;
import com.example.xiewencai.littletree.fragment.ChooseAreaFragment;
import com.example.xiewencai.littletree.fragment.NoteFragment;
import com.example.xiewencai.littletree.util.ActivityCollector;
import com.example.xiewencai.littletree.util.CommonFab;
import com.example.xiewencai.littletree.util.NoteUtils;
import com.example.xiewencai.littletree.util.NotificationUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements CommonFab {

    public static final String BROADCAST_FLAG = "com.example.xiewencai.material_learning.FORCE_OFFLINE";

    private Toolbar toolbar;
    private View statusView;

    private NoteFragment f3;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = initStatusBar();
        fab = findViewById(R.id.common_fab);


        //初始化tab和ViewPager
        initTabViewPager();


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//设置toolbar
        //设置toolbar上的汉堡菜单
        ActionBar actionBar = getSupportActionBar();

    }
//onCreate结束
    /*
     */


    //为toolbar填充菜单布局
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    //给toolbar的选项设置点击事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.backup:
                NoteUtils.uploadNoteData(this);
                //  Toast.makeText(this, "well, you clicked Backup", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                showToast("Software Copyright Reserved ");

                break;

            case R.id.settings: //
                Intent intent1 = new Intent(this, SettingActivity.class);
                startActivity(intent1);
                break;
            case R.id.deleteAll:
                deleteAllNote();
                break;

            default:
                break;
        }
        return true;
    }



    //按返回键时执行该方法弹出对话框 是否退出并 杀掉进程
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Sure to exit ?");
        dialog.setMessage("Make sure or just cancel");
        dialog.setCancelable(false);//用户不能拒绝应答此对话框，必须做出选择
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                ActivityCollector.finishAll();
                // android.os.Process.killProcess(android.os.Process.myPid());//杀掉当前进程
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();//取消对话框
            }
        });
        dialog.show();


    }

    //初始化Tab和ViewPager的绑定
    private void initTabViewPager() {
        final List<String> tabList = new ArrayList<>();
        final int[] materalColor = getResources().getIntArray(R.array.material_color);

        tabList.add("笔记");
        tabList.add("天气");

        final TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //让tab和toolbar实现颜色渐变；
                ArgbEvaluator evaluator = new ArgbEvaluator();
                int evaluate = (Integer) evaluator.evaluate(positionOffset, materalColor[position], materalColor[(position + 1) % tabList.size()]);
                tabLayout.setBackgroundColor(evaluate);
                toolbar.setBackgroundColor(evaluate);
                statusView.setBackgroundColor(evaluate);
                getWindow().setNavigationBarColor(evaluate);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //设置tab模式，MODE_FIXED是固定的，MODE_SCROLLABLE可超出屏幕范围滚动的
        //  tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        List<Fragment> fragmentList = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();

        //  Fragment f1 = new HorosFragment();
        Fragment f2 = new ChooseAreaFragment();
        f3 = new NoteFragment();
        //      ((HorosFragment)f1).setCommonFab(this);
        Log.w("MainActivity这边先执行", "test" + (this.getCommonFab() == null));
        ((ChooseAreaFragment) f2).setCommonFab(this);
        f3.setCommonFab(this);
        //     fragmentList.add(f1);
        fragmentList.add(f3);
        fragmentList.add(f2);
        //     }else {
        //   fragmentList=fragmentManager.getFragments();
        //      }


        TabFragmentAdapter fragmentAdapter = new TabFragmentAdapter(fragmentManager, fragmentList, tabList);
        viewPager.setAdapter(fragmentAdapter);//给ViewPager设置适配器
        tabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
        //tabLayout.setTabsFromPagerAdapter(fragmentAdapter);//给Tabs设置适配器

    }

    //初始化状态栏
    public View initStatusBar() {
        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        Window window = getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        int finalColor = Color.argb(50, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
        window.setStatusBarColor(finalColor);
        View statusView = createStatusBarView(this, ContextCompat.getColor(this, R.color.colorPrimary));
        frameLayout.addView(statusView);
        decorView.setSystemUiVisibility(option);
        return statusView;
    }

    private static View createStatusBarView(Activity activity, int color) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        params.gravity = Gravity.TOP;
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(color);
        return statusBarView;
    }


    private void deleteAllNote() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除确认");
        builder.setCancelable(false);
        builder.setMessage("将删除所有笔记数据，清除数据库\n数据有可能完全丢失\n确定进行吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataSupport.deleteAll(Note.class);
                f3.deleteAllLocal();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public FloatingActionButton getFab(){
        return  fab;
    }



    @Override
    public FloatingActionButton getCommonFab() {
        if (fab != null) {
            return fab;
        }
        return null;
    }
}










