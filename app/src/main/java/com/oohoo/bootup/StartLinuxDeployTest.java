package com.oohoo.bootup;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class StartLinuxDeployTest {
    final String TAG = "UiA2[StartLinuxDeploy]";
    final String LinuxDeployPackage = "ru.meefik.linuxdeploy";
    final String LinuxDeployLauncher = "ru.meefik.linuxdeploy.Launcher";

    @Test
    public void run() throws RemoteException, UiObjectNotFoundException, InterruptedException, IOException {
        // adb shell am instrument --user 0 -w -r -e debug false -e class com.oohoo.bootup.StartLinuxDeployTest#run com.oohoo.bootup.test/androidx.test.runner.AndroidJUnitRunner
        Log.i(TAG, "StartLinuxDeployTest Start");
        Instrumentation mInstrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice mDevice = UiDevice.getInstance(mInstrumentation);
        Context context = mInstrumentation.getContext();
        boolean running = true;
        while (running){
            while (!mDevice.isScreenOn()) {
                mDevice.pressKeyCode(KeyEvent.KEYCODE_WAKEUP);
                Log.i(TAG, "current package in screen check: " + mDevice.getCurrentPackageName());
            }
            boolean isLock;
            do {
                KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                isLock = mKeyguardManager.inKeyguardRestrictedInputMode();
                Log.i(TAG, "current lock: " + isLock);
                if(isLock) {
                    int height = mDevice.getDisplayHeight();
                    int width = mDevice.getDisplayWidth();
                    mDevice.swipe(width/2, (int)(height*0.8), width/2,  (int)(height*0.2), 10);
                    Log.i(TAG, "current package in unlock: " + mDevice.getCurrentPackageName());
                }
                if(isLock) Thread.sleep(100);
            }while (isLock);
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("ps | grep " + LinuxDeployPackage + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            BufferedReader result = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String resultLine = "";
            while ((resultLine = result.readLine()) != null) {
                Log.i(TAG, resultLine);
                if(resultLine.contains(LinuxDeployPackage) && !resultLine.contains("grep")){
                    Log.i(TAG, LinuxDeployPackage + " is running, so exit");
                    running = false;
                    break;
                }
            }
            if(!running) break;
            String currentPkg = mDevice.getCurrentPackageName();
            while (currentPkg == null || !currentPkg.equals(LinuxDeployPackage)){
                Log.i(TAG, "ready to start " + LinuxDeployPackage);
                mDevice.executeShellCommand("am start -n " + LinuxDeployPackage + "/" + LinuxDeployLauncher);//执行一个shell命令，需要5.0以上手机
                mDevice.wait(Until.hasObject(By.pkg(LinuxDeployPackage).depth(0)), 2000);
                Log.i(TAG, "start end " + LinuxDeployPackage);
                currentPkg = mDevice.getCurrentPackageName();
            }
            while (mDevice.getCurrentPackageName().equals(LinuxDeployPackage)){
                mDevice.wait(Until.hasObject(By.text("启动").clickable(true)), 10000);
                Log.i(TAG, "启动按钮出现");
                UiObject uo = mDevice.findObject(new UiSelector().text("启动").clickable(true));
                if(uo.exists() && uo.isClickable()){
                    uo.click();
                }
                mDevice.wait(Until.hasObject(By.text("OK").clickable(true)), 10000);
                Log.i(TAG, "OK按钮出现");
                UiObject uok = mDevice.findObject(new UiSelector().text("OK").clickable(true));
                if(uok.exists() && uok.isClickable()){
                    uok.click();
                    mDevice.pressHome();
                    mDevice.wait(Until.hasObject(By.pkg(mDevice.getLauncherPackageName()).depth(0)), 2000);
                    mDevice.pressKeyCode(KeyEvent.KEYCODE_POWER);
                    running = false;
                }
            }
        }
        Log.i(TAG, "StartLinuxDeployTest End");
    }
}
