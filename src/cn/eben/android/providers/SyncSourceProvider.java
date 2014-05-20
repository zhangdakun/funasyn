package cn.eben.android.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;


import com.funambol.util.Log;

public class SyncSourceProvider extends ContentProvider {
    private static final String TAG_LOG = "SyncSourceProvider";

    public static final String AUTHORITY = "cn.eben.provider.syncSource";
    public static final String AUTHORITY_URI = "content://cn.eben.provider.syncSource/";
    
    private DatabaseHelper mDatabaseHelper = null;
    public static final String sdCardRoot = Environment.getExternalStorageDirectory().toString();

    public static final String DB_NAME = sdCardRoot + "/AppFiles/ebenbackup/backup.db";
//    public static final String DB_NAME = sdCardRoot + "/syncsource.db";
    private SQLiteDatabase dbStore = null;
    public static final String PRIFEX_DOWN = "down_";
    public static final String PRIFEX_MARK = "appsmark_";
    public static final String PRIFEX_STATUS = "appstatus_";
    
    public static final String PRIFEX_NEW = "new_";
    public static final String PRIFEX_UPDATE = "update_";
    public static final String PRIFEX_DELETE = "delete_";
    public static final String PRIFEX_RENAME = "rename_";
    public static final String PRIFEX_ERROR = "error_";
    
    public static final String PRIFEX_LUID = "appluid_";

    public final static String KEY_COLUMN_NEW   = "_keynew";
    public final static String KEY_COLUMN_OLD = "_keyold";
    
    public final static String KEY_COLUMN_NAME   = "_key";
    public final static String VALUE_COLUMN_NAME = "_value";

    public final static String PATH_COLUMN_NAME   = "_path";
    public final static String PARENT_COLUMN_NAME = "_parent";
    
    public final static String SYNC_COLUMN_NAME = "_issync";
    
    public static final String PNT = "%"; // FOR % %
    public static final String PNTTOKEN = "<PNT>"; // FOR %
    
    public static final String NAME = "name";
    public static final String ETAG = "etag";
    public static final String URI = "uri";
    public static final String MODIFY = "modified";
    public static final String SIZE = "size";
    public static final String LUID = "luid";// A or U
    public static final String PARENT = "parent";    
    public static final String STATE = "state";// A or U
    public static final String DENOTE = "denote";
    
    private static final UriMatcher mUriMatcher;
    private final int DBVERSION = 4;
    
    public static final String  source = "ebackup";
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		int incoming = 0;
	
//        for(String source:Constants.itemKey) {
        	incoming ++;
        	mUriMatcher.addURI(AUTHORITY, source, incoming);
//        }
        
    }
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (dbStore == null || !dbStore.isOpen()) {
			open();
		}
//		if (-1 == mUriMatcher.match(uri)) {
//			return 0;
//		}
		String table = uri.getLastPathSegment();
        Log.debug(TAG_LOG, "table : "+table+", selection, "+selection);
        return dbStore.delete(table, selection, selectionArgs);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		if (dbStore == null || !dbStore.isOpen()) {
			open();
		}
//		if (-1 == mUriMatcher.match(uri)) {
//			return null;
//		}
		String table = uri.getLastPathSegment();
        long rowId = dbStore.insert(table, null, values);
//        if(-1 == rowId) {
//        	return null;
//        }
        return ContentUris.withAppendedId(uri, rowId);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		
		mDatabaseHelper = new DatabaseHelper(getContext(),DB_NAME,null,DBVERSION);
//		open();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.debug(TAG_LOG, "query : "+uri.getLastPathSegment());
		if (dbStore == null || !dbStore.isOpen()) {
			open();
		}
//		if (-1 == mUriMatcher.match(uri)) {
//			return null;
//		}

		return dbStore.query(uri.getLastPathSegment(), projection,
				selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        Log.debug(TAG_LOG, "update");

		if (dbStore == null || !dbStore.isOpen()) {
			open();
		}
//		if (-1 == mUriMatcher.match(uri)) {
//			return 0;
//		}
		String table = uri.getLastPathSegment();
        return dbStore.update(table, values, selection, selectionArgs);

	}
	
    protected static String getDownCreateSQLCommand(String source) {
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_DOWN+source + " ("
               + NAME + " varchar[50] PRIMARY KEY,"+ETAG+ " varchar[50] ,"+MODIFY+" varchar[50] ,"
               + SIZE +" varchar[50] ,"+ URI + " varchar[50] ,"+LUID+" varchar[50] ,"+PARENT+" varchar[50]);";
    }
    protected String getDownCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_DOWN+source + " ON "
               + PRIFEX_DOWN+source + " (" + NAME + ");";
    }
    
    protected String getStatusCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getStatusCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_STATUS+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }
    protected String getStatusCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_STATUS+source + " ON "
               + PRIFEX_STATUS+source + " (" + KEY_COLUMN_NAME + ");";
    } 
    
//    protected String getMarkCreateSQLCommand(String source) {
//    	Log.debug(TAG_LOG, "getMarkCreateSQLCommand");
//        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_MARK+source + " ("
//               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
//               + VALUE_COLUMN_NAME + " varchar[50]);";
//    }
//    protected String getMarkCreateIndexSQLCommand(String source) {
//        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_MARK+source + " ON "
//               + PRIFEX_MARK+source + " (" + KEY_COLUMN_NAME + ");";
//    }  
 
    protected String getNewCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getNewCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_NEW+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }
    protected String getNewCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_NEW+source + " ON "
               + PRIFEX_NEW+source + " (" + KEY_COLUMN_NAME + ");";
    } 
    
    protected String getUpdateCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getUpdateCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_UPDATE+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }
    protected String getUpdateCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_UPDATE+source + " ON "
               + PRIFEX_UPDATE+source + " (" + KEY_COLUMN_NAME + ");";
    } 
    
    protected String getDeleteCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getDeleteCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_DELETE+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }
    protected String getDeleteCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_DELETE+source + " ON "
               + PRIFEX_DELETE+source + " (" + KEY_COLUMN_NAME + ");";
    }    
 
    protected String getRenameCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getRenameCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_RENAME+source + " ("
               + KEY_COLUMN_NEW + " varchar[50] PRIMARY KEY,"
               + KEY_COLUMN_OLD + " varchar[50]);";
    }
    protected String getRenameCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_RENAME+source + " ON "
               + PRIFEX_RENAME+source + " (" + KEY_COLUMN_NEW + ");";
    } 
    
    protected String getErrorCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getErrorCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_ERROR+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }
    protected String getErrorCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_ERROR+source + " ON "
               + PRIFEX_ERROR+source + " (" + VALUE_COLUMN_NAME + ");";
    }   

    protected String getLuidCreateSQLCommand(String source) {
    	Log.debug(TAG_LOG, "getRenameCreateSQLCommand");
        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_LUID+source + " ("
               + KEY_COLUMN_NAME + " varchar[50] PRIMARY KEY,"+SYNC_COLUMN_NAME+ " varchar[50],"
               +PATH_COLUMN_NAME+ " varchar[50]  UNIQUE ,"+PARENT_COLUMN_NAME+" varchar[50] ,"
               +  VALUE_COLUMN_NAME + " varchar[50]);";
        
//        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_DOWN+source + " ("
//        + NAME + " varchar[50] PRIMARY KEY,"+ETAG+ " varchar[50] ,"+MODIFY+" varchar[50] ,"
//        + SIZE +" varchar[50] ,"+ URI + " varchar[50] ,"+STATE+" varchar[50] ,"+DENOTE+" varchar[50]);";
    }
    protected String getLuidCreateIndexSQLCommand(String source) {
        return "CREATE INDEX IF NOT EXISTS " + "index_"+PRIFEX_LUID+source + " ON "
               + PRIFEX_LUID+source + " (" + KEY_COLUMN_NAME + ");";
        
//        return "CREATE TABLE IF NOT EXISTS " + PRIFEX_DOWN+source + " ("
//        + NAME + " varchar[50] PRIMARY KEY,"+ETAG+ " varchar[50] ,"+MODIFY+" varchar[50] ,"
//        + SIZE +" varchar[50] ,"+ URI + " varchar[50] ,"+STATE+" varchar[50] ,"+DENOTE+" varchar[50]);";
        
    } 
    
    private void open() {
        dbStore = mDatabaseHelper.getWritableDatabase();
        Log.debug(TAG_LOG, "open");

//        for(String source:Constants.itemKey) {
	        dbStore.execSQL(getDownCreateSQLCommand(source));
	        dbStore.execSQL(getDownCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getStatusCreateSQLCommand(source));
	        dbStore.execSQL(getStatusCreateIndexSQLCommand(source));
	        
//	        dbStore.execSQL(getMarkCreateSQLCommand(source));
//	        dbStore.execSQL(getMarkCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getNewCreateSQLCommand(source));
	        dbStore.execSQL(getNewCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getUpdateCreateSQLCommand(source));
	        dbStore.execSQL(getUpdateCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getDeleteCreateSQLCommand(source));
	        dbStore.execSQL(getDeleteCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getRenameCreateSQLCommand(source));
	        dbStore.execSQL(getRenameCreateIndexSQLCommand(source));
	        
	        dbStore.execSQL(getLuidCreateSQLCommand(source));
	        dbStore.execSQL(getLuidCreateIndexSQLCommand(source));	        

//	        dbStore.execSQL(getErrorCreateSQLCommand(source));
//	        dbStore.execSQL(getErrorCreateIndexSQLCommand(source));	
	        
//        }
		
        
    }
    
    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            Log.error(TAG_LOG, "onUpgrade , oldVserion : "+oldVersion+", newVersion: "+newVersion);
//        	if(1 == oldVersion) 
        	{
//        		for(String source:Constants.itemKey) {
        			db.execSQL("DROP TABLE IF EXISTS " + PRIFEX_DOWN+source);
        			db.execSQL("DROP TABLE IF EXISTS " + "index_"+PRIFEX_DOWN+source);
        			db.execSQL("DROP TABLE IF EXISTS " + PRIFEX_LUID+source);        			
        			db.execSQL("DROP TABLE IF EXISTS " + "index_"+PRIFEX_LUID+source);
        			db.execSQL("DROP TABLE IF EXISTS " + PRIFEX_RENAME+source); 
        			db.execSQL("DROP TABLE IF EXISTS " + "index_"+PRIFEX_RENAME+source);
        			
        			db.execSQL("DROP TABLE IF EXISTS " + PRIFEX_MARK+source); 
        			db.execSQL("DROP TABLE IF EXISTS " + "index_"+PRIFEX_MARK+source);   
        			     			
        			
//        		    public static final String PRIFEX_LUID = "appluid_";
//        		}
        	}
        	
        }
        
    }

}
