package org.fao.crowd.dao;

import org.fao.crowd.dto.DataFilter;
import org.fao.crowd.dto.FilteredData;
import org.fao.crowd.utils.DataSource;
import org.fao.fenix.commons.utils.database.DataIterator;
import org.fao.fenix.commons.utils.database.DatabaseUtils;

import javax.inject.Inject;
import javax.ws.rs.NotAcceptableException;
import java.sql.*;
import java.util.*;

public class DataDao {
    private static final String exportQuerySelection =
            "d.userid as user,\n" +
            "to_char(d.date, 'DD-MM-YYYY') as date,\n" +
            "to_char(d.fulldate, 'HH24:MI:SS') as time,\n" +
            "to_char(d.gaul0code, 'FM999999999999999999') as country_code,\n" +
            "g0.name as country_name,\n" +
            "to_char(d.citycode, 'FM999999999999999999') as city_code,\n" +
            "ci.name as city_name,\n" +
            "d.lat as latitude,\n" +
            "d.lon as longitude,\n" +
            "d.marketcode as market_code,\n" +
            "ma.name as market_name,\n" +
            "d.vendorcode as vendor_code,\n" +
            "d.vendorname as vendor_name,\n" +
            "d.commoditycode as commodity_code,\n" +
            "co.name as commodity_name,\n" +
            "d.varietycode as variety_code,\n" +
            "d.varietyname as variety_name,\n" +
            "d.quantity as quantity,\n" +
            "d.munitcode as quantity_um_code,\n" +
            "mu.name as quantity_um_name,\n" +
            "d.price as price,\n" +
            "d.untouchedprice as untouched_price,\n" +
            "d.currencycode as currency_code,\n" +
            "cu.name as currency_name,\n" +
            "d.note as note";



    @Inject DataSource dataSource;
    @Inject DatabaseUtils databaseUtils;

    public FilteredData filterData(DataFilter filter) throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            Collection<Object> params = new LinkedList<>();
            PreparedStatement statement = connection.prepareStatement(buildFilterDataQuery(connection, filter, params));
            databaseUtils.fillStatement(statement, null, params.toArray());
            ResultSet rawData = statement.executeQuery();
            return new FilteredData(getHeader(rawData), new DataIterator(rawData, connection, null, null));
        } catch (Exception ex) {
            connection.close();
            throw ex;
        }
    }

    private String[] getHeader(ResultSet rawData) throws Exception {
        ResultSetMetaData resultMetadata = rawData.getMetaData();
        String[] header = new String[resultMetadata.getColumnCount()];
        for (int i=0; i<header.length; i++)
            header[i] = resultMetadata.getColumnName(i+1);
        return header;
    }

    private String buildFilterDataQuery(Connection connection, DataFilter filter, Collection<Object> params) throws NotAcceptableException, SQLException {
        String tableName = filter != null ? getCountryTableName(connection, filter.country) : null;
        if (tableName == null)
            throw new NotAcceptableException("Unknown country");


        StringBuilder where = new StringBuilder();
        if (filter.markets != null && filter.markets.length > 0) {
            where.append(" AND marketcode IN (").append(getQuestionMarks(filter.markets.length)).append(')');
            params.addAll(Arrays.asList(filter.markets));
        }
        if (filter.commodities != null && filter.commodities.length > 0) {
            where.append(" AND commoditycode IN (").append(getQuestionMarks(filter.commodities.length)).append(')');
            params.addAll(Arrays.asList(filter.commodities));
        }
        if (filter.from != null) {
            where.append(" AND date >= ?");
            params.add(filter.from);
        }
        if (filter.to != null) {
            where.append(" AND date <= ?");
            params.add(filter.to);
        }

        return  "SELECT " + exportQuerySelection + " FROM " +
                "(SELECT * FROM " + tableName + (where.length() > 0 ? " WHERE " + where.substring(4) : "") + ") d\n"+
                "LEFT OUTER JOIN gaul0 g0 ON (to_char(d.gaul0code, 'FM999999999999999999') = g0.code)\n" +
                "LEFT OUTER JOIN city ci ON (d.citycode = ci.code)\n" +
                "LEFT OUTER JOIN market ma ON (d.marketcode = to_char(ma.code, 'FM999999999999999999'))\n" +
                "LEFT OUTER JOIN commodity co ON (d.commoditycode = to_char(co.code, 'FM999999999999999999'))\n" +
                "LEFT OUTER JOIN munit mu ON (d.munitcode = mu.code)\n" +
                "LEFT OUTER JOIN currency cu ON (d.currencycode = cu.code)";
    }

    private String getQuestionMarks(int count) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < count; i++)
            buffer.append(",?");
        return count > 0 ? buffer.substring(1) : null;
    }

    private String getCountryTableName(Connection connection, String country) throws SQLException {
        if (country==null)
            return null;
        PreparedStatement statement = connection.prepareStatement("SELECT name FROM gaul0 WHERE code = ?");
        statement.setString(1,country);
        ResultSet rawData = statement.executeQuery();
        String name = rawData.next() ? rawData.getString(1) : null;
        rawData.close();
        statement.close();
        return name!=null ? "data_"+name.trim().replace(' ','_').toLowerCase() : null;
    }


}
