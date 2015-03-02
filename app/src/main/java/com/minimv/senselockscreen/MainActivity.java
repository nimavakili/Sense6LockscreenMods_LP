package com.minimv.senselockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends Activity {

	private static Context mContext;
	private static PackageManager pm;
	private static CharSequence[] appsList;
	private static CharSequence[] appsPackage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		pm = getPackageManager();
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment())
					.commit();
		}
	}

	public static class SettingsFragment extends PreferenceFragment {

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			PreferenceManager prefManager = getPreferenceManager();
			prefManager.setSharedPreferencesMode(MODE_WORLD_READABLE);
	        addPreferencesFromResource(R.xml.prefs);
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
	        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
	        	SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sprefs, String key) {
						updatePreferences(sprefs);
						if (key.startsWith("shortcut")) {
							sprefs.edit().putBoolean("shortcutsUpdated", true).commit();
						}
					}
			};
			prefs.registerOnSharedPreferenceChangeListener(spChanged);
			updatePreferences(prefs);

			List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
			
			TreeMap<String,String> appsMap = new TreeMap<String,String>(new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					return lhs.compareTo(rhs);
				}
			});
			for (ApplicationInfo packageInfo : packages) {
			    String pn = packageInfo.packageName;
			    if (pm.getLaunchIntentForPackage(pn) != null) {
			    	appsMap.put((String)pm.getApplicationLabel(packageInfo), pn);
			    }
			}
			appsList = new CharSequence[appsMap.size()+2];
			appsPackage = new CharSequence[appsMap.size()+2];
			appsList[0] = "[DEFAULT]";
			appsPackage[0] = "default";
			appsList[1] = "[HIDE]";
			appsPackage[1] = "hide";
			for (int i = 0; i < appsMap.size(); i++) {
		    	appsPackage[i+2] = (String)appsMap.values().toArray()[i];
		    	appsList[i+2] = (String)appsMap.keySet().toArray()[i];
			}
			ListPreference shortcut0 = (ListPreference) findPreference("shortcut0");
			shortcut0.setEntries(appsList);
			shortcut0.setEntryValues(appsPackage);
			ListPreference shortcut1 = (ListPreference) findPreference("shortcut1");
			shortcut1.setEntries(appsList);
			shortcut1.setEntryValues(appsPackage);
			ListPreference shortcut2 = (ListPreference) findPreference("shortcut2");
			shortcut2.setEntries(appsList);
			shortcut2.setEntryValues(appsPackage);
			ListPreference shortcut3 = (ListPreference) findPreference("shortcut3");
			shortcut3.setEntries(appsList);
			shortcut3.setEntryValues(appsPackage);
		}
		
		private void updatePreferences(SharedPreferences sprefs) {
	        //SharedPreferences.Editor editor = sprefs.edit();
			/*if (sprefs.getBoolean("panelAlignBottom", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("hideCarrier");
				preference.setChecked(true);
				editor.putBoolean("hideCarrier", true);
				preference = (CheckBoxPreference) findPreference("largeWidget");
				preference.setEnabled(true);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("largeWidget");
				preference.setEnabled(false);
				preference.setChecked(false);
				editor.putBoolean("largeWidget", false);
			}
			if (sprefs.getBoolean("hidePanel", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHidePanel");
				preference.setChecked(false);
				preference.setEnabled(false);
				editor.putBoolean("nukeHidePanel", false);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHidePanel");
				preference.setEnabled(true);
			}
			if (sprefs.getBoolean("nukeHidePanel", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHorizontalArrows");
				preference.setChecked(true);
				preference.setEnabled(false);
				editor.putBoolean("nukeHorizontalArrows", true);
			}
			else {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("nukeHorizontalArrows");
				preference.setEnabled(true);
			}
			if (!sprefs.getBoolean("disablePatternScroll", false)) {
				CheckBoxPreference preference = (CheckBoxPreference) findPreference("improvePattern");
				preference.setChecked(false);
				editor.putBoolean("improvePattern", false);
			}
			editor.commit();*/
		}
	}
}