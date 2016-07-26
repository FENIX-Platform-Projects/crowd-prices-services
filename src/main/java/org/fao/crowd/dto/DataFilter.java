package org.fao.crowd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.fao.fenix.commons.utils.JSONUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataFilter {

    public String country;
    public String[] markets;
    public String[] commodities;
    public Date from;
    public Date to;

    public DataFilter() {
    }
    public DataFilter(HttpServletRequest request) throws Exception {
        country = request.getParameter("country");
        markets = request.getParameterValues("markets");
        commodities = request.getParameterValues("commodities");

        String fromValue = request.getParameter("from");
        if (fromValue!=null)
            from = JSONUtils.toObject(fromValue, Date.class);

        String toValue = request.getParameter("to");
        if (toValue!=null)
            to = JSONUtils.toObject(toValue, Date.class);
    }

}
