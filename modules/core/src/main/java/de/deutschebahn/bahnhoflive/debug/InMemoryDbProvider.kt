package de.deutschebahn.bahnhoflive.debug

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

abstract class ContentProviderTable(
    val db: SQLiteDatabase,
    val providerName: String,
    val tableName: String
) {
    protected val values: HashMap<String, String>? = null

//    private val URL_TABLE = "content://${providerName}/${tableName}"
//    val CONTENT_URI: Uri = Uri.parse(URL_TABLE)
    fun getType(): String = providerName + "//" + tableName

    abstract fun createQuery(queryBuilder: SQLiteQueryBuilder, sortOrder: String?): String?
    abstract fun insert(values: ContentValues?): Long // returns rowID

    abstract fun createInDatabase()
    open fun upgradeInDatabase(oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tableName")
        createInDatabase()
    }

    open fun update(
        values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {

        return db.update(
            tableName,
            values,
            selection,
            selectionArgs
        )

    }

    open fun delete(selection: String?, selectionArgs: Array<String>?): Int {
        return db.delete(tableName, selection, selectionArgs)
    }

}


class ContentProviderTableValues(db:SQLiteDatabase, providerName:String, tableName:String) :
    ContentProviderTable(db, providerName, tableName.lowercase())
{
    override fun createInDatabase() {
        val sqlCreateTable =
            (" CREATE TABLE " + tableName +
                    " (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " key_name TEXT(256) UNIQUE NOT NULL," +
                    " var_type INTEGER DEFAULT(0) NOT NULL," +
                    " var_value TEXT(256)," +
                    " last_change TEXT DEFAULT (DATETIME('NOW', 'localtime') )" +
                    " );"
                    )

        db.execSQL(sqlCreateTable)

        val sqlCreateTrigger =
            ("CREATE TRIGGER trigger_key_values_after_update" +
                    " AFTER UPDATE" +
                    " ON " + tableName +
                    " BEGIN" +
                    " UPDATE key_values" +
                    " SET last_change = datetime('now', 'localtime')" +
                    " WHERE ID = new.ID;" +
                    " END;")

        db.execSQL(sqlCreateTrigger)
    }

    override fun createQuery(queryBuilder : SQLiteQueryBuilder, sortOrder:String?) : String {
        queryBuilder.projectionMap = values
        if (sortOrder == null || sortOrder === "") {
            return InMemoryDbProviderInterface.field_name_values_id
        }
        return sortOrder
    }

    override fun insert(values: ContentValues?) : Long {
        return db.insertWithOnConflict(tableName, "", values, CONFLICT_REPLACE)
    }

}


class ContentProviderTableDebug(db:SQLiteDatabase, providerName:String, tableName:String) :
    ContentProviderTable(db, providerName, tableName.lowercase())
{
    override fun createInDatabase() {
        val sqlCreateTable =
            (" CREATE TABLE " + tableName +
                    " (" +
                    " receive_date_time TEXT DEFAULT (DATETIME('NOW', 'localtime') )," +
                    " debug_key TEXT(256)," +
                    " debug_value BLOB" +
                    " );"
                    )

        db.execSQL(sqlCreateTable)
    }


    override fun createQuery(queryBuilder : SQLiteQueryBuilder, sortOrder:String?) : String {

        queryBuilder.projectionMap = values
        if (sortOrder == null || sortOrder === "") {
            return InMemoryDbProviderInterface.field_name_debug_receive_date_time
        }
        return sortOrder
    }

    override fun insert(values: ContentValues?) : Long {
        return db.insert(tableName, "", values)
    }


}

class InMemoryDbProvider : ContentProvider() {

    private var db: SQLiteDatabase? = null

    private var tables: ArrayList<ContentProviderTable> = arrayListOf()
    private var uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    private fun checkOrDie(uri: Uri?): Int {
        val code = uriMatcher.match(uri)
        if (code >= 1 && code <= tables.size) return code
        throw IllegalArgumentException("Unknown URI $uri")
    }

    fun addTable(table: ContentProviderTable) {
        tables.add(table)

        uriMatcher.apply {
            addURI(
                table.providerName,
                table.tableName,
                tables.size
            ) // tables.size = unique code for table
            addURI(table.providerName, table.tableName + "/*", tables.size)
        }
    }

    override fun getType(uri: Uri): String {
        val code = uriMatcher.match(uri)
        if (code >= 1 && code <= tables.size)
            return tables[code - 1].getType()
        else
            throw IllegalArgumentException("Unsupported URI: $uri")
    }

    override fun onCreate(): Boolean {

//   todo: get providerName from manifest-file (android:authorities)
//        val packageManager = context?.packageManager
//
//        if (packageManager != null) {
//            for (pack in packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)) {
//                val providers = pack.providers
//                if (providers != null) {
//                    for (provider in providers) {
//                        Log.d("Example", "provider: " + provider.authority)
//                        packageManager?.let { provider.loadLabel(it).toString() }?.let { Log.d("cr", it) }
//                    }
//                }
//            }
//        }

        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        db?.also {
            initMe(it, this)
            it.execSQL("PRAGMA recursive_triggers = 0;")

            if(dbHelper.createNeeded) {
                for (table in tables) {
                    table.createInDatabase()
                }
            }

            if(dbHelper.upgradeNeeded) {
                for (table in tables) {
                    table.upgradeInDatabase(dbHelper.oldVersion, dbHelper.newVersion)
                }
            }

        }
        return db != null
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, _sortOrder: String?
    ): Cursor? {

        var sortOrder = _sortOrder
        val qb = SQLiteQueryBuilder()

        val code = checkOrDie(uri)

        qb.tables = tables[code - 1].tableName
        sortOrder = tables[code - 1].createQuery(qb, sortOrder)

        val c = qb.query(
            db, projection, selection, selectionArgs, null,
            null, sortOrder
        )
        context?.also {
            c.setNotificationUri(it.contentResolver, uri)
        }
        return c
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val code = checkOrDie(uri)
        val rowID = tables[code - 1].insert(values)
        if (rowID > 0) {
            val uriWithRowId = ContentUris.withAppendedId(InMemoryDbProviderInterface.getUri(tables[code - 1].tableName), rowID)
            context?.contentResolver?.notifyChange(uriWithRowId, null)
            return uriWithRowId
        }
        throw SQLiteException("Failed to add a record into $uri")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val code = checkOrDie(uri)
        val count = tables[code-1].update(values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val code = checkOrDie(uri)
        val count = tables[code-1].delete(selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }


    private class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, null, null, DATABASE_VERSION) { // name=null -> in Memory

        var oldVersion : Int = -1
            private set

        var newVersion : Int = -1
            private set

        val upgradeNeeded = oldVersion != newVersion

        var createNeeded : Boolean = false
            private set


        override fun onCreate(db: SQLiteDatabase) {
            createNeeded = true
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            this.oldVersion = oldVersion
            this.newVersion = newVersion
        }

    }

    companion object {
        const val DATABASE_VERSION = 1

        fun initMe(db: SQLiteDatabase, provider: InMemoryDbProvider) {
            provider.addTable(ContentProviderTableValues(db, InMemoryDbProviderInterface.PROVIDER_NAME, InMemoryDbProviderInterface.TABLE_NAME_VALUES))
            provider.addTable(ContentProviderTableDebug(db, InMemoryDbProviderInterface.PROVIDER_NAME, InMemoryDbProviderInterface.TABLE_NAME_DEBUG))
        }
    }
}
