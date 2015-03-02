package com.minimv.senselockscreenlp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class XposedHooks implements IXposedHookLoadPackage {

	private XSharedPreferences prefs;
	private boolean hideCarrier, panelAlignBottom, nukeHidePanel, hidePanel, forceDoubleTap; //,nukeHorizontalArrows, hideWidgetFrame, maximizeWidget, disablePatternScroll, improvePattern, improveUnlock;
	private String carrierText, hintText, unlockSensitivity; //, movePattern, bgDimming, defaultWidget
	private String preShortcut0 = "";
	private String preShortcut1 = "";
	private String preShortcut2 = "";
	private String preShortcut3 = "";
	
	public XposedHooks() {
        prefs = new XSharedPreferences("com.minimv.senselockscreenlp");
        prefs.makeWorldReadable();
	}
	
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.htc.lockscreen"))
        	return;

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.OperatorView", lpparam.classLoader, "updateOperatorName", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hideCarrier = prefs.getBoolean("hideCarrier", false);
        			panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
        			carrierText = prefs.getString("carrierText", "");
        			TextView carrier = (TextView) param.thisObject;
        			if (hideCarrier || panelAlignBottom) {
        				carrier.setVisibility(View.GONE);
        			}
        			else if (!carrierText.equals("")) {
                        carrier.setVisibility(View.VISIBLE);
        				if (carrierText.contains("<") && carrierText.contains(">"))
        					carrier.setText(Html.fromHtml(carrierText));
        				else
        					carrier.setText(carrierText);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        /*try { // Pattern hint
            findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardMessageArea", lpparam.classLoader, "getCurrentHintMessage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.reload();
                    XposedBridge.log(param.getResult());
                }
            });
        }
        catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log(e);
        }

        try { // Pattern message
            findAndHookMethod("com.htc.lockscreen.keyguard.KeyguardMessageArea", lpparam.classLoader, "getCurrentTitleMessage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.reload();
                    XposedBridge.log(param.getResult());
                }
            });
        }
        catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log(e);
        }*/

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "adjustPosition", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
        			View panel = (View) param.thisObject;
                    FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams) panel.getLayoutParams();
                    if (panel.getTag() == null) {
                        panel.setTag(localLayoutParams.bottomMargin);
                    }
                    if (panelAlignBottom) {
                        localLayoutParams.bottomMargin = ((Integer) panel.getTag()) - 81;
                        panel.setLayoutParams(localLayoutParams);
                    }
                    else {
                        localLayoutParams.bottomMargin = (Integer) panel.getTag();
                        panel.setLayoutParams(localLayoutParams);
                    }
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "updateShortcutVisible", boolean.class, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hidePanel = prefs.getBoolean("hidePanel", false);
        			nukeHidePanel = prefs.getBoolean("nukeHidePanel", false);
        			hintText = prefs.getString("hintText", "");
        			RelativeLayout panel = (RelativeLayout) param.thisObject;
        			boolean visibility = (Boolean) param.args[0];
        			int count = panel.getChildCount();
        			final TextView mHint = (TextView) ((FrameLayout) panel.getChildAt(count-2)).getChildAt(0);
                    if (mHint.getTag() == null) {
                        mHint.setTag(false);
                        mHint.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (!s.toString().contains(hintText)) {
                                    s.append("\n" + hintText);
                                }
                            }
                        });
                    }
                    if (visibility && !hintText.equals("")) {
                        mHint.setVisibility(View.VISIBLE);
                        mHint.setAlpha(1.0f);
                        if (!mHint.getText().toString().contains(hintText)) {
                            mHint.setText(hintText);
                        }
                    }
                    else {
                        mHint.setAlpha(0.0f);
                    }
        			if (hidePanel) {
        				param.args[0] = false;
        			}
        			else if (nukeHidePanel) {
        				param.setResult(null);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	Class<?> ShortcutSphere = findClass("com.htc.lockscreen.ui.footer.ShortcutSphere", lpparam.classLoader);
        	findAndHookMethod("com.htc.lockscreen.ui.footer.ButtonFooter", lpparam.classLoader, "onBeginDrag", ShortcutSphere, new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        			prefs.reload();
        			hintText = prefs.getString("hintText", "");
        			if (!hintText.equals("")) {
        				param.setResult(null);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.keyguard.HtcKeyguardViewStateManager", lpparam.classLoader, "onScreenRestart", new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(MethodHookParam param) {
        			prefs.reload();
        			hidePanel = prefs.getBoolean("hidePanel", false);
                    hintText = prefs.getString("hintText", "");
                    panelAlignBottom = prefs.getBoolean("panelAlignBottom", false);
                    carrierText = prefs.getString("carrierText", "");
                    Object mFooter = getObjectField(param.thisObject, "mFooter");
        			if (!hintText.equals("")) {
                        callMethod(mFooter, "updateShortcutVisible", true);
                    }
                    if (!carrierText.equals("")) {
                        callMethod(param.thisObject, "updateOperatorName");
                    }
                    callMethod(mFooter, "adjustPosition");
                    if (!hidePanel) {
                        if (shortcutsUpdated(true)) {
                            callMethod(param.thisObject, "onShortcutUpdate");
                        }
                    }
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.drag.SpeedRecorder", lpparam.classLoader, "getUnlockDistance", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			unlockSensitivity = prefs.getString("unlockSensitivity", "default");
        			// stock value: 200
        			if (unlockSensitivity.equals("more")) {
        				param.setResult(100);
        			}
        			else if (unlockSensitivity.equals("less")) {
        				param.setResult(300);
        			}
        		}
        		/*@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			XposedBridge.log("getUnlockDistance: " + param.getResult());
        		}*/
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.ctrl.EasyAccessCtrl", lpparam.classLoader, "isSupportDoubleTap", new XC_MethodHook() {
        		@Override
        		protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        			prefs.reload();
        			forceDoubleTap = prefs.getBoolean("forceDoubleTap", false);
        			if (forceDoubleTap) {
        				param.setResult(true);
        			}
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }

        try {
        	findAndHookMethod("com.htc.lockscreen.setting.SettingDB", lpparam.classLoader, "getShortcutInfos", Context.class, new XC_MethodHook() {
        		@Override
        		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
        			//XposedBridge.log("here"); // getting here on lollipop
        			//0: mId
        			//1: mTitle
        			//2: mResPackageName
        			//3: mIntent
        			//4: mIntentRes
        			//5: mIconRes
        			//6: mIcon
        			//7: mItemType
        			//8: mIconType
        			//9: mCellX
        			prefs.reload();
                	Class<?> ShortcutInfo = findClass("com.htc.lockscreen.setting.ShortcutInfo", lpparam.classLoader);
                	Constructor<?> siC = findConstructorExact(ShortcutInfo, Context.class);
                	ArrayList<Object> siArrayList = new ArrayList<Object>();
                	for (int i = 0; i < 4; i++) {
    					String shortcut = prefs.getString("shortcut" + i, "default");
                		if (shortcut.equals("default")) {
                			ArrayList<?> mShortcutList = (ArrayList<?>) param.getResult();
                			for (int j = 0; j < mShortcutList.size(); j++) {
                				Object si = mShortcutList.get(i);
                            	int cell = (Integer) callMethod(si, "getcell");
                            	if (cell == i) {
                                	siArrayList.add(ShortcutInfo.cast(si));
                            	}
                			}
                		}
                		else if (!shortcut.equals("hide")) {
	    					Context mContext = (Context) param.args[0];
	    					PackageManager pm = mContext.getPackageManager();
	    					Intent localIntent = pm.getLaunchIntentForPackage(shortcut);
	    					ApplicationInfo ai = pm.getApplicationInfo(shortcut, PackageManager.GET_META_DATA);
	    					String l = (String) pm.getApplicationLabel(ai);
                        	Object si = siC.newInstance(mContext);
                        	callMethod(si, "update", ai.uid, l, null, localIntent, null, null, null, 0, 0, i);
                        	callMethod(si, "setCount", 0);
                        	siArrayList.add(ShortcutInfo.cast(si));
                		}
                	}
                	param.setResult(siArrayList);
        		}
        	});
        }
        catch (XposedHelpers.ClassNotFoundError e) {
        	XposedBridge.log(e);
        }
	}
	
	private boolean shortcutsUpdated(boolean reset) {
		prefs.reload();
		String shortcut0 = prefs.getString("shortcut0", "default");
		String shortcut1 = prefs.getString("shortcut1", "default");
		String shortcut2 = prefs.getString("shortcut2", "default");
		String shortcut3 = prefs.getString("shortcut3", "default");
		if (!shortcut0.equals(preShortcut0) || !shortcut1.equals(preShortcut1) || !shortcut2.equals(preShortcut2) || !shortcut3.equals(preShortcut3)) {
			if (reset) {
				preShortcut0 = shortcut0;
				preShortcut1 = shortcut1;
				preShortcut2 = shortcut2;
				preShortcut3 = shortcut3;
			}
			return true;
		}
		return false;
	}
}