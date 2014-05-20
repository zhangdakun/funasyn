package cn.eben.android.source.edisk;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.funambol.android.AndroidCustomization;
import com.funambol.storage.StringKeyValuePair;
import com.funambol.storage.StringKeyValueSQLiteStore;
import com.funambol.util.Log;

public class EdiskdbStore {

	private static final String TAG = "EdiskdbStore";

	protected String tableName = "edisk_guid";

	protected final String KEY_COLUMN_NAME = "luid";
	protected final String VALUE_COLUMN_NAME = "guid";

	protected final String[] QUERY_KEY_COLUMN = { KEY_COLUMN_NAME };
	protected final String[] QUERY_VALUE_COLUMN = { VALUE_COLUMN_NAME };
	protected final String[] QUERY_KEY_VALUE_COLUMN = { KEY_COLUMN_NAME,
			VALUE_COLUMN_NAME };

	protected SQLiteDatabase dbStore;
	protected DatabaseHelper mDatabaseHelper = null;

	public EdiskdbStore(Context c) {
		mDatabaseHelper = new DatabaseHelper(c, FUNAMBOL_SQLITE_DB_NAME,
				tableName);
		// this.tableName = tableName;

		open();
		dbStore.execSQL(getCreateSQLCommand());
		dbStore.close();
		dbStore = null;
	}

	public Enumeration keyValuePairs() {

		if (dbStore == null) {
			open();
		}

		final Cursor result = dbStore.query(true, tableName,
				QUERY_KEY_VALUE_COLUMN, null, null, null, null, KEY_COLUMN_NAME
						+ " ASC", null);

		final int keyColumnIndex = result
				.getColumnIndexOrThrow(KEY_COLUMN_NAME);
		final int valueColumnIndex = result
				.getColumnIndexOrThrow(VALUE_COLUMN_NAME);

		// Move Cursor to the first element
		result.moveToFirst();

		return new Enumeration() {

			boolean last = false;
			boolean closed = false;

			public Object nextElement() {

				// Get the Current value
				String key = result.getString(keyColumnIndex);
				String value = result.getString(valueColumnIndex);

				// Move Cursor to the next element
				result.moveToNext();

				return new StringKeyValuePair(key, value);
			}

			public boolean hasMoreElements() {
				if (last) {
					return false;
				}
				last = result.isAfterLast();
				if (last) {
					result.close();
					closed = true;
				}
				return !last;
			}

			@Override
			public void finalize() throws Throwable {
				try {
					if (!closed) {
						result.close();
						closed = true;
					}
				} finally {
					super.finalize();
				}
			}
		};
	}

	public void remove(String key) {
		if (dbStore == null) {
			open();
		}
		StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);

		where.append("=\"").append(key).append("\"");
		dbStore.delete(tableName, where.toString(), null);

	}

	public boolean add(String key, String value) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_COLUMN_NAME, key);
		cv.put(VALUE_COLUMN_NAME, value);
		if (dbStore == null) {
			open();
		}
		if (dbStore.insert(tableName, null, cv) != -1) {
			return true;
		}
		return false;
	}

	public boolean update(String oldkey, String newkey) {

		ContentValues cv = new ContentValues();
		cv.put(KEY_COLUMN_NAME, newkey);
		if (dbStore == null) {
			open();
		}
		StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);
		where.append("=\"").append(oldkey).append("\"");
		if (dbStore.update(tableName, cv, where.toString(), null) != -1) {
			return true;
		}

		return false;
	}

	public void updates(String oldkey, String newkey) {
		if (dbStore == null)
			open();

		Cursor dbcursor = dbStore.query(tableName, QUERY_KEY_COLUMN,
				KEY_COLUMN_NAME + " LIKE '" + oldkey + "%'", null, null, null,
				null);
		if (dbcursor.moveToFirst()) {
			do {
				String oldName = dbcursor.getString(0);
				update(oldName, newkey);
			} while (dbcursor.moveToNext());
		}
		dbcursor.close();
		dbStore.close();
		dbStore = null;
	}

	public String get(String vlaue) {

		String result = null;
		if (dbStore == null) {
			open();
		}
		StringBuffer where = new StringBuffer(VALUE_COLUMN_NAME);

		where.append("=\"").append(vlaue).append("\"");
		Cursor resultCursor = dbStore.query(true, tableName, QUERY_KEY_COLUMN,
				where.toString(), null, null, null, null, null);

		if (resultCursor.getCount() > 0) {
			int colIndex = resultCursor.getColumnIndexOrThrow(KEY_COLUMN_NAME);
			resultCursor.moveToFirst();
			result = resultCursor.getString(colIndex);
		}
		resultCursor.close();
		dbStore.close();
		dbStore = null;
		return result;

	}

	public void reset() throws IOException {

		if (dbStore == null) {
			open();
		}
		dbStore.delete(tableName, null, null);
		dbStore.close();
		dbStore = null;
	}

	public void save() {
		if (dbStore != null) {
			dbStore.close();
			dbStore = null;
		}
	}

	protected String getCreateSQLCommand() {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ KEY_COLUMN_NAME + " varchar[50]," + VALUE_COLUMN_NAME
				+ " varchar[50]);";
	}

	private void open() {
		dbStore = mDatabaseHelper.getWritableDatabase();
	}

	public class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String dbName, String tableName) {
			super(context, dbName, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private static final String SYNC_STATUS_TABLE_PREFIX = "syncstatus_";
	private static final String SENT_ITEM_KEY = "SENT_ITEM_";
	private static final String RECEIVED_ITEM_KEY = "RECEIVED_ITEM_";

	private static final String TABLE_EDISK = "edisk";
	private static final String TABLE_EDISK_STATUS = SYNC_STATUS_TABLE_PREFIX
			+ "edisk";

	private static final String FUNAMBOL_SQLITE_DB_NAME = AndroidCustomization
			.getInstance().getFunambolSQLiteDbName();

	public static String checkItemExist(Context context, String source,
			String value) {
		String item = null;
		try {
			funamboldbStore ediskdb = new funamboldbStore(context,
					FUNAMBOL_SQLITE_DB_NAME, source);

			Enumeration enumDb;

			enumDb = ediskdb.keyValuePairs();

			while (enumDb.hasMoreElements()) {
				StringKeyValuePair pairs = (StringKeyValuePair) enumDb
						.nextElement();
				if (value.equalsIgnoreCase(pairs.getValue())) {
					item = pairs.getKey();
					break;
				}
			}

			ediskdb.getDB().close();
			ediskdb = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}

//	public static void resetStoreFingerPrinter(Context context, String source) {
//		try {
//			funamboldbStore ediskdb = new funamboldbStore(context,
//					FUNAMBOL_SQLITE_DB_NAME, source);
//			Enumeration enumDb;
//
//			enumDb = ediskdb.keyValuePairs();
//
//			while (enumDb.hasMoreElements()) {
//				StringKeyValuePair pairs = (StringKeyValuePair) enumDb
//						.nextElement();
//				String fileName = EbenHelpers.decodeKey(pairs.getKey());
//				String fp = pairs.getValue();
//				if (null != fp) {
//					String abFileName = FileHelpers.getFilePath(source)
//							+ File.separator + fileName;
//					String funambolFp = MD5Util.funambolMd5(abFileName);
//
//					Log.info(TAG, " fileName is " + fileName + ", fp: " + fp
//							+ ",funambolFp: " + funambolFp);
//
//					if (fp.equals(funambolFp)) {
//						String newFp = MD5Util.md5(new File(abFileName));
//						Log.info(TAG, "reset : newFp: " + newFp);
//
//						if (null != newFp) {
//							ediskdb.update(pairs.getKey(), newFp);
//						}
//					} else if(!fp.endsWith("==")) {
//						Log.info(TAG, "return ,md5 fp right ");
//						return;
//					}
//				}
//
//			}
//			ediskdb.getDB().close();
//			ediskdb = null;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Log.error(TAG, "resetStoreFingerPrinter : "+e.toString());
////			e.printStackTrace();
//		}
//	}

	public static void reNamedbStore(Context context, String source,
			String oldName, String newName) {
		funamboldbStore ediskdb;
		try {
			ediskdb = new funamboldbStore(context, FUNAMBOL_SQLITE_DB_NAME,
					source);

			ContentValues cValue = new ContentValues();
			cValue.put("_key", newName);
			int count = ediskdb.getDB().update(source, cValue, "_key=?",
					new String[] { oldName });
			Log.info(TAG, "reNameSourceStore , : " + count + " ," + source
					+ ", " + oldName + ", " + newName);

			ediskdb.getDB().close();
			ediskdb = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void reNamedbStore(Context context, String oldName,
			String newName) {
		funamboldbStore ediskdb;
		try {
			ediskdb = new funamboldbStore(context, FUNAMBOL_SQLITE_DB_NAME,
					TABLE_EDISK);

			Cursor cursor = ediskdb.getDB().query(TABLE_EDISK,
					new String[] { "_key" }, "_key LIKE '" + oldName + "%'",
					null, null, null, null);
			String oldKey = null;
			if (cursor != null && cursor.moveToFirst()) {
				do {
					oldKey = cursor.getString(0);
					ContentValues cValue = new ContentValues();
					cValue.put("_key", oldKey.replace(oldName, newName));
					ediskdb.getDB().update(TABLE_EDISK, cValue, "_key=?",
							new String[] { oldKey });
				} while (cursor.moveToNext());
				cursor.close();
				ediskdb.getDB().close();
				ediskdb = null;

				// funamboldbStore statusdb = new funamboldbStore(context,
				// FUNAMBOL_SQLITE_DB_NAME, TABLE_EDISK_STATUS);
				// Cursor statusCursor =
				// statusdb.getDB().query(TABLE_EDISK_STATUS,
				// new String[]{"_key"}, "(_key LIKE '"+SENT_ITEM_KEY +
				// oldName+"%'"
				// + ") or (_key LIKE '"+RECEIVED_ITEM_KEY + oldName+"%'" + ")",
				// null, null, null, null);
				// if(statusCursor != null && statusCursor.moveToFirst()){
				// do{
				// oldKey = statusCursor.getString(0);
				// ContentValues cValue = new ContentValues();
				// cValue.put("_key", oldKey.replace(oldName, newName));
				// statusdb.getDB().update(TABLE_EDISK_STATUS, cValue, "_key=?",
				// new
				// String[]{oldKey});
				//
				// }while(statusCursor.moveToNext());
				// statusCursor.close();
				// }
				// statusdb.getDB().close();
				// statusdb = null;
				EdiskdbStore ediskluiddb = new EdiskdbStore(context);
				ediskluiddb.updates(oldName, newName);
				ediskluiddb.save();

			} else {
				cursor.close();
				ediskdb.getDB().close();
				ediskdb = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static class funamboldbStore extends StringKeyValueSQLiteStore {

		public funamboldbStore(Context c, String dbName, String tableName)
				throws Exception {
			super(c, dbName, tableName);

		}

		public SQLiteDatabase getDB() {
			if (dbStore == null) {
				dbStore = mDatabaseHelper.getWritableDatabase();
			}
			return dbStore;
		}

	}
}
