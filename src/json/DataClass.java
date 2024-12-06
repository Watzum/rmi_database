package json;

import db.Column;
import db.Record;

import java.util.ArrayList;
import java.util.HashMap;

class DataClass {

    int magicCookie;

    int offSet;

    int numberOfFields;

    ArrayList<Column> fields = new ArrayList<>();

    ArrayList<Record> recordList = new ArrayList<>();

}
