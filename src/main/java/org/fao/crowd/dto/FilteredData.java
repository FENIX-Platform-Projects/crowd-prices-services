package org.fao.crowd.dto;

import java.util.Iterator;

/**
 * Created by meco on 25/07/16.
 */
public class FilteredData {
    String[] header;
    Iterator<Object[]> data;

    public FilteredData() {
    }
    public FilteredData(String[] header, Iterator<Object[]> data) {
        this.header = header;
        this.data = data;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public Iterator<Object[]> getData() {
        return data;
    }

    public void setData(Iterator<Object[]> data) {
        this.data = data;
    }

    public boolean isEmpty() {
        return data==null || !data.hasNext();
    }
}
