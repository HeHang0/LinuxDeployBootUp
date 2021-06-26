package com.oohoo.bootup;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


/**
 * Created by HeHang.
 */
public class XposedInit implements IXposedHookLoadPackage
{
    final private String OppoLaucherPackage = "com.oppo.launcher";//"com.oohoo.bootup";
    final private String OppoLaucherClass = "com.oppo.launcher.Launcher";//"com.oohoo.bootup.MainActivity";
    final private String LinuxDeployPackage = "ru.meefik.linuxdeploy";
    //要调用另一个APP的activity名字
    final private String LinuxDeployLauncher = "ru.meefik.linuxdeploy.Launcher";
    final private String LinuxDeployClass = "ru.meefik.linuxdeploy.activity.MainActivity";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //找到launcher的包名，hook Application的onCreate方法
        //com.oppo.launcher
        XposedBridge.log("BootUp >> current package:" + loadPackageParam.packageName);
        if(OppoLaucherPackage.equals(loadPackageParam.packageName)){
            findAndHookMethod(OppoLaucherClass, loadPackageParam.classLoader,
                "onCreate", Bundle.class, new XC_MethodHook(){
                    @SuppressLint("SetTextI18n")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("BootUp >> afterHookedMethod");
                        try{

                            Process process = Runtime.getRuntime().exec("su");
                            DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                            os.writeBytes("echo -e `date`\": start before\\n\" >> /sdcard/bootup.txt \n");
                            os.writeBytes("while true; do r=`getprop sys.boot_completed`; [[ \"$r\" -eq \"1\" ]] && break;sleep 1;done \n");
//                            os.writeBytes("input keyevent KEYCODE_POWER \n");
//                            os.writeBytes("sleep 1 \n");
                            os.writeBytes("input keyevent KEYCODE_WAKEUP \n");
                            os.writeBytes("sleep 1 \n");
                            os.writeBytes("input swipe 500 720 500 0 \n");
                            os.writeBytes("sleep 1 \n");
                            os.writeBytes("input tap 100 5 \n");
                            os.writeBytes("echo `date`\": 2sys.boot_completed \" >> /sdcard/bootup.txt \n");
                            os.writeBytes("echo -e `getprop sys.boot_completed`\"\\n\" >> /sdcard/bootup.txt \n");
                            os.writeBytes("echo -e `date`\": start end...\\n\" >> /sdcard/bootup.txt \n");
                            os.writeBytes("/system/bin/screencap -p /sdcard/bootup-screenshot.png \n");
                            os.writeBytes("sleep 10 \n");
                            os.writeBytes("am start -n " + LinuxDeployPackage + "/" + LinuxDeployLauncher + " \n");
                            os.writeBytes("exit \n");
                            os.flush();
//                            process.waitFor();
//                            XposedBridge.log("BootUp >>start package: "+ LinuxDeployPackage + "/" + LinuxDeployLauncher);
//                            Object activityThread = callStaticMethod(findClass(
//                                    "android.app.ActivityThread", null),
//                                    "currentActivityThread");
//                            Context mContext = (Context) callMethod(activityThread,
//                                    "getSystemContext");
//                            //自己应用的隐式启动action
//                            XposedBridge.log("BootUp >> ready start package:");
//                            ComponentName component = new ComponentName(LinuxDeployPackage, LinuxDeployLauncher);
//                            Intent intent = new Intent();
//                            intent.setComponent(component);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            mContext.startActivity(intent);
                        }catch (Exception e) {
                            XposedBridge.log("BootUp >> start package error:" + e.getMessage());
                        }
                    }
                }
            );
        }else if(LinuxDeployPackage.equals(loadPackageParam.packageName)){
            findAndHookMethod(LinuxDeployClass, loadPackageParam.classLoader,
                    "onCreate", Bundle.class, new XC_MethodHook(){
                        @SuppressLint("SetTextI18n")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object activityThread = callStaticMethod(findClass(
                                    "android.app.ActivityThread", null),
                                    "currentActivityThread");
                            Context mContext = (Context) callMethod(activityThread,
                                    "getSystemContext");
                            MultiProcessSharedPreferences.setAuthority("com.oohoo.bootup.provider");
                            SharedPreferences settings = MultiProcessSharedPreferences.getSharedPreferences(mContext, MainActivity.ButtonPositionKey, Context.MODE_PRIVATE);
                            int startXV = settings.getInt("startX", -1);
                            int startYV = settings.getInt("startY", -1);
                            int confirmXV = settings.getInt("confirmX", -1);
                            int confirmYV = settings.getInt("confirmY", -1);
                            XposedBridge.log("BootUp >> value tap " + startXV + " " + startYV + " " + confirmXV + " " + confirmYV);
                            if(startXV >= 0 && startYV >= 0 && confirmXV >= 0 && confirmYV >= 0){
                                Process process = Runtime.getRuntime().exec("su");
                                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                os.writeBytes("sleep 2 \n");
                                os.writeBytes("input tap " + startXV + " " + startYV + " \n");
                                os.writeBytes("sleep 1 \n");
                                os.writeBytes("input tap " + confirmXV + " " + confirmYV + " \n");
                                os.writeBytes("sleep 1 \n");
                                os.writeBytes("input keyevent KEYCODE_POWER \n");
                                os.writeBytes("echo -n 'mtp,adb' > /data/property/persist.sys.usb.config \n");
                                os.writeBytes("exit \n");
                                os.flush();
                            }
                        }
                    }
            );
        }
    }
}