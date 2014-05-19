package com.lmis.orm;

/**
 * The Class LmisFields. Different database data types. Also support many2many and
 * many2one
 */
public class LmisFields {

	/**
	 * Varchar.
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String varchar(int size) {
		return " VARCHAR(" + size + ") ";
	}

	/**
	 * Integer.
	 * 
	 * @return the string
	 */
	public static String integer() {
		return " INTEGER ";
	}

	/**
	 * Integer.
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String integer(int size) {
		return " INTEGER(" + size + ") ";
	}

	/**
	 * Text.
	 * 
	 * @return the string
	 */
	public static String text() {
		return " TEXT ";
	}

	/**
	 * Blob.
	 * 
	 * @return the string
	 */
	public static String blob() {
		return " BLOB ";
	}

	/**
	 * Many to many.
	 * 
	 * @param db
	 *            the db
	 * @return the many to many object
	 */
	public static LmisManyToMany manyToMany(Object db) {
		return new LmisManyToMany((LmisDBHelper) db);
	}

	/**
	 * Many to one.
	 * 
	 * @param db
	 *            the db
	 * @return the many to one object
	 */
	public static LmisManyToOne manyToOne(Object db) {
		return new LmisManyToOne((LmisDBHelper) db);
	}

  /*
   * One to Many
   * @param db
   * @return the one to many object
   * */
  public static LmisOneToMany oneToMany(Object db) {
    return new LmisOneToMany((LmisDBHelper) db);
  }

}
