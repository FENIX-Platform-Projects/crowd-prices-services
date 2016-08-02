package org.fao.crowd.services;

import org.fao.crowd.dao.DataDao;
import org.fao.crowd.dto.DataFilter;
import org.fao.crowd.dto.FilteredData;
import org.fao.fenix.commons.utils.CSVWriter;
import org.fao.fenix.commons.utils.FileUtils;
import org.fao.fenix.commons.utils.JSONUtils;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import java.io.*;

@WebServlet (urlPatterns = "/export/csv")
public class DownloadServcice extends HttpServlet {
    @Inject DataDao dao;
    @Inject FileUtils fileUtils;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            export(resp, new DataFilter(req));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (MediaType.APPLICATION_JSON.equalsIgnoreCase(req.getContentType())) {
                String requestContent = fileUtils.readTextFile(req.getInputStream());
                DataFilter filter = JSONUtils.toObject(requestContent, DataFilter.class);
                export(resp, filter);
            } else
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


    private void export (HttpServletResponse resp, DataFilter filter) throws Exception {
        FilteredData data;
        try {
            data = dao.filterData(filter);
        } catch (NotAcceptableException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (NotFoundException ex) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        resp.setContentType(MediaType.TEXT_PLAIN);
        resp.addHeader("Content-Disposition", "attachment;filename=crowdDataExport.csv");
        OutputStream out = resp.getOutputStream();
        new CSVWriter(out, null, null, null, null, null, null, data.getHeader())
                .write(data.getData(), Integer.MAX_VALUE);
        out.close();
    }


}



/*
            //create tmp file with
            File tmpFile = new File("/tmp/" + uidUtils.newId());
            Writer fileOut = new FileWriter(tmpFile);
            CSVWriter csvWriter = new CSVWriter(fileOut, null,null,null,null,null,null, data.getHeader());
            csvWriter.write(data.getData(), Integer.MAX_VALUE);
            fileOut.close();
            //Send tmp file in zip format
            fileUtils.zip(tmpFile, out, false);
            //Remove tmp file
            tmpFile.deleteOnExit();

 */