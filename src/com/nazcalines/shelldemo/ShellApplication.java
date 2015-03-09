package com.nazcalines.shelldemo;

import android.app.Application;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2015/3/8.
 */
public class ShellApplication extends Application {
    
	public static final String TAG = "ShellApplication";
    
	File mDexInternalStoragePath;

    @Override
    protected  void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        try {
        	getAPKFromDir();   
        	File optDir = getDir("outdex", Context.MODE_PRIVATE);
        	optDir.mkdir();
        
        	WeakReference wr = getLoadedApk();
        	//ClassLoader parentCl;
			//Class c = wr.get().getClass();
	        //Field mClassLoader = c.getDeclaredField("mClassLoader");
	        //mClassLoader.setAccessible(true);
	        //parentCl = (ClassLoader)mClassLoader.get(wr.get());
	        Log.v(TAG, "dexInternal = "+mDexInternalStoragePath.getAbsolutePath());
	        DexClassLoader dcl = new DexClassLoader(mDexInternalStoragePath.getAbsolutePath(),
	                optDir.getAbsolutePath(),
	                null, getClassLoader()
	                );
	        //replace mClassLoader
	        setField("android.app.LoadedApk",
	    			"mClassLoader", wr.get(), dcl);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * the application classloader is 
     * android.app.LoadedApk.mClassLoader
     * @param cl    dexClassLoader
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException 
     */
    private WeakReference getLoadedApk() throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException, NoSuchMethodException,
    ClassNotFoundException, NoSuchFieldException{
		//get currentActivityThread
    	Object currentActivityThread = invokeStaticMethod("android.app.ActivityThread",
    			"currentActivityThread",
    			new Class[0], new Object[0]);
    	//get mPackages
    	Field field = currentActivityThread.getClass().getDeclaredField("mPackages");
    	field.setAccessible(true);
    	Object mPackages = field.get(currentActivityThread);
    	//get LoadedApk
    	WeakReference wr = ((ArrayMap<String, WeakReference>)mPackages)
    			.get(getPackageName());
    	return wr;  	
    			
	}

	private void getAPKFromDir() throws IOException {
        int resOriginAPK = getResources().getIdentifier("originAPK", "string" ,
                getPackageName());
        int resLoadedAPK = getResources().getIdentifier("dynamicLoadedAPKPath", 
        		"string", getPackageName());
        String originAPKPath = getResources().getString(resOriginAPK);
        String dynamicLoadedAPKPath = getResources().getString(resLoadedAPK);
        Log.v(TAG, "ori="+originAPKPath+" and dyn="+dynamicLoadedAPKPath);
        mDexInternalStoragePath = getDir("dex" ,Context.MODE_PRIVATE);
        mDexInternalStoragePath.mkdir();
        File dex = new File(mDexInternalStoragePath, dynamicLoadedAPKPath);
        InputStream inputStream = getAssets().open(originAPKPath);
        FileOutputStream fileOutputStream = new FileOutputStream(dex);
        byte[] buffer = new byte[256];
        int len;
        while((len = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, len);
        }
        mDexInternalStoragePath = dex;
        inputStream.close();
        fileOutputStream.close();
    }

    /*private File writeOrigDynApkToInternalDexDir() throws IOException {
    	Log.i(TAG, "ShellApp Application::writeOrigDynApkToInternalDexDir");
//        String dynApkPath = getString(apkshell.apk.internal.R.string.shell_dyn_apk_path);
//        String origDynApkPath = getString(apkshell.apk.internal.R.string.shell_orig_dyn_apk_path);
    	
    	Log.i(TAG, "begin get source id");
        int resID = getResources().getIdentifier("dynamicLoadedAPKPath", "string", getPackageName()); 
        Log.i(TAG, "shell_dyn_apk_path:"+resID+" PACKAGENAME: "+getPackageName());
        String dynApkPath = getResources().getString(resID);
        Log.i(TAG, "shell_dyn_apk_path:"+resID+"--"+dynApkPath);
        
        int resID2 = getResources().getIdentifier("originAPK", "string", getPackageName()); 
        String origDynApkPath = getResources().getString(resID2);
        Log.i(TAG, "shell_dyn_apk_path:"+resID2+"--"+origDynApkPath);

        File dex = getDir("dex", Context.MODE_PRIVATE);
        
        dex.mkdir();
        File f = new File(dex, dynApkPath);
        InputStream fis = getAssets().open(origDynApkPath);
        FileOutputStream fos = new FileOutputStream(f);
        byte[] buffer = new byte[0xFF];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fis.close();
        fos.close();
        return f;
    }
    */
	
	public static Object invokeStaticMethod(
			String className, String methodName,
			Class[] parameterTypes, Object[] args)
					throws NoSuchMethodException,
					ClassNotFoundException, 
					IllegalAccessException, 
					IllegalArgumentException, 
					InvocationTargetException{
			Class cls = Class.forName(className);
			Method method = cls.getDeclaredMethod(methodName,
					parameterTypes);
			return method.invoke(method, parameterTypes);
	}

	/**
	 * 
	 * @param className
	 * @param fieldName
	 * @param object
	 * @param value
	 * @throws ClassNotFoundException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static void setField(String className, 
			String fieldName, Object object, Object value) 
					throws ClassNotFoundException, 
					NoSuchFieldException, 
					IllegalAccessException, 
					IllegalArgumentException {
		Class cls = Class.forName(className);
		Field field = cls.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);	
	}
	
    @Override
    public void onCreate() {
    	//Log.v(TAG, "=====Appliation create.=====");
    	super.onCreate();
    }
       
}
