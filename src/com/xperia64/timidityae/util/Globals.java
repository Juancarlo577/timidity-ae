/*******************************************************************************
 * Copyright (C) 2014 xperia64 <xperiancedapps@gmail.com>
 * <p>
 * Copyright (C) 1999-2008 Masanao Izumo <iz@onicos.co.jp>
 * <p>
 * Copyright (C) 1995 Tuukka Toivonen <tt@cgs.fi>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.xperia64.timidityae.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.TextView;

import com.xperia64.timidityae.JNIHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
public class Globals {
	public static boolean libLoaded = false;
	public static ArrayList<String> plist; // Because arguments don't like big things.
	public static ArrayList<String> tmpplist; // I'm lazy.
	public static Bitmap currArt;
	public static boolean hardStop = false;

	public static final String autoSoundfontHeader = "#<--------Config Generated By Timidity AE (DO NOT MODIFY)-------->";

	public static String repeatedSeparatorString = String.format("[%c]+", File.separatorChar);
	public static String parentString = ".." + File.separator;
	public static char[] invalidChars = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};


	// Fragment Keys
//public static String currFoldKey="CURRENT_FOLDER";
//public static String currPlistDirectory="CURRENT_PLIST_DIR";
	public static boolean shouldRestore = false;
	// Resampling Algorithms
	public static String[] sampls = {"Cubic Spline", "Lagrange", "Gaussian", "Newton", "Linear", "None"};
// File filters


	public static InputFilter fileNameInputFilter = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {
				for (int o = 0; o < Globals.invalidChars.length; o++) {
					if (source.charAt(i) == Globals.invalidChars[o]) {
						return "";
					}
				}
			}
			return null;
		}
	};

	public static String getFileExtension(File f) {
		int dotPosition = f.getName().lastIndexOf(".");
		if (dotPosition != -1) {
			return (f.getName().substring(dotPosition)).toLowerCase(Locale.US);
		}
		return null;
	}

	public static String getFileExtension(String s) {
		int dotPosition = s.lastIndexOf(".");
		if (dotPosition != -1) {
			return (s.substring(dotPosition)).toLowerCase(Locale.US);
		}
		return null;
	}

	public static boolean hasSupportedExtension(File f) {
		String ext = getFileExtension(f);
		return ext != null && getSupportedExtensions().contains("*" + ext + "*");
	}

	public static boolean hasSupportedExtension(String s) {
		String ext = getFileExtension(s);
		return ext != null && getSupportedExtensions().contains("*" + ext + "*");
	}

	// Requires TiMidity to be loaded to play these files:
	private static final String TIMIDITY_FILES = "*.mid*.smf*.kar*.mod*.xm*.s3m*.it*.669*.amf*.dsm*.far*.gdm*.imf*.med*.mtm*.stm*.stx*.ult*.uni*";
	private static final String MEDIA_FILES = "*.mp3*.m4a*.wav*.ogg*.flac*.mid*.smf*.kar*";
	private static final String VIDEO_FILES = "*.mp4*.3gp*";

	public static String getSupportedExtensions() {
		StringBuilder supportedExtensions = new StringBuilder(MEDIA_FILES);
		if (SettingsStorage.showVideos) {
			supportedExtensions.append(VIDEO_FILES);
		}
		if (!SettingsStorage.onlyNative) {
			supportedExtensions.append(TIMIDITY_FILES);
		}
		return supportedExtensions.toString().replaceAll("[*]+", "*");
	}

	public static ArrayList<String> normalToUuid(ArrayList<String> list) {
		ArrayList<String> uuid = new ArrayList<>();
		for (String xx : list) {
			uuid.add(String.format("%s*%08x", xx, new Random().nextInt()));
		}
		return uuid;
	}

	public static ArrayList<String> uuidToNormal(ArrayList<String> list) {
		ArrayList<String> normal = new ArrayList<>();
		for (String xx : list) {
			normal.add(xx.substring(0, xx.lastIndexOf("*")));
		}
		return normal;
	}

	public static String playlistFiles = "*.tpl*";
	public static String configFiles = "*.tcf*.tzf*";
	public static String fontFiles = "*.sf2*.sfark*.sfark.exe*";
	public static int defaultListColor = -1;


	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static int getBackgroundColor(TextView textView) {
		Drawable drawable = textView.getBackground();
		if (drawable instanceof ColorDrawable) {
			ColorDrawable colorDrawable = (ColorDrawable) drawable;
			if (Build.VERSION.SDK_INT >= 11) {
				return colorDrawable.getColor();
			}
			try {
				Field field = colorDrawable.getClass().getDeclaredField("mState");
				field.setAccessible(true);
				Object object = field.get(colorDrawable);
				field = object.getClass().getDeclaredField("mUseColor");
				field.setAccessible(true);
				return field.getInt(object);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	@SuppressLint("SdCardPath")
	public static File getExternalCacheDir(Context c) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return c.getExternalCacheDir();
		} else {
			return new File("/sdcard/Android/data/com.xperia64.timidityae/cache/");
		}
	}

	@SuppressLint({"SdCardPath"})
	public static String getLibDir(Context c) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			String s = c.getApplicationInfo().nativeLibraryDir;
			if (!s.endsWith(File.separator)) {
				s += File.separator;
			}
			return s;
		} else {
			return "/data/data/com.xperia64.timidityae/lib/";
		}
	}

	public static int probablyFresh = 0;
	public static final int NOTIFICATION_ID = 13901858;
	public static boolean phoneState = true;
	public static int highlightMe = -1;

	public static boolean isMidi(String songFileName) {
		return !(songFileName.toLowerCase(Locale.US).endsWith(".mp3")
				|| songFileName.toLowerCase(Locale.US).endsWith(".m4a")
				|| songFileName.toLowerCase(Locale.US).endsWith(".wav")
				|| songFileName.toLowerCase(Locale.US).endsWith(".ogg")
				|| songFileName.toLowerCase(Locale.US).endsWith(".flac")
				|| songFileName.toLowerCase(Locale.US).endsWith(".mp4")
				|| songFileName.toLowerCase(Locale.US).endsWith(".3gp")
				|| songFileName.toLowerCase(Locale.US).endsWith(".webm") // MediaPlayer might support webm audio only on some devices but not mine
				|| (SettingsStorage.nativeMidi
				&& (songFileName.toLowerCase(Locale.US).endsWith(".mid")
				|| songFileName.toLowerCase(Locale.US).endsWith(".kar")
				|| songFileName.toLowerCase(Locale.US).endsWith(".smf"))));
	}

	public static int extract8Rock(Context c) {
		InputStream in = null;
		try {
			in = c.getAssets().open("8Rock11e.sfArk");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] needLol = null;
		try {
			new FileOutputStream(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk", true).close();
		} catch (FileNotFoundException e) {
			needLol = DocumentFileUtils.getExternalFilePaths(c, SettingsStorage.dataFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (needLol != null) {
			File f = new File(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk");
			if (f.exists())
				DocumentFileUtils.tryToDeleteFile(c, SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk");
			OutputStream out = null;
			String probablyTheDirectory = needLol[0];
			String probablyTheRoot = needLol[1];
			String needRename;
			String value;
			String value2;
			if (probablyTheDirectory.length() > 1) {
				needRename = SettingsStorage.dataFolder.substring(SettingsStorage.dataFolder.indexOf(probablyTheRoot) + probablyTheRoot.length()) + "/soundfonts/8Rock11e.sf2";
				value = probablyTheDirectory + '/' + "8Rock11e.sfArk";
				value2 = probablyTheDirectory + '/' + "8Rock11e.sf2";
			} else {
				return -9;
			}
			try {
				out = new FileOutputStream(value);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (out == null)
				return -1;
			byte buf[] = new byte[1024];
			int len;
			try {
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			JNIHandler.decompressSFArk(value, "8Rock11e.sf2");
			DocumentFileUtils.renameDocumentFile(c, value2, needRename);
			DocumentFileUtils.tryToDeleteFile(c, value);
		} else {
			File f = new File(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk");
			if (f.exists())
				f.delete();
			OutputStream out = null;
			try {
				out = new FileOutputStream(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (out == null)
				return -1;
			byte buf[] = new byte[1024];
			int len;
			try {
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			JNIHandler.decompressSFArk(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk", "8Rock11e.sf2");
			//System.out.println("decompresed sfark");
			new File(SettingsStorage.dataFolder + "/soundfonts/8Rock11e.sfArk").delete();
		}

		return 777;

	}


}
