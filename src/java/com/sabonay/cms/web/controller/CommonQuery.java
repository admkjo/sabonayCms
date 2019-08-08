/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */ 
package com.sabonay.cms.web.controller;

import com.sabonay.cms.web.common.ApplicationConstant;
import com.sabonay.cms.web.common.CommonUtils;
import com.sabonay.cms.web.wrapper.UserTask;
import com.sabonay.common.formating.StringFormatter;
import com.sabonay.ejb.common.CMSDataSource;
import com.sabonay.ejb.entities.IncludedPage;
import com.sabonay.ejb.entities.Page;
import com.sabonay.ejb.entities.SubPages;
import com.sabonay.ejb.entities.WhereWeAre;
import com.sabonay.ejb.entities.Category;
import com.sabonay.ejb.entities.Resources;
import com.sabonay.ejb.sessionbean.CMSSessionBean;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author emma
 *
 */
@Named
@ViewScoped
public class CommonQuery implements Serializable {

    private static final Logger LOG = Logger.getLogger(CommonQuery.class.getName());
    //CommonUtils CommonUtils = new CommonUtils();
    private int p_offset = 0;//Resource Offset and in some cases
    private //Page offset
            int c_offset = 0;
    private // category Offset
            int r_offset = 0;
    private static int pubp;
    private static int pube;
    private static int pubq; 
    private Page pp = new Page();
    private String hereAreWe = null;
    private int mainPageContentId = 0;
    private String mainPageName = "";
    private String page = "";
    private String pageFile = "";
    private String itemId = null;
    private String serverQueryString = null;
    private Enumeration serverParameterList = null;
    private List<String> serverParameters = new LinkedList<>();

    @PostConstruct
    public void initialize() {

    }

    public Page getPageDetails(int pageid) {
        try {
            Page pg = null;
            String p = "";
            String a = "select * from content where content_id = " + pageid;
            ResultSet rs = CMSDataSource.dataSource().processQuery(a);
            if (rs.next()) {
                pg = new Page();
                p = rs.getInt(1) + "<h1>" + rs.getString(3) + "</h1>" + rs.getString(4) + "<br />" + rs.getString(2) + "<br />" + rs.getString(6) + "<br />" + rs.getString(12);
                pg.setPageid(rs.getInt(1));
                pg.setIsPublished(rs.getString(9));
                pg.setCategoryId(rs.getInt(6));
                pg.setSource(null == rs.getString(11) || "null".equals(rs.getString(11)) || "".equals(rs.getString(11)) || "null null:null:00".equals(rs.getString(11)) ? "Unknown" : rs.getString(11));
                pg.setDatePublished(rs.getTimestamp(8) != null ? new SimpleDateFormat("dd MMM, yyyy hh:mm:ss").format(rs.getTimestamp(8)) : "");
                pg.setDirectUrl(rs.getString(12));
                pg.setKeyword(rs.getString(2));
                pg.setCattype(rs.getString("cattype"));
                pg.setPageContent(rs.getString(4));
                pg.setPageTitle(rs.getString(3));
                pg.setIncludedPages(includedPages(pageid));
                pg.setFirstImage(pageFirstImage(pageid));
            }
            pg.setPageCategory(getPageCategory(pg.getCategoryId(), 0, 20, false));
            pg.setSubPages(getSubPages(pg.getPageid(), 0, 20, false));

            return pg;
        } catch (SQLException sQLException) {
            System.out.println("sQLException..." + sQLException);
        }

        return null;
    }

    public String getUrl() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        System.out.println("get " + request.getRequestURI() + request.getContextPath() + request.getQueryString());
        return request.getRequestURI() + "/" + request.getContextPath() + request.getQueryString();
    }
    public String getUrlForContact() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return  "?"+ request.getQueryString();
    }
   
    
    public String templateFile() throws SQLException, IOException {
        //Get the Http ServletRequest 
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        //Get the Page from the url page eg ?page=5134 
        page = request.getParameter("page");
        System.out.println("page" + page);
        //Check if the page has a value
        if (null == page || "".equals(page)) {
            //Default page/home page
            return "theme/template/" + ApplicationTheme() + "/c_temp.xhtml";

        }
        try {
            pp = getPageDetails(Integer.parseInt(page));
            hereAreWe = "You are here: " + whereWeAre(pp.getPageid(), pp.getCattype(), pp.getPageTitle(), pp.getCategoryId(), pp.getDirectUrl());
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }

        //CMSDataSource.dataSource().
        return "theme/template/" + ApplicationTheme() + "/m_temp.xhtml";

    }

    public String templateAdmin() throws SQLException {
        //Get the Http ServletRequest 
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        //HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

//        System.out.println("request.getQueryString()..." + request.getQueryString());
//        System.out.println("request.getQueryString()..." + request.getMethod());
        try {
            List<FileItem> yitems = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            if (!yitems.isEmpty()) {
                for (FileItem re : yitems) {
                    System.out.println("fileItem..." + re.getName());
                }
            }
        } catch (FileUploadException fileUploadException) {
            System.out.println("fileUploadException>>>>>>>>" + fileUploadException);
        }
        //CommonQueryCRUD.processPageCRUD(request); 

        //Get the Page from the url page eg ?page=5134  
        String p = request.getParameter("page");
        String iid = request.getParameter("itemid");
        pg = request.getParameter("pg") == null ? "0" : request.getParameter("pg");
        tt = request.getParameter("tt") == null ? null : request.getParameter("tt");
        aj = request.getParameter("aj") == null ? null : request.getParameter("aj");
        pid = request.getParameter("pid") == null ? null : request.getParameter("pid");
        cid = request.getParameter("cid") == null ? null : request.getParameter("cid");
        itemid = request.getParameter("itemid") == null ? null : request.getParameter("itemid");

        serverQueryString = request.getQueryString();
        serverParameters = StringFormatter.parseStringToList(serverQueryString, "&");
        serverParameterList = request.getParameterNames();

        pageFile = "welcome.xhtml";

        if (null != p) {
            try {
                page = p;
                itemId = iid;
                pageFile = "page.xhtml";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "./../theme/template/" + ApplicationTheme() + "/a_temp.xhtml";
    }

    public String[][] ApplicationSettings() throws SQLException {
        String[][] siteSettings = new String[50][2];
        String q = "select * from settings order by settings_id";
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        int counter = 0;

        while (rs.next()) {
            siteSettings[counter][0] = rs.getString(2);
            siteSettings[counter][1] = rs.getString(3);
            counter += 1;
        }

        return siteSettings;
    }

    public String ApplicationTheme() throws SQLException {
        String r = "select * from settings where settingname='CURRENTTHEME'";
        //Object setting = CMSDataSource.dataSource().settingFindByKey(q);
        //return setting.;
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        if (rs.next()) {

            return rs.getString(3);
        }
        
        return null;
    }

    public String getApplicationSettings(int x, int y) throws SQLException {
        return ApplicationSettings()[x][y];
    }

    public String getSabonayEducationLink() {
        return CommonUtils.getSettingValue(ApplicationConstant.SABONAY_EDUCATION_LINK);
    }

    public String getStaffMailLink() {
        return CommonUtils.getSettingValue(ApplicationConstant.STAFF_MAIL_URL);
    }

    public int getNewsId() {
        return Integer.parseInt(CommonUtils.getSettingValue(ApplicationConstant.NEWS_CATEGORY));
    }

    public String getCurrenDate() {
        return (new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date()));
    }

    public boolean validatePage(String page) throws SQLException {
        String query = "select contentid from content where contentid='" + page + "' ";
        ResultSet st = CMSDataSource.dataSource().processQuery(query);
        return st.next();
    }

    public ResultSet adminUserLogin(String username, String password) throws SQLException {
        String query = "select * from admin_user where username='" + username + "' and user_password='" + password + "'";
        ResultSet st = CMSDataSource.dataSource().processQuery(query);
        if (st.next()) {
            return st;
        } else {
            return null;
        }
    }

    public ArrayList<IncludedPage> includedPages(int page) throws SQLException {
        String query = "select * from included_pages where contentid='" + page + "' ";
        ResultSet st = CMSDataSource.dataSource().processQuery(query);
        ArrayList<IncludedPage> ipages = new ArrayList<>();

        if (st.next()) {
            ipages.add(new IncludedPage(st.getString("pageloc"), st.getString("contentid"), st.getString("description")));
        } else {
        }

        return ipages;
    }

    //begining of methods used for gallery
    public boolean isItAlbumOrPic(int categoryId, int contentId) throws SQLException {

        if (categoryId == 5004 || contentId == 5004) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isItPicsUnderAlbums(int categoryId) throws SQLException {

        if (categoryId == 5004) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isItAlbums(int contentid) throws SQLException {

        if (contentid == 5004) {
            return true;
        } else {
            return false;
        }

    }

    public String getAlbums(int contentId, boolean showTitle) throws SQLException {
        try {
            String gallery = "";
            String qp = "select * from content where categoryid=" + contentId + " ";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            while (rs1.next()) {
                System.out.println("gethere 1 " + rs1.getInt(1));
                String qp1 = "select * from content_resource where contentid=" + rs1.getInt(1) + " ";
                ResultSet rs2 = CMSDataSource.dataSource().processQuery(qp1);

                if (rs2.next()) {
                    System.out.println("gethere 2 " + rs2.getInt(1));
                    String qp2 = "select * from resources where resourceid=" + rs2.getInt(1) + " ";
                    ResultSet rs3 = CMSDataSource.dataSource().processQuery(qp2);
                    if (rs3.next()) {

                        gallery += "<div style='float:left;margin: 5px;'>"
                                + "<a href='?" + rs1.getString(3) + "&amp;page=" + rs1.getInt(1) + "'>"
                                + "<img style='float:left;max-width:100%; height:140px; width: 140px; max-height:100%; ' class='img-responsive'"
                                + " src=\"" + ApplicationConstant.RESOURCES + rs3.getString(3) + "\" alt='" + rs1.getString(4) + "'"
                                + " title='" + (showTitle == true ? rs1.getString(4) : "") + "' /><br/>"
                                + rs1.getString(3)
                                + "</a>"
                                + "</div>";
                    }
                }
            }
            System.out.println("gallery " + gallery);
            return gallery;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPicsUnderAlbums(int contentId, boolean showTitle) throws SQLException {
        try {
            int returning = 0;
            ResultSet im = CMSDataSource.dataSource().processQuery("Select * from content where categoryid = '" + contentId + "'");
            if (im.next()) {
                returning = im.getInt(1);
            }
            String gallery = "";

            String qp1 = "select * from content_resource where contentid=" + contentId + " ";
            ResultSet rs2 = CMSDataSource.dataSource().processQuery(qp1);

            while (rs2.next()) {
                System.out.println("gethere 2 " + rs2.getInt(1));
                String qp2 = "select * from resources where resourceid=" + rs2.getInt(1) + " ";
                ResultSet rs3 = CMSDataSource.dataSource().processQuery(qp2);
                if (rs3.next()) {
                    gallery += "<div style='float:left;margin: 5px;'>"
                            + "<a href='" + ApplicationConstant.RESOURCES + rs3.getString(3) + "'  rel=\"prettyPhoto[gallery1]\">"
                            + "<img style='float:left;max-width:100%; height:140px; width: 140px; max-height:100%; ' "
                            + " src=\"" + ApplicationConstant.RESOURCES + rs3.getString(3) + "\" alt='" + rs3.getString(4) + "'"
                            + " title='" + (showTitle == true ? rs3.getString(4) : "") + "' />"
                            //                                + "<br/>"
                            //                                + rs3.getString(3)
                            + "</a>"
                            + "</div>";

                }
            }
            System.out.println("gallery " + gallery);
            return gallery;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //end of methods used for gallery
    public ArrayList<Resources> getPageResources(int contentId) throws SQLException {
        String q = "select resources.resourceid,contentid,categoryname,resourceurl,resourcedesc from"
                + " content_resource inner join resources inner join category on"
                + " content_resource.resourceid = resources.resourceid  and resources.category = categoryid"
                + " where content_resource.contentid=" + contentId;
        ResultSet st = CMSDataSource.dataSource().processQuery(q);
        while (st.next()) {
        }
        return null;
    }

    public ArrayList<SubPages> getSubPages(int contentId, int offset, int numtoshow, boolean isAll) throws SQLException {
        String r = "select categoryid,content_id,ptitle,pcontent,cattype,directlink from content where ispublished='yes' and categoryid=" + contentId;
        r = r + CommonUtils.addLimitValues(offset, numtoshow, isAll);
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        ArrayList<SubPages> subpages = new ArrayList<>();
        String url = "";
        while (rs.next()) {
            SubPages cat = new SubPages();
            cat.setCategoryId(rs.getInt(1));
            cat.setContentId(rs.getInt(2));
            cat.setPageTitle(rs.getString(3));
            url = CommonUtils.processUrl("page", cat.getContentId(), cat.getPageTitle(), rs.getString(6));
            cat.setDirectUrl(url);
            String[] f = pageFirstImage(cat.getContentId());
            if (f != null) {
                cat.setDefaultPicture(f[0]);
                cat.setDefaultDesc(f[1]);
                cat.setDefaultPictureAttr(f[2]);
            }
            cat.setSummary(CommonUtils.getSmallContents(rs.getString(4), 300));
            subpages.add(cat);
        }

        return subpages;
    }

    public String[] pageFirstImage(int contentId) throws SQLException {
        String[] fileLoc = null;
        String q = "select resourceurl,resourcedesc, otherattri, resourceorder from"
                + " content_resource inner join resources inner join category on"
                + " content_resource.resourceid = resources.resourceid  and resources.category = category_id"
                + " where content_resource.contentid= " + contentId + " and category=7 limit 0,1";

        //System.out.println("q..." + q);
        ResultSet st = CMSDataSource.dataSource().processQuery(q);
        while (st.next()) {
            fileLoc = new String[4];
            fileLoc[0] = st.getString(1);
            fileLoc[1] = st.getString(2);
            fileLoc[2] = st.getString(3);
            fileLoc[3] = String.valueOf(st.getInt(4));
        }

        return fileLoc;
    }

    public String groupPictures(int categoryid, String htmlElement, String cssClassName, String imgClass, String textClass, boolean linkReference, boolean nameFromResource) throws SQLException {
        String ul = "<ul class='" + cssClassName + "'>";
        String query = "select content.content_id,directlink,ptitle , resourceurl,resourcedesc, otherattri, content.url " + " from content inner join content_resource  inner join resources "
                + "on content.content_id = content_resource.contentid  and " + " content_resource.resourceid = resources.resourceid "
                + "where categoryid = " + categoryid + " and resources.ispublished=1 group by content.content_id";

        //System.out.println("query.." + query);
        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        String url = "";
        while (rs.next()) {
            //System.out.println("Hi there");
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(2));
            if (linkReference) {
                ul += "<li>"
                        + "<a href='" + url + "' title='" + rs.getString(3) + "'" + (ApplicationConstant.LINK_TARGET_NEW.equals(rs.getString(7)) ? " target='_blank'" : 'c') + ">"
                        + "<img style=\"height: 250px; max-height:100%\" class=\"" + imgClass + "\" src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' title='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' " + rs.getString(6) + ">"
                        + "<div class=\"" + textClass + "\">" + (nameFromResource ? (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) : rs.getString(3)) + "</div>"
                        + "</a>"
                        + "</li>";
            } else {
                ul += "<li>"
                        + "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' title='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' " + rs.getString(6) + ">"
                        + "<div>" + (nameFromResource ? (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) : rs.getString(3)) + "</div>"
                        + "</li>";
            }

        }
        ul += "</ul>";
        return ul;
    }

    public String imageList(int contentid, boolean showTitle) throws SQLException {
        String html = "";
        String query = "SELECT resources.resourceid, resourceurl, resourcedesc, otherattri FROM content_resource "
                + "INNER JOIN resources ON content_resource.resourceid = resources.resourceid "
                + "WHERE contentid=" + contentid + " ORDER BY content_resource.resourceid";

        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        while (rs.next()) {
            html += "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(2) + "\" alt=\"" + (null == rs.getString(3) ? "" : rs.getString(3)) + "\" title=\"" + (showTitle == true ? rs.getString(3) : "") + "\" style=\"" + rs.getString(4) + "\" />";
        }

        return html;
    }

    public ArrayList<Category> getPageCategory(int parentId, int offset, int numtoshow, boolean isAll) throws SQLException {
        String q = "select category.category_id,content_id,ptitle,pcontent,cattype,directlink from content inner join category on"
                + " category.category_id = content.categoryid  where content.ispublished='yes' and parent_id='" + parentId + "'";

        q = q + CommonUtils.addLimitValues(offset, numtoshow, isAll);
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        ArrayList<Category> catList = new ArrayList<>();
        String url = "";
        while (rs.next()) {
            Category cat = new Category();
            cat.setCategoryId(rs.getInt(1));
            cat.setContentId(rs.getInt(2));
            cat.setPageTitle(rs.getString(3));
            url = CommonUtils.processUrl("page", cat.getContentId(), cat.getPageTitle(), rs.getString(6));
            cat.setDirectUrl(url);
            String[] pfi = pageFirstImage(cat.getContentId());
            if (pfi != null) {
                cat.setDefaultPicture(pfi[0]);
                cat.setDefaultDesc(pfi[1]);
            }
            cat.setSummary(CommonUtils.getSmallContents(rs.getString(4) == null ? " " : rs.getString(4), 300));
            catList.add(cat);
        }
        return catList;
    }

    public ResultSet getContentByCategory(int categoryid) throws SQLException {
        String qry = "select * from content where ispublished='yes' and categoryid=" + categoryid + " order by content_id DESC"; //Newly added item is the first to be seen
        ResultSet rs = CMSDataSource.dataSource().processQuery(qry);
        return rs;
    }
public String listCategoryData(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul>";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li><a href='" + url + "'> " + rs.getString(3) + "</a></li>";

            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }
    public ResultSet getCategoryByParent(int parentId) throws SQLException {
        String qry = "select category.category_id,content_id,ptitle,directlink,cattype,category_name, datecreated, url from content inner join category on "
                + " category.category_id = content.categoryid   "
                + "  where content.ispublished='yes' and parent_id=" + parentId + " "
                + " ORDER BY category.category_order, category.category_id";

        //System.out.println("q..." + q);
        ResultSet rs = CMSDataSource.dataSource().processQuery(qry);
        return rs;
    }

    public String checkingForComment() throws SQLException {
        
        Random rand  = new Random();
pubp = rand.nextInt(15)+1;
pubq = rand.nextInt(15)+1;
pube = pubp + pubq;

        String qry = "select categoryid from content where content_id = " + pp.getPageid();
        ResultSet rs = CMSDataSource.dataSource().processQuery(qry);
        while (rs.next()) {
            if (rs.getInt(1) == 5328) {
                return "yes";
            }

        }
        return "none";
    }

    
     public String getUrlForReturn() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return "?" + request.getQueryString();
    }

    public String checkingForContactUs() throws SQLException {
        Random rand  = new Random();
pubp = rand.nextInt(15)+1;
pubq = rand.nextInt(15)+1;
pube = pubp + pubq;

        if (pp.getPageid() == 5217) {

            return "yes";
        }

        return "none";
    }

    public String checkingForGallery() throws SQLException {

        if (pp.getPageid() == 5004) {

            return "yes";
        }

        return "none";
    }

    public String checkingForSiteMap() throws SQLException {

        if (pp.getPageid() == 5307) {

            return "/pages/sitemap.xhtml";
        }

        return null;
    }

    public ResultSet getCategoryByParentResource(int parentId) throws SQLException {
        String q = "select category.category_id,content_id,ptitle,directlink,cattype,category_name, url, resourceurl from content inner join category on "
                + " category.category_id = content.categoryid inner join content_resource on content_resource.contentid=content.content_id "
                + " inner join resources on resources.resourceid=content_resource.resourceid "
                + "  where content.ispublished='yes' and parent_id=" + parentId + " "
                + " ORDER BY category.category_id desc";

        //System.out.println("q..." + q);
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        return rs;
    }

    public ResultSet getCategoryByParent_Event(int parentId) throws SQLException {
        String qry = "select category.category_id,content_id,ptitle,directlink,cattype,category_name, datecreated, url from content inner join category on "
                + " category.category_id = content.categoryid   "
                + "  where parent_id=" + parentId + " and datecreated >= now()"
                + " ORDER BY datecreated ";
        ResultSet rs = CMSDataSource.dataSource().processQuery(qry);
        return rs;
    }

    public ResultSet getCategoryByParent(int parentId, String cattype) throws SQLException {

        String q = "";
//        System.out.println("Cattype : " + cattype);
        if (cattype.equalsIgnoreCase("cat")) {
            q = "select parent_id,content_id,ptitle,directlink,cattype from content inner join category on "
                    + " category.category_id = content.categoryid   "
                    + "  where category.category_id =" + parentId;
        } else {
            q = "select content.categoryid,content_id,ptitle,directlink,cattype from content inner join category on "
                    + " category.category_id = content.categoryid   "
                    + "  where content_id=" + parentId;
        }
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        return rs;
    }

    public int getAppDate() {
        int thisyear = new java.util.GregorianCalendar().get(java.util.Calendar.YEAR);
        return thisyear;
    }

    public String siteMap(int pos) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul  style=\"display: block;\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {

                lst = lst + "<li><a href='./" + "'><span>" + sr.getString(3) + "</span></a>"
                        + listCategoryData(sr.getInt(1), false, false)
                        + "</li>";

            } else {

                lst = lst + "<li><a class=\"top_link\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                        + listCategoryData(sr.getInt(1), false, false)
                        + "</li>";

            }
        }
        lst += "</ul>";
        return lst;
    }
//KMA USES THIS HOMELINKS
  
    
    public String homeLinks3(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"menu\" style=\"display: block;\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"current active\"><a class=\"top_link\" href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\"><a class=\"top_link\" href='./" + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
    

  public String HomeLinks1(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"menu\" style=\"display: block;\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"current active\"><a class=\"top_link\" href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\"><a class=\"top_link\" href='./" + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
    


//    ama use this homelinks
    public String homeLinks(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"nav navbar-nav navbar-left\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"navbar-brand current active  \">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + " </span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";

                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a  class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "'><span  >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
    
   
    
     public String HomeLinks(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"nav navbar-nav navbar-left\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"navbar-brand current active  \">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + " </span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";

                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a  class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "'><span  >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
   
//    tma uses this homelinks
    public String homeLinks1(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"nav navbar-nav navbar-left\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"navbar-brand current active  \">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + " </span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";

                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a  class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "'><span  >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
 public String HomeLinks2(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"menu\" style=\"display: block;\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"current active\"><a class=\"top_link\" href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\"><a class=\"top_link\" href='./" + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\"><a class=\"top_link\" href='" + url + "'><span class=\"down\">" + sr.getString(3) + "</span></a>"
                            + listCategoryData(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }
    public String ParkHomeLinks2(int pos, boolean descriptionNeededd, boolean dropdown, boolean showHeading, boolean showTitle) throws SQLException {
        String r = "select category_id from category where category_name='Home' limit 0,1";
        ResultSet rs = CMSDataSource.dataSource().processQuery(r);
        int homepos = 0;
        if (rs.next()) {
            homepos = rs.getInt(1);
        }

        if (pos > 0) {
            pos = pos - 1;
            String qp = "select category_id from category where parent_id=" + homepos + " limit " + pos + ", 1";
            ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
            if (rs1.next()) {
                homepos = rs1.getInt(1);
            }
        }

        String url = "";
        String lst = "<ul class=\"nav navbar-nav navbar-left\">";
        int ct = 0;
        ResultSet sr = getCategoryByParent(homepos);
        String titles = "";
        while (sr.next()) {
            ct = ct + 1;
            String dlinks = "";
            dlinks += sr.getString(4);

            url = CommonUtils.processUrl("page", sr.getInt(2), sr.getString(3), "");

            if (sr.getString(3).contentEquals("Home")) {
                if (showTitle) {
                    lst = lst + "<li class=\"navbar-brand current active  \">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + " </span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"" + (null == page ? "current active" : "") + "\">"
                            + "<a  href='./" + "' title='" + sr.getString(3).trim() + "'><span>" + sr.getString(3) + "</span></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";

                }

            } else {
                if (showTitle) {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a  class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "' title='" + sr.getString(3).trim() + "'><span >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                } else {
                    lst = lst + "<li class=\"dropdown " + (null != page && mainPageContentId == sr.getInt(2) ? "active" : "") + " deeper parent\">"
                            + "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href='" + url + "'><span  >" + sr.getString(3) + "</span><b class=\"caret\"></b></a>"
                            + listCategoryD(sr.getInt(1), showHeading, showTitle)
                            + "</li>";
                }
            }
        }
        lst += "</ul>";
        return lst;
    }

    public String upperLinks(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul>";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    public String pageTitle(int categoryid) throws SQLException {
        String qp = "select ptitle from content where categoryid=" + categoryid + " ";
        ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
        if (rs1.next()) {
            return rs1.getString(1);
        } else {
            return "";
        }
    }

    public String listSublinks(Page p) {
        String lst = "<ul>";

        for (int i = 0; i < p.getPageCategory().size(); i++) {
            Category cat = p.getPageCategory().get(i);
            lst += "<li><a href='" + cat.getDirectUrl() + "'>" + cat.getPageTitle() + "</a></li>";
        }
        lst += "</ul>";

        return lst;
    }

    public String listCategoryD(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul class=\"dropdown-menu\">   ";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li><a href='" + url + "'> " + rs.getString(3) + "</a></li>";

            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }
//only kma uses this
      public String listCategoryDataHome(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getContentByCategory(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul id=\"slider1\">";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));

            if (showTitle) {
                list += "<li class=\"panel\">"
                        + "<div class=\"fl\" style=\"width: 68%!important; height: 100%; border-right: 1px solid #000\">\n"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + resourceDetails(getResourceId(rs.getInt(1)), false) + "</a>"
                        + "</div>"
                        + "<div class=\"home-slider-right fl\">"
                        + "<div class=\"bann\">"
                        + "<h3>TMA General News</h3>" 
                        + "</div>"
                        + "<div class=\"cont\">"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a>"  
                        + "<p>" + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(rs.getString(4), 230 - rs.getString(3).length())) + "...<a style='font-size:12px!important;' href='" + url + "' title='" + rs.getString(3).trim() + "'>read more</a> </p>"
                        + "</div>" 
                        + "</div>"
                        + "</li>";
            } else {
                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }
        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    public String listCategoryData3(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul>";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li><a href='" + url + "'> " + rs.getString(3) + "</a></li>";

            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }
    public String listCategoryData2(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul>";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li><a href='" + url + "'> " + rs.getString(3) + "</a></li>";

            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    
    
    
    public String showComment() throws SQLException {
        int id = pp.getPageid();
        String query = "select *  "
                + " from user_comment where content_id =" + id + " and is_publish= 'YES' group by comment_date ";
        try {

            ResultSet rs = CMSDataSource.dataSource().processQuery(query);

            String ul = "";
            while (rs.next()) {
                System.out.println(rs.getString(2) + " - " + rs.getString(3) + " - " + rs.getString(4) + " - " + rs.getString(5));
                ul += "<li>";
                ul += "<p style='font-size: 11px; ' styleClass='psourcing'> " + rs.getString(5) + "</p>"
                        + " <p> " + rs.getString(3) + "(<i> " + rs.getString(2) + "</i>) </p> "
                        + "<p>" + rs.getString(4) + "</p> <br/><hr/>";

                ul += "</li>";
            }

            ul += "</ul>";
            return ul;
        } catch (Exception e) {
            return null;
        }
    }

    public String listTopStories(int catid, boolean showHeading, boolean showTitle) throws SQLException {

        String newsId = "5328";
        String sql = "select content_id,ptitle,directlink,cattype from content  "
                + "  where ispublished='yes' and categoryid=" + newsId + " order by content_id desc";

//        String sql = "select ptitle) from content where ispublished='yes' and categoryid=" + newsId;
        try {

            ResultSet rs = CMSDataSource.dataSource().processQuery(sql);
//        
            String list = "";
//        ResultSet rs = getCategoryByParent(catid);
            int ct = 0;
            String url;
            while (rs.next()) {
                if (ct == 0) {
                    list += "<ul >";
                }
                ct++;
                url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(2), rs.getString(3));
                if (showTitle) {
                    list += "<li style='border-bottom: 1px dotted dodger-blue'><a href='" + url + "' title='" + rs.getString(2).trim() + "'> " + rs.getString(2) + "</a></li><hr/>";
                } else {
                    list += "<li><a href='" + url + "'> " + rs.getString(2) + "</a></li>";

                }

            }
            if (ct > 0) {
                list += "</ul>";
            }
            if (showHeading) {
                list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String listCategoryDataTab(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            ct++;
            //url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                if (ct == 1) {
                    list += "<li><a href='#' rel=\"tcont" + ct + "\" class='selected'> " + rs.getString(3) + "</a></li>";
                } else {
                    list += "<li><a href='#' rel=\"tcont" + ct + "\"> " + rs.getString(3) + "</a></li>";
                }
            } else {
                if (ct == 1) {
                    list += "<li><a href='#' rel=\"tcont" + ct + "\" class='selected'>" + rs.getString(3) + "</a></li>";
                } else {
                    list += "<li><a href='#' rel=\"tcont" + ct + "\"> " + rs.getString(3) + "</a></li>";
                }

            }

        }

        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    public String listCategoryDataResource(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParentResource(catid);
        int ct = 0;
        String fileExtension;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul>";
            }
            ct++;
            url = ApplicationConstant.RESOURCES + rs.getString("resourceurl");//CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4)); 
            fileExtension = rs.getString("resourceurl").substring(rs.getString("resourceurl").indexOf('.') + 1);
            if (showTitle) {
                list += "<li><a target=/'none/'  class='" + fileExtension + "' href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {

                list += "<li>"
                        + "<a target=/'none/' class='" + fileExtension + "' href='" + url + "'> " + rs.getString(3) + "</a>"
                        + "</li>";
            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    public String Slider2(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getContentByCategory(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));

            if (showTitle) {
                list += "<div class =\"ms-slide slide-1\"  data-delay=\"8\">";

                list += rattrayResourceDetails(getResourceId(rs.getInt(1)), false)
                        + "<h1 class=\"ms-layer stext1  \"\n"
                        + "\n"
                        + "                                style=\"left: 820px; color: white; font-size:74px; font-family: cursive;font-weight:bolder; top: 162px;\"\n"
                        + "\n"
                        + "                                data-type=\"text\"\n"
                        + "\n"
                        + "                                data-duration=\"4900\"\n"
                        + "\n"
                        + "                                data-delay=\"4800\"\n"
                        + "\n"
                        + "                                data-ease=\"easeOutExpo\"\n"
                        + "\n"
                        + "                                data-effect=\"bottom(40)\"\n"
                        + "\n"
                        + "                                >"
                        + rs.getString(3).trim()
                        + "   </h1>\n"
                        + "<div class=\"ms-layer\"\n"
                        + "\n"
                        + "                                 style=\"left: 825px; top: 404px;\"\n"
                        + "\n"
                        + "                                 data-type=\"text\"\n"
                        + "\n"
                        + "                                 data-delay=\"4900\"\n"
                        + "\n"
                        + "                                 data-ease=\"easeOutExpo\"\n"
                        + "\n"
                        + "                                 data-duration=\"4800\"\n"
                        + "\n"
                        + "                                 data-effect=\"scale(1.5,1.6)\"\n"
                        + "\n"
                        + "                                 >"
                        + "<a href='" + url + "' class=\"sbutton1\">Read More!</a>"
                        + "   </div>\n"
                        + "   </div>\n";

            } else {
                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }
        }
        if (ct > 0) {
            list += "   </div>\n";
        }
//        if (showHeading) {
//            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
//        }
        return list;
    }

//    kma uses this slider
    
     public String Slider4(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getContentByCategory(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul id=\"slider1\">";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));

            if (showTitle) {
                list += "<li class=\"panel\">"
                        + "<div class=\"fl\" style=\"width: 68%!important; height: 100%; border-right: 1px solid #000\">\n"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + resourceDetails(getResourceId(rs.getInt(1)), false) + "</a>"
                        + "</div>"
                        + "<div class=\"home-slider-right fl\">"
                        + "<div class=\"bann\">"
                        + "<h3>" + ApplicationTheme() + " General News</h3>"
                        + "</div>"
                        + "<div class=\"cont\">"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a>"
                        + "<p>" + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(rs.getString(4), 230 - rs.getString(3).length())) + "...<a style='font-size:12px!important;' href='" + url + "' title='" + rs.getString(3).trim() + "'>read more</a> </p>"
                        + "</div>"
                        + "</div>"
                        + "</li>";
            } else {
                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }
        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    
    
    public String Slider1(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getContentByCategory(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul id=\"slider1\">";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));

            if (showTitle) {
                list += "<li class=\"panel\">"
                        + "<div class=\"fl\" style=\"width: 68%!important; height: 100%; border-right: 1px solid #333\">\n"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + resourceDetails(getResourceId(rs.getInt(1)), false) + "</a>"
                        + "</div>"
                        + "<div class=\"home-slider-right fl\">"
                        + "<div class=\"bann\">"
                        + "<h3>" + ApplicationTheme() + " General News</h3>"
                        + "</div>"
                        + "<div class=\"cont\">"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a>"
                        + "<p>"
                        + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(rs.getString(4), 230 - rs.getString(3).length())) + "...<a style='font-size:12px!important;' href='" + url + "' title='" + rs.getString(3).trim() + "'>read more</a> </p>"
                        + "<div class=\"fb-share-button\" data-href=" + url + " data-layout=\"box_count\"></div>\n"
                        + "</div>"
                        + "</div>"
                        + "</li>";
            } else {
                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }
        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    
    public String Slider3(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getContentByCategory(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul id=\"slider1\">";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));

            if (showTitle) {
                list += "<li class=\"panel\">"
                        + "<div class=\"fl\" style=\"width: 68%!important; height: 100%; border-right: 1px solid #000\">\n"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + resourceDetails(getResourceId(rs.getInt(1)), false) + "</a>"
                        + "</div>"
                        + "<div class=\"home-slider-right fl\">"
                        + "<div class=\"bann\">"
                        + "<h3>" + ApplicationTheme() + " General News</h3>"
                        + "</div>"
                        + "<div class=\"cont\">"
                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a>"
                        + "<p>" + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(rs.getString(4), 230 - rs.getString(3).length())) + "...<a style='font-size:12px!important;' href='" + url + "' title='" + rs.getString(3).trim() + "'>read more</a> </p>"
                        + "</div>"
                        + "</div>"
                        + "</li>";
            } else {
                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
            }
        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }
//    public String rattrayParkSlider(int catid, boolean showHeading, boolean showTitle) throws SQLException {
//        String list = "";
//        ResultSet rs = getContentByCategory(catid);
//        int ct = 0;
//        String url;
//        while (rs.next()) {
//            if (ct == 0) {
//                list += "<ul id=\"slider1\">";
//            }
//            ct++;
//            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(12));
//
//            if (showTitle) {
//                list += "<li class=\"panel\">"
//                        + "<div class=\"fl\" style=\"width: 68%!important; height: 100%; border-right: 1px solid #333\">\n"
//                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + resourceDetails(getResourceId(rs.getInt(1)), false) + "</a>"
//                        + "</div>"
//                        + "<div class=\"home-slider-right fl\">"
//                        + "<div class=\"bann\">"
//                        + "<h3 >AMA General News</h3>"
//                        + "</div>"
//                        + "<div class=\"cont\">"
//                        + "<a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a>"
//                        + "<p>" + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(rs.getString(4), 230 - rs.getString(3).length())) + "...<a style='font-size:12px!important;' href='" + url + "' title='" + rs.getString(3).trim() + "'>read more</a> </p>"
//                        + "</div>"
//                        + "</div>"
//                        + "</li>";
//            } else {
//                list += "<li class=\"panel\"><a href='" + url + "'> " + rs.getString(3) + "</a></li>";
//            }
//        }
//        if (ct > 0) {
//            list += "</ul>";
//        }
//        if (showHeading) {
//            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
//        }
//        return list;
//    }
    public String listCategoryDataEvent(int catid, boolean showHeading, boolean showTitle) throws SQLException {
        String list = "";
        ResultSet rs = getCategoryByParent_Event(catid);
        int ct = 0;
        String url;
        while (rs.next()) {
            if (ct == 0) {
                list += "<ul class=\"sub\">";
            }
            ct++;
            url = CommonUtils.processUrl("page", rs.getInt(2), rs.getString(3), rs.getString(4));
            if (showTitle) {
                list += "<li><a href='" + url + "' title='" + rs.getString(3).trim() + "'> " + rs.getString(3) + "</a></li>";
            } else {
                list += "<li>"
                        + "<span class=\"date\">"
                        + "<div class=\"month\">" + (new SimpleDateFormat("MMM").format(rs.getDate("datecreated"))) + "</div>"
                        + "<div class=\"day\">" + (new SimpleDateFormat("dd").format(rs.getDate("datecreated"))) + "</div>"
                        + "<div class=\"time\">" + (new SimpleDateFormat("h:mm a").format(rs.getTime("datecreated"))) + "</div>"
                        + "</span>"
                        + "<span>"
                        + "<a href='" + url + "'> " + rs.getString(3) + "</a>"
                        + "</span>"
                        + "</li>";
            }

            if (ct > 2) {
                break;
            }

        }
        if (ct > 0) {
            list += "</ul>";
        }
        if (showHeading) {
            list = "<div class='head'>" + pageTitle(catid) + "</div>" + list;
        }
        return list;
    }

    //whereWeAre($page,$cattype,$curTitle,$curCID,$directlink)
    public String whereWeAre(int pageid, String cattype, String pageTitle, int catid, String diretLink) throws SQLException {
        int parent = catid;
        String c = cattype;
        ArrayList<WhereWeAre> whereAreWeList = new ArrayList<>();

        WhereWeAre where1 = new WhereWeAre();
        where1.setContentId(pageid);
        where1.setPageTitle(pageTitle);
        where1.setWhereUrl(CommonUtils.processUrl("page", where1.getContentId(), where1.getPageTitle(), diretLink));

        whereAreWeList.add(where1);
        String url = "";
        int pid = 0;
        int p = 0;
        do {
            p += 1;
            WhereWeAre where = new WhereWeAre();
            ResultSet resultS = getCategoryByParent(parent, cattype);
            if (resultS.next()) {
                parent = resultS.getInt(1);
                // System.out.println("Cat Id " + resultS.getInt(1) + " 2 :  " + resultS.getInt(2));
                where.setContentId(resultS.getInt(2));
                where.setPageTitle(resultS.getString(3));

                url = CommonUtils.processUrl("page", where.getContentId(), where.getPageTitle(), resultS.getString(4));
                where.setWhereUrl(url);
                cattype = resultS.getString(5);
                if (pid != where.getContentId()) {
                    whereAreWeList.add(where);
                }
                pid = where.getContentId();
            }

            if (p == 5) {
                break;
            }
        } while (parent > 11);

        String ulWhere = "<a href='./' title='Home' class='home'>Home</a> &raquo; ";
        int maxs = whereAreWeList.size();
//        System.out.println("Size Where : " + maxs);
        int pos = 0;
        for (int i = 1; i < maxs; i++) {
            pos = maxs - i;
            WhereWeAre here = whereAreWeList.get(pos);

            //System.out.println("here..." + here.getContentId());
            if (!here.getPageTitle().equalsIgnoreCase("links")) {
                ulWhere = ulWhere + "<a href='" + here.getWhereUrl() + "' title='" + here.getPageTitle() + "'>" + here.getPageTitle() + "</a> &raquo; ";
            }

        }

        if (maxs > 0) {
            mainPageContentId = whereAreWeList.get(maxs - 1).getContentId();
            mainPageName = whereAreWeList.get(maxs - 1).getPageTitle();
        }

        if (c.equalsIgnoreCase("page")) {
            ulWhere += "<a href='" + where1.getWhereUrl() + "' title='" + where1.getPageTitle() + "'>" + where1.getPageTitle() + "</a>";
        }

        return ulWhere;
    }

    public String adminUpdateLink() {
        return CommonUtils.adminUpdateLinks(pp.getPageid());
    }

    public String pageFirstImage() throws SQLException {
        String query = "select  contentid,  content_resource.resourceid,resourceurl,resourcedesc, otherattri, resources.resourceid "
                + " from content_resource inner join  resources on "
                + "content_resource.resourceid= resources.resourceid "
                + "where contentid = " + pp.getPageid() + " and resources.ispublished=1 group by resources.resourceid ";

//        System.out.println("query.." + query);
        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        if (null != pp.getFirstImage()
                && pp.getFirstImage()[3].equals("2")) {
            String ul = "<ul class='mces'>";
            while (rs.next()) {
                ul += "<li>";
                ul += "<img class=\"mces img-responsive\" style=\" width: 480px; float:left; max-width: 100%; height: 250px; max-height:100%\""
                        + " src=\"" + ApplicationConstant.RESOURCES
                        + rs.getString(3) + "\" alt='" + rs.getString(4) + "' title='"
                        + rs.getString(4) + "'  class=\"imgleft\"/>";

                ul += "</li>";
            }

            ul += "</ul>";
//            System.out.println("ul " + ul);
            return ul;

        }
        return "";
    }

    public String pageFirstImageForNotNews() throws SQLException {

        String query = "select  contentid,  content_resource.resourceid,resourceurl,resourcedesc, otherattri, resources.resourceid "
                + " from content_resource inner join  resources on "
                + "content_resource.resourceid= resources.resourceid "
                + "where contentid = " + pp.getPageid() + " and resources.ispublished=1 group by resources.resourceid ";

//        System.out.println("query.." + query);
        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        if (null != pp.getFirstImage()
                && pp.getFirstImage()[3].equals("2")) {
            String ul = "<ul class='mces'>";
            while (rs.next()) {
                ul += "<li>";
                ul += "<img class=\"mces img-responsive\" style=\" width: 400px; float:left; max-width: 100%; height: 250px; max-height:100%\""
                        + " src=\"" + ApplicationConstant.RESOURCES
                        + rs.getString(3) + "\" alt='" + rs.getString(4) + "' title='"
                        + rs.getString(4) + "'  class=\"imgleft\"/>";

                ul += "</li>";
            }

            ul += "</ul>";
//            System.out.println("ul " + ul);
            return ul;

        }
        return "";
    }

    public String pageMainImage() throws SQLException {
        String[] mainImage = pageFirstImage(mainPageContentId);
        if (mainImage != null && mainImage[3].equals("1")) {
            return "<img src=\"" + ApplicationConstant.RESOURCES + mainImage[0] + "\" alt='" + mainImage[1] + "' title='" + mainImage[1] + "' " + mainImage[2] + "/>";
        }
        return "";
    }

    public String includedPages() {
        String pages = "";
        for (int i = 0; i < pp.getIncludedPages().size(); i++) {
            IncludedPage ip = pp.getIncludedPages().get(i);
            String ptn = ApplicationConstant.SITE_URL + ApplicationConstant.RESOURCES.substring(2) + ip.getPageUrl();

            pages += ptn;
        }
        return pages;
    }

    public String categoryPage() {
        String html = "";
        for (int i = 0; i < pp.getPageCategory().size(); i++) {
            Category cat = pp.getPageCategory().get(i);
            html += "<h3><a style=\"font-size:12px\" href='" + cat.getDirectUrl() + "' title='" + pp.getPageTitle() + "'&raquo;" + cat.getPageTitle() + ">" + cat.getPageTitle() + "</a></h3>";
            html += cat.getSummary() + "<a style=\"font-size:12px\" href='" + cat.getDirectUrl() + "' title=' Read more'" + cat.getPageTitle() + " class=\"more\"> &raquo; read more</a>";
        }
        return html;
    }

    public String subPage() {
        String html = "";
        for (int j = 0; j < pp.getSubPages().size(); j++) {
            SubPages spage = pp.getSubPages().get(j);
            html += "<div id=\"listitems\">" + "<a href=\"" + spage.getDirectUrl() + "\" title=\"" + pp.getPageTitle() + " &raquo; " + spage.getPageTitle() + "\">";
            if (spage.getDefaultPicture() != null) {
                html += "<img src=\"" + ApplicationConstant.RESOURCES + spage.getDefaultPicture() + "\" alt=\"" + spage.getDefaultDesc() + "\" title=\"" + spage.getPageTitle() + "\" class=\"imgleft\"" + spage.getDefaultPictureAttr() + "/>";
            }
            html += "</a>"
                    + "<p style=\"font-size:12px\">"
                    + "<a style=\"font-size:12px\" href=\"" + spage.getDirectUrl() + "\" title=\"" + pp.getPageTitle() + " &raquo; " + spage.getPageTitle() + "\">"
                    + spage.getPageTitle() + "</a>"
                    + "</p>";

            html += spage.getSummary() + " <a href=\"" + spage.getDirectUrl() + "\" title=\"Read More: " + spage.getPageTitle() + "\" class=\"more\">"
                    + "&raquo; read more</a>"
                    + "</div>";
        }

        return html;
    }

    public String galleryThumbnail() {
        String html = "<div class=\"page-content\">";
        html += "<ul class=\"thumb-list\">";
        for (int j = 0; j < pp.getSubPages().size(); j++) {
            SubPages spage = pp.getSubPages().get(j);
            html += "<li>";
            html += "<div class=\"picture\">";
            html += "<a href=\"" + spage.getDirectUrl() + "\" title=\"" + spage.getPageTitle() + "\">";
            if (spage.getDefaultPicture() != null) {
                html += "<img src=\"" + ApplicationConstant.RESOURCES + spage.getDefaultPicture() + "\" alt=\"" + spage.getPageTitle() + "\" title=\"" + spage.getPageTitle() + "\"/>";
            }
            html += "</a></div>";
            html += "<div class=\"title\">";
            html += "<a href=\"" + spage.getDirectUrl() + "\" title=\"" + spage.getPageTitle() + "\">" + spage.getPageTitle() + "</a></div>";
        }
        html += "</ul></div>";

        return html;
    }

    public String pageResources() throws SQLException {
        String html = "<div id=\"gallery\">";
        html += "<ul>";
        html += getResourcesList(pp.getPageid());
//        for (int j = 0; j < pp.getSubPages().size(); j++) {
//            SubPages spage = pp.getSubPages().get(j);
//            html += "<li>";
//            html += "<div class=\"picture\">";
//            html += "<a href=\"" + spage.getDirectUrl() + "\" title=\"" + spage.getPageTitle() + "\">";
//            if (spage.getDefaultPicture() != null) {
//                html += "<img src=\"" + ApplicationConstant.RESOURCES + spage.getDefaultPicture() + "\" alt=\"" + spage.getPageTitle() + "\" title=\"" + spage.getPageTitle() + "\"/>";
//            }
//            html += "</a></div>";
//            html += "<div class=\"title\">";
//            html += "<a href=\"" + spage.getDirectUrl() + "\" title=\"" + spage.getPageTitle() + "\">" + spage.getPageTitle() + "</a></div>";
//        }
        html += "</ul></div>";

        return html;
    }

    public int getResourceId(int contentid) throws SQLException {
        String qp = "select resourceid from content_resource where contentid=" + contentid + " ";
        ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
        if (rs1.next()) {
            return rs1.getInt(1);
        } else {
            return 0;
        }
    }

    public String ResourceDetails2(int resourceid, boolean showTitle) throws SQLException {
        String qp = "select * from resources where resourceid=" + resourceid + " ";
        ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
        if (rs1.next()) {
            return "<img class='img-responsive'   src=\"" + ApplicationConstant.RESOURCES + rs1.getString(3) + "\" alt='Slide1 background' title='" + (showTitle == true ? rs1.getString(4) : "") + "' />";
        } else {
            return "";
        }
    }
    
     public String imageList(int contentid, boolean showTitle, boolean linkify) throws SQLException {
        String html = "";
        String query = "SELECT resources.resourceid, resourceurl, resourcedesc, url, directlink FROM content_resource "
                + "INNER JOIN resources ON content_resource.resourceid = resources.resourceid "
                + "INNER JOIN content ON content_resource.contentid = content.content_id "
                + "WHERE contentid=" + contentid + " ORDER BY content_resource.resourceid";
        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        while (rs.next()) {
            if (linkify) {
                html += "<a href='" + rs.getString(5) + "' title='" + rs.getString(3) + "'" + (ApplicationConstant.LINK_TARGET_NEW.equals(rs.getString(4)) ? " target='_blank'" : 'c') + ">"
                        + "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(2) + "\" alt=\"" + (null == rs.getString(3) ? "" : rs.getString(3)) + "\" title=\"" + (showTitle == true ? rs.getString(3) : "") + "\" />"
                        + "</a>";
            } else {
                html += "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(2) + "\" alt=\"" + (null == rs.getString(3) ? "" : rs.getString(3)) + "\" title=\"" + (showTitle == true ? rs.getString(3) : "") + "\" />";
            }

        }

        return html;
    }

    public String resourceDetails(int resourceid, boolean showTitle) throws SQLException {
        String qp = "select * from resources where resourceid=" + resourceid + " ";
        ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
        if (rs1.next()) {
            return "<img style='max-width:100% width: 100%; height:380px,  max-height:100%; ' class='img-responsive' src=\"" + ApplicationConstant.RESOURCES + rs1.getString(3) + "\" alt='" + rs1.getString(4) + "' title='" + (showTitle == true ? rs1.getString(4) : "") + "' />";
        } else {
            return "";
        }
    }

    public String resourcedetails(int resourceid, boolean showTitle) throws SQLException {
        String qp = "select * from resources where resourceid=" + resourceid + " ";
        ResultSet rs1 = CMSDataSource.dataSource().processQuery(qp);
        if (rs1.next()) {
//            System.out.println("<img style='' class='img-responsive' src=\"" + ApplicationConstant.RESOURCES + rs1.getString(3) + "\" alt='" + rs1.getString(4) + "' title='" + (showTitle == true ? rs1.getString(4) : "") + "' />");
            return "<img style='' class='img-responsive' src=\"" + ApplicationConstant.RESOURCES + rs1.getString(3) + "\" alt='" + rs1.getString(4) + "' title='" + (showTitle == true ? rs1.getString(4) : "") + "' />";
        } else {
            return "";
        }
    }

    public String resourceDirect(String resourceName, int width, int height) {
        if (0 == width && 0 == height) {
            return "<img src=\"" + ApplicationConstant.RESOURCES + resourceName + "\" />";
        } else {
            return "<img src=\"" + ApplicationConstant.RESOURCES + resourceName + "\" style='width:" + width + "px; height:" + height + "px'/>";
        }

    }

    public String getResourcesList(int contenId) throws SQLException {
        String html = "<br/>";
        String q = "select resources.resourceid,contentid,category_name,resourceurl,resourcedesc,content.ptitle from"
                + " content_resource inner join resources inner join category on"
                + " content_resource.resourceid = resources.resourceid  and resources.category = category_id"
                + " inner join content on content.content_id=content_resource.contentid"
                + " where content_resource.resourceorder=3 and content_resource.contentid=" + contenId; //3 is for displaying gallaric images

        //System.out.println("..." + q); 
        ResultSet st = CMSDataSource.dataSource().processQuery(q);
        while (st.next()) {
            html += "<li>"
                    + "<span class=\"image\">"
                    + "<a href=\"" + ApplicationConstant.RESOURCES + st.getString(4) + "\" title=\"" + (null == st.getString(5) || "".equals(st.getString(5)) ? st.getString(6) : st.getString(5)) + "\">"
                    + "<img src=\"" + ApplicationConstant.RESOURCES + st.getString(4) + "\"/>"
                    + "</a></span>"
                    + "</li>";
        }
        return html;
    }

    public String getResourceDetail(int contentid, boolean linkify, boolean showTitle) throws SQLException {
        String q = "select resources.resourceid,contentid,category_name,resourceurl,resourcedesc,ptitle,url,directlink from"
                + " content_resource inner join resources inner join category inner join content on "
                + " content_resource.resourceid = resources.resourceid  and resources.category = category_id"
                + " and content_resource.contentid=content.content_id where content.ispublished='yes' and content_resource.contentid=" + contentid;
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        String url = "";
        if (rs.next()) {
            if (linkify) {
                String link = CommonUtils.processUrl("cat", rs.getInt(2), rs.getString(6), rs.getString(8));
                url = "<a href='" + link + "' " + CommonUtils.urlTarget(rs.getString(7), rs.getString(8)) + " >\n"
                        + "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + rs.getString(6) + "' " + (showTitle == true ? "title='" + rs.getString(6) + "'" : "") + "/>\n"
                        + "</a>";
            } else {
                url = "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + rs.getString(6) + "' " + (showTitle == true ? "title='" + rs.getString(6) + "'" : "") + "/>";
            }
        }
        return url;
    }

//    kma uses this sublinks
    
     public String Sublinks4() throws SQLException {
        String lst = "<ul>";
        Category cat = null;
        for (int i = 0; i < pp.getPageCategory().size(); i++) {
            cat = pp.getPageCategory().get(i);
            lst += "<li><a href='" + cat.getDirectUrl() + "'>" + cat.getPageTitle() + "</a></li>";
        }

        for (int j = 0; j < pp.getSubPages().size(); j++) {
            SubPages spage = pp.getSubPages().get(j);
            lst += "<li><a href='" + spage.getDirectUrl() + "'>" + spage.getPageTitle() + "</a></li>";
        }

        lst += "</ul>";

        if (lst.contentEquals("<ul></ul>")) {
            lst = "";
        }

        if (pp.getPageCategory().size() < 3 && pp.getSubPages().size() < 3) {
            lst += getParentPartner(pp.getCategoryId(), pp.getCattype());
        }
        return lst;
    }
    
    
    //public String resourceDetailsList(int)
    public String Sublinks1() throws SQLException {
        String lst = "";
        Category cat = null;
        for (int i = 0; i < pp.getPageCategory().size(); i++) {
            cat = pp.getPageCategory().get(i);
            lst += "<a class=\"list-group-item\" href='" + cat.getDirectUrl() + "'>" + cat.getPageTitle() + "</a>";
        }

        for (int j = 0; j < pp.getSubPages().size(); j++) {
            SubPages spage = pp.getSubPages().get(j);
            lst += "<a class=\"list-group-item\" href='" + spage.getDirectUrl() + "'>" + spage.getPageTitle() + "</a>";
        }

        lst += "";

        if (lst.contentEquals("")) {
            lst = "";
        }

        if (pp.getPageCategory().size() < 3 && pp.getSubPages().size() < 3) {
            lst += getParentPartner(pp.getCategoryId(), pp.getCattype());
        }
        return lst;
    }

//    kma uses this 
    public String sublinks1() throws SQLException {
        String lst = "<ul>";
        Category cat = null;
        for (int i = 0; i < pp.getPageCategory().size(); i++) {
            cat = pp.getPageCategory().get(i);
            lst += "<li><a href='" + cat.getDirectUrl() + "'>" + cat.getPageTitle() + "</a></li>";
        }

        for (int j = 0; j < pp.getSubPages().size(); j++) {
            SubPages spage = pp.getSubPages().get(j);
            lst += "<li><a href='" + spage.getDirectUrl() + "'>" + spage.getPageTitle() + "</a></li>";
        }

        lst += "</ul>";

        if (lst.contentEquals("<ul></ul>")) {
            lst = "";
        }

        if (pp.getPageCategory().size() < 3 && pp.getSubPages().size() < 3) {
            lst += getParentPartner(pp.getCategoryId(), pp.getCattype());
        }
        return lst;
    }

    
    public String getParentPartner(int catid, String cattype) throws SQLException {
        String q, nq = "";
        int p = 0;
        if (cattype.equalsIgnoreCase("cat")) {
            q = "select parent_id,ptitle,directlink from category inner join content on "
                    + " content.categoryid = category.category_id "
                    + "  where content.ispublished='yes' and category.category_id= " + catid;

            ResultSet rs = CMSDataSource.dataSource().processQuery(q);
            if (rs.next()) {
                p = rs.getInt(1);

            }
            q = "select ptitle from category inner join content on "
                    + " content.categoryid = category.category_id "
                    + "  where content.ispublished='yes' and category.category_id=" + p;

            ResultSet rs1 = CMSDataSource.dataSource().processQuery(q);

            nq = " select content_id, ptitle, directlink, cattype from category inner join content on "
                    + " content.categoryid = category.category_id  "
                    + "  where content.ispublished='yes' and parent_id = " + p;
        } else {
            nq = "Select content_id, ptitle, directlink, cattype from content where ispublished='yes' and categoryid='" + catid + "'  and cattype='" + cattype + "'";
        }

        //  System.out.println("Nq &raquo; " + nq);
        ResultSet rs3 = CMSDataSource.dataSource().processQuery(nq);
        String pUl = "";
        String url = "";
        while (rs3.next()) {
            url = CommonUtils.processUrl("page", rs3.getInt(1), rs3.getString(2), rs3.getString(3));
            pUl = pUl
                    //                    + "<li>"
                    + "<a class=\"list-group-item\" href='" + url + "' title='" + rs3.getString(2).trim() + "' > " + rs3.getString(2) + " </a>";
//                    + "</li>";
        }
//        pUl += "</ul>";

        return pUl;
    }

    public ArrayList<SubPages> searchAllContent(String val, int offset, int numToShow, boolean isAll) throws SQLException {
        val = CommonUtils.addSlashes(val);
//        System.out.println("Val : " + val);
        String q = "select categoryid,contentid,ptitle,pcontent,cattype,directlink from content where (contentid like '%" + val + "%' or ptitle like '%" + val + "%' or keywords like '%" + val + "%' or pcontent like '%" + val + "%' or categoryid like '%" + val + "%') and ispublished=1  order by lastmodified,datecreated desc ";

        q += CommonUtils.addLimitValues(offset, numToShow, isAll);
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        ArrayList<SubPages> subpages = new ArrayList<>();
        String url = "";
        int ct = 0;
        while (rs.next()) {
            ct++;
            SubPages cat = new SubPages();
            cat.setCategoryId(rs.getInt(1));
            cat.setContentId(rs.getInt(2));
            cat.setPageTitle(rs.getString(3));
            url = CommonUtils.processUrl("page", cat.getContentId(), cat.getPageTitle(), rs.getString(6));
            cat.setDirectUrl(url);

            cat.setSummary(CommonUtils.getSmallContents(rs.getString(4), 300));
            subpages.add(cat);
        }
//        System.out.println("CCat : " + ct);
        return subpages;
    }

//    public String  
    public ArrayList<SubPages> getGroupMembers(int groupid, String orderBy) throws SQLException {
        String q = "select categoryid,content.contentid,ptitle,pcontent,cattype,directlink from content inner join groups on content.contentid = groups.contentid where groupid=" + groupid;
        q = q + CommonUtils.addLimitValues(0, 6, true) + " order by rand()";

//        System.out.println("Group : " + q);
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        ArrayList<SubPages> subpages = new ArrayList<>();
        String url = "";
        while (rs.next()) {
            SubPages cat = new SubPages();
            cat.setCategoryId(rs.getInt(1));
            cat.setContentId(rs.getInt(2));
            cat.setPageTitle(rs.getString(3));
            url = CommonUtils.processUrl("page", cat.getContentId(), cat.getPageTitle(), rs.getString(6));
            cat.setDirectUrl(url);
            String[] f = pageFirstImage(cat.getContentId());
            if (f != null) {
                cat.setDefaultPicture(f[0]);
                cat.setDefaultDesc(f[1]);
            }
            cat.setSummary(CommonUtils.getSmallContents(rs.getString(4), 400));
            subpages.add(cat);
        }

        return subpages;
    }

    public boolean addContact(String sender, String email, String topic, String tel, String comment) {
        String q = "insert into contactus (sendersname,sendersemail,topic,comments,dateposted,extrainfo) values (\"" + sender + "\",\"" + email + "\",\"" + topic + "\",\"" + comment + "\",now(),\"tel : " + tel + "\")";
        System.out.println("q : " + q);
        try {
            CMSDataSource.dataSource().processQuery(q);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public String getUserTask(String username) throws SQLException {
        String q = "select user_privilege from admin_user where username = '" + username + "'";
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        String results = "";
        String whereBuilder = "";
        String taskQuery = "";
        List<UserTask> userTaskList = new ArrayList<>();
        int numericCounter = 0;
        int nan = 0;

        if (rs.next()) {
            results = rs.getString(1);
        }

        String[] priviledgeList = results.split("#");
        for (String priviledgeList1 : priviledgeList) {
            try {
                int in = Integer.parseInt(priviledgeList1);
                numericCounter++;
                if (numericCounter == 1) {
                    whereBuilder += " where ";
                } else {
                    whereBuilder += " or ";
                }
                whereBuilder += " content_id = " + priviledgeList1;
            } catch (NumberFormatException e) {
                nan++;
                userTaskList.add(new UserTask("", priviledgeList1.toLowerCase(), priviledgeList1));
            }
        }

        taskQuery = "select content_id,ptitle from content " + whereBuilder;

        ResultSet taskResults = CMSDataSource.dataSource().processQuery(taskQuery);
        while (taskResults.next()) {
            userTaskList.add(new UserTask(taskResults.getString(1), "content".toLowerCase(), taskResults.getString(2)));
        }

        String html = "<ul>";
        boolean pagesStart = false;
        for (UserTask ut : userTaskList) {
            if (!pagesStart && ut.getIndex().length() > 0) {
                pagesStart = !pagesStart;
                html += "</ul>";
                html += "<div class=\"ptitle\">Pages </div>";

                html += "<ul>";
            }
            String otherlink = "";
            if (ut.getIndex().length() > 0) {
                otherlink = "&index=" + ut.getIndex() + "&itemid=" + ut.getIndex(); //"&index=" + ut.getIndex() + 
            }
            html += "<li><a href='?page=" + ut.getPage() + otherlink + "'>" + ut.getTitle() + "</a></li>";
        }
        html += "</ul>";

        return html;
    }

    public String getLinkPart(int parentid) throws SQLException {
        String html = "";
        ResultSet rs = getCategoryByParent(parentid);
        while (rs.next()) {
            html += "<a href=\"?page=contentq&amp;itemid='" + rs.getString(1) + "\">" + rs.getString(6) + "</a>";
        }

        return html;
    }

    public int maxPageRowViewCat(String page) throws SQLException {
        String q = "Select count(*) from " + page;

        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    private int findResultSetRows(ResultSet rs) throws SQLException {
        int size = 0;
        if (rs != null) {
            rs.beforeFirst();
            rs.last();
            size = rs.getRow();

            rs.first();
        }
        return size;
    }

    public ResultSet getCategoryFindList(int offset, String ifAll) throws SQLException {
        String q = "select * from category order by category_id desc, parent_id ";
        if ("NO".equals(ifAll)) {
            q += " limit " + offset + ", " + maxRows_rs_viewcat;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    
    public ResultSet getCategoryFindList2(int offset, String ifAll) throws SQLException {
        String q = "select * from category order by category_id desc, parent_id ";
        if ("NO".equals(ifAll)) {
            q += " limit " + offset + ", " + maxRows_rs_viewcat;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }
    public ResultSet getSubCategoryFind(int parentid, int offset, String ifAll) throws SQLException {
        String q = "select * from category inner join content on category.category_id=content.categoryid where parent_id = " + parentid;
        if ("NO".equals(ifAll)) {
            q += " limit " + offset + ", " + maxRows_rs_viewcat;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getCategoryDetails(int catid) throws SQLException {
        String q = "select * from category where category_id=" + catid;
        return CMSDataSource.dataSource().processQuery(q);
    }

    
    public String getCategoryName(String name) throws SQLException {
        String q = "select * from category where category_id='" + name + "'";
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        if (rs.next()) {
            return rs.getString("category_name");
        } else {
            return "None";
        }
    }

    public ResultSet loadRelatedParent(int catid) throws SQLException {
        String q = "select parent_id from category where category_id=" + catid;
        ResultSet rs = CMSDataSource.dataSource().processQuery(q);
        int r = 0;
        if (rs.next()) {
            if (rs.getInt("parent_id") < 1) {
                r = 0;
            } else {
                r = rs.getInt("parent_id");
            }
        }

        q = "SELECT * FROM category where parent_id =" + r;

        //System.out.println("......" + q);
        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet loadRelatedContentParent(int catid, String cattype) throws SQLException {
        String q = "";
        if ("page".equals(cattype)) {
            q = "select content_id as id , ptitle as title from content where content_id=" + catid;
        } else {
            q = "select category_id as id , category_name as title from category where category_id=" + catid;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getContentList(int offset, String ifAll) throws SQLException {
        String q = "select * from content order by content_id   desc ";
        if ("NO".equals(ifAll)) {
            q += " limit " + offset + ", " + maxRows_rs_viewcat;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getContentDetails(int colname_rs_up) throws SQLException {
        String q = String.format("SELECT * FROM content WHERE content_id = %s", CommonUtils.GetSQLValueString(colname_rs_up, "int"));
        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getContentResource(int contentid) throws SQLException {
        String q = "select resources.resourceid,content_resource.contentid,category_name,resourceurl,resourcedesc, resourceorder from "
                + "content_resource inner join resources inner join category on "
                + "content_resource.resourceid = resources.resourceid  and resources.category = category.category_id "
                + "where content_resource.contentid=" + contentid;

        //System.out.println("............" + q);
        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getSiteBanners() throws SQLException {
        String q = "select resourceurl,resources.resourceid from category inner join content inner join content_resource inner join resources "
                + "on  category.category_id = content.categoryid "
                + "and content.content_id = content_resource.contentid "
                + "and resources.resourceid = content_resource.resourceid "
                + "where parent_id=" + CommonUtils.SITE_ID + " and category_name like '%banner%'";

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getRelatedPages(int contentid) throws SQLException {
        String q = "select * from related_pages where primary_page=" + contentid + " or secondary_page=" + contentid;
        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet searchResources(String ps) throws SQLException {
        String q = "select * from resources inner join category "
                + "on resources.category =category.category_id where resourceurl like '%" + ps + "%' or resourcedesc like '%" + ps + "%' limit 0,20";

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getUserCommentList(int contentid, int offset, String isAll) throws SQLException {
        String q = "";
        if ("no".equals(isAll.toLowerCase())) {
            q = "select * from user_comment where content_id = " + contentid + " limit " + offset + ", 50 ";
        } else {
            q = "select count(user_comment_id) as cnt from user_comment where content_id = " + contentid;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getContentByCat(int catid, int offset, String isAll) throws SQLException {
        String q = "select * from content where categoryid =" + catid;
        if ("no".equals(isAll.toLowerCase())) {
            q += " limit " + offset + ", " + maxRows_rs_viewcat;
        } else {
            q = "select count(content_id) as pg from content where categoryid= " + catid;
        }
        return CMSDataSource.dataSource().processQuery(q);
    }

    public String slidingPicturesUnderParent_id(int parent_id, String htmlElement, String cssClassName, String imgClass, String textClass, boolean linkReference, boolean nameFromResource) throws SQLException {

        String ul = "<ul class='" + cssClassName + "'>";
        String query = "select content.content_id,directlink,ptitle , resourceurl,resourcedesc, otherattri, content.url "
                + " from category inner join content_resource  inner join resources inner join content "
                + "on category.category_id = content.categoryid  and "
                + "content.content_id = content_resource.contentid and"
                + " content_resource.resourceid = resources.resourceid "
                + "where parent_id = " + parent_id + " and resources.ispublished=1 group by content.content_id";

//        System.out.println("query.." + query);
        ResultSet rs = CMSDataSource.dataSource().processQuery(query);
        String url = "";
        while (rs.next()) {
            //System.out.println("Hi there");
            url = CommonUtils.processUrl("page", rs.getInt(1), rs.getString(3), rs.getString(2));
            if (linkReference) {
                ul += "<li>"
                        + "<a href='" + url + "' title='" + rs.getString(3) + "'" + (ApplicationConstant.LINK_TARGET_NEW.equals(rs.getString(7)) ? " target='_blank'" : 'c') + ">"
                        + "<img  class=\"" + imgClass + "\" src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' title='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' " + rs.getString(6) + ">"
                        + "<div style='float:right' class=\"" + textClass + "\">" + (nameFromResource ? (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) : rs.getString(3)) + "</div>"
                        + "</a>"
                        + "</li>";
            } else {
                ul += "<li>"
                        + "<img src=\"" + ApplicationConstant.RESOURCES + rs.getString(4) + "\" alt='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' title='" + (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) + "' " + rs.getString(6) + ">"
                        + "<div>" + (nameFromResource ? (rs.getString(5) == null ? rs.getString(3) : rs.getString(5).trim()) : rs.getString(3)) + "</div>"
                        + "</li>";
            }

        }
        ul += "</ul>";
        return ul;
    }

    public String shortenMce(int pageId) {

        String url = CommonUtils.processUrl("page", pageId, getPageDetails(pageId).getPageTitle(), getPageDetails(pageId).getDirectUrl());
        String shorten = "<p style = 'text-align:left; margin-left:5px'>" + StringEscapeUtils.unescapeHtml(CommonUtils.getSmallContents(getPageDetails(pageId).getPageContent(), 60)) + "...<a style='font-size:12px!important;' href='" + url + "'>read more</a> ";
        return shorten;
    }

    public ResultSet getResourceListDesc(int offset, String ifAll, String siteid) throws SQLException {
        String q = "select * from resources   inner join category on resources.category = category.category_id";
        if ("no".equals(ifAll.toLowerCase())) {
            q += " order by resourceid desc limit " + offset + ", " + maxRows_rs_viewcat;
        }

        return CMSDataSource.dataSource().processQuery(q);
    }

    public ResultSet getListType(int categoryid) throws SQLException {
        String q = "";
        ResultSet rs = null;
        if (categoryid > 4999) {
            q = "select categoryid from content where content_id=" + categoryid;

            rs = CMSDataSource.dataSource().processQuery(q);
        }

        if (findResultSetRows(rs) > 0) {
            categoryid = rs.getInt("categoryid");

            while (categoryid > 4999) {
                q = "select categoryid  from content where content_id=" + categoryid;

                rs = CMSDataSource.dataSource().processQuery(q);
                if (rs.next()) {
                    categoryid = rs.getInt("categoryid");
                }
            }
        }

        String q2 = "select list_type,allow_comment from category where category_id=" + categoryid;
        return CMSDataSource.dataSource().processQuery(q2);
    }

    public ResultSet getResourceDetails(int resourceid) throws SQLException {
        String q = "select * from resources inner join category on resources.category = category.category_id where resourceid=" + resourceid;

        return CMSDataSource.dataSource().processQuery(q);
    }

    
     public ResultSet getResourceDetails2(int resourceid) throws SQLException {
        String q = "select * from resources inner join category on resources.category = category.category_id where resourceid=" + resourceid;

        return CMSDataSource.dataSource().processQuery(q);
    }
    public String getResourceUrl(String resourceid) {
        try {
            String q = "select * from resources where resourceid='" + resourceid + "'";

            return CMSDataSource.dataSource().processQuery(q).getString("resourceurl");
        } catch (SQLException sQLException) {
        }
        return null;
    }

    public String shortenContent(String content, int max) {
        if (content.length() <= max) {
            return content;
        } else {
            return content.substring(0, max) + "...";
        }
    }
    String pg = "0"; //Pagination
    String tt = null; //Total Number of rows returned
    String pgnum = "0";
    String ttnum = null;
    private String aj = null;
    String pid = null; //Parent Id
    String itemid = null; //Parent Id
    String index = null; //Parent Id
    String cid = null;
    int parentid = 0;
    String q = null;
    ResultSet row_rs_up = null;
    String pageNum_rs_sim = null;
    String pageNum_rs_resource = null;
    String totalRows_rs_sim = null;
    int maxRows_rs_viewcat = CommonUtils.pageRowNo();

    private void initNonIndexPage() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        String p = request.getParameter("page");
        String iid = request.getParameter("itemid");
        pg = request.getParameter("pg") == null ? "0" : request.getParameter("pg");
        tt = request.getParameter("tt") == null ? null : request.getParameter("tt");
        pgnum = request.getParameter("pgnum") == null ? "0" : request.getParameter("pgnum");
        ttnum = request.getParameter("ttnum") == null ? null : request.getParameter("ttnum");
        aj = request.getParameter("aj") == null ? null : request.getParameter("aj");
        pid = request.getParameter("pid") == null ? null : request.getParameter("pid");
        cid = request.getParameter("cid") == null ? null : request.getParameter("cid");
        itemid = request.getParameter("itemid") == null ? null : request.getParameter("itemid");
        index = request.getParameter("index") == null ? null : request.getParameter("index");
        pageNum_rs_sim = request.getParameter("pageNum_rs_sim") == null ? null : request.getParameter("pageNum_rs_sim");
        pageNum_rs_resource = request.getParameter("pageNum_rs_resource") == null ? null : request.getParameter("pageNum_rs_resource");
        totalRows_rs_sim = request.getParameter("totalRows_rs_sim") == null ? null : request.getParameter("totalRows_rs_sim");
        q = request.getParameter("q") == null ? null : request.getParameter("q");

        page = p;

        serverQueryString = request.getQueryString();
        serverParameters = StringFormatter.parseStringToList(serverQueryString, "&");
        serverParameterList = request.getParameterNames();

    }

    //<editor-fold defaultstate="collapsed" desc="Category Pages">
    public String categoryPageView() throws SQLException {
        String currentPage = "index.xhtml";
        int startRow_rs_viewcat = Integer.parseInt(pg) * maxRows_rs_viewcat;

        ResultSet rs_viewcat = getCategoryFindList(startRow_rs_viewcat, "NO");
        ResultSet all_rs_viewcat = null;
        if (tt == null) {
            all_rs_viewcat = getCategoryFindList(startRow_rs_viewcat, "YES");
            tt = String.valueOf(findResultSetRows(all_rs_viewcat));
        }
        int totalPages_rs_viewcat = Math.round(Integer.parseInt(tt) / maxRows_rs_viewcat);

        String queryString_rs_viewcat = "";
        List<String> newParamterList = new LinkedList<>();
        if (null != serverParameterList) {
            for (String param : serverParameters) {
                if (!param.startsWith("pg=") && !param.startsWith("tt=")) {
                    newParamterList.add(param);
                }
            }
        }

        if (!newParamterList.isEmpty()) {
            queryString_rs_viewcat = "&" + StringFormatter.formatListToString(newParamterList, "&");
        }

        queryString_rs_viewcat = String.format("&tt=%d%s", Integer.parseInt(tt), queryString_rs_viewcat);

        //        System.out.println("pg..." + pg);
        //        System.out.println("tt..." + tt);
        //        System.out.println("currentPage..." + currentPage);
        //        System.out.println("maxRows_rs_viewcat..." + maxRows_rs_viewcat);
        //        System.out.println("startRow_rs_viewcat..." + startRow_rs_viewcat);
        //        System.out.println("totalPages_rs_viewcat..." + totalPages_rs_viewcat);
        String html = "";
        if (Integer.parseInt(tt) > 0) {
            html += "<form id=\"frmCatView\" name=\"frmCatView\" enctype=\"multipart/form-data\" method=\"post\" action=\"\">"
                    + "<label>  </label>"
                    + "<table border=\"0\">"
                    + "<tr>";
            html += "<td>";
            if (Integer.parseInt(pg) > 0) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, 0, queryString_rs_viewcat) + "\">First</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) > 0) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.max(0, Integer.parseInt(pg) - 1), queryString_rs_viewcat) + "\">Previous</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) < totalPages_rs_viewcat) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.min(totalPages_rs_viewcat, Integer.parseInt(pg) + 1), queryString_rs_viewcat) + "\">Next</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) < totalPages_rs_viewcat) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, totalPages_rs_viewcat, queryString_rs_viewcat) + "\">Last</a>";
            }
            html += "</td><td>";
            html += "Records " + (startRow_rs_viewcat + 1) + " to " + Math.min(startRow_rs_viewcat + maxRows_rs_viewcat, Integer.parseInt(tt)) + " of " + tt + "</td>";

            html += "<td><label><input type=\"submit\" name=\"button\" id=\"button\" value=\"Approve\" onclick=\"document.getElementById('frmaction').value='approve'\" /></label>";
            html += "<input type=\"submit\" name=\"button2\" id=\"button2\" value=\"Delete\" onclick=\"document.getElementById('frmaction').value='delete'; return confirm('Do you really want to delete this Item as it might delete other sub Item');\" />";
            html += "<input type=\"hidden\" name=\"frmaction\" id=\"frmaction\" />";
            html += "<input name=\"frmCont\" type=\"hidden\" id=\"frmCont\" value=\"category\" />";
            html += "<input name=\"chkprefix\" type=\"hidden\" id=\"chkprefix\" value=\"delchk\" /></td>";
            html += "</tr></table>";

            html += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"datatable\">";
            html += "<thead class=\"fixedHeader\"><tr>";
            html += "<th width=\"30%\" align=\"left\">Category Name</th>";
            html += "<th width=\"19%\" align=\"left\">Show</th>";
            html += "<th width=\"37%\" align=\"left\">Parent </th>";
            html += "<th width=\"14%\" align=\"center\">Action";
            html += "<input type=\"checkbox\" name=\"delchk_0\" id=\"delchk_0\" onchange=\"checkUncheck('frmCatView','delchk')\" /></th></tr></thead>";
            html += "<tbody>";

            while (rs_viewcat.next()) {
                html += "<tr>";
                html += "<td><a href=\"?page=category&amp;itemid=" + rs_viewcat.getString("category_id") + "\">" + rs_viewcat.getString("category_name") + "</a></td>";
                html += "<td>" + rs_viewcat.getString("is_published") + "</td>";
                html += "<td><a href=\"pages/category_update.xhtml?aj&amp;itemid=" + rs_viewcat.getString("parent_id") + "\" rel=\"facebox\">" + getCategoryName(rs_viewcat.getString("parent_id")) + "</a></td>";
                html += "<td align=\"center\"><label>";
                html += "<input type=\"checkbox\" name=\"delchk_" + rs_viewcat.getString("category_id") + " id=\"delchk_" + rs_viewcat.getString("category_id") + " value=\"" + rs_viewcat.getString("category_id") + "\"/>";
                html += "</label></td></tr>";
            }
            html += "</tbody></table></form>";

        } else {
            html += "<div id=\"error\">No Category Yet</div>";
        }
        return html;
    }

    public String categoryPageAdd() throws SQLException {
        initNonIndexPage();

        String currentPage = "index.xhtml";
        int parentid = 0;

        String html = "";

        if (CommonUtils.isset(aj)) {
            parentid = Integer.parseInt(pid);
        }

        ResultSet re_sublinks = getSubCategoryFind(1, 0, "YES");

        String it = "";
        if (CommonUtils.isset(pid)) {
            it = "&itemid=" + pid;
        }

        //        System.out.println("it..." + it);
        //        System.out.println("pid..." + pid);
        //        System.out.println("parentid..." + parentid);
        //        System.out.println("aj..." + aj);
        if (CommonUtils.isset(pid)) {
            html += "<form action=\"?page=category" + it + "\" method=\"POST\" enctype=\"multipart/form-data\" name=\"frmcatadd\" id=\"frmcatadd\">"
                    + "<table align=\"center\" cellpadding=\"4\">"
                    + "<tr valign=\"baseline\">"
                    + "<td width=\"95\" align=\"right\" nowrap=\"nowrap\">Category name:</td>"
                    + "<td width=\"307\"><input type=\"text\" name=\"categoryname\" value=\"\" size=\"32\"/></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Publish:</td>"
                    + "<td><select name=\"ispublished\">"
                    + "<option value=\"yes\" " + (!"".equals("yes") ? "SELECTED" : "") + ">Yes</option>"
                    + "<option value=\"no\" " + (!"".equals("no") ? "SELECTED" : "") + ">No</option>"
                    + "</select></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Order By </td>"
                    + "<td><select name=\"listtype\" id=\"listtype\" >";

            while (re_sublinks.next()) {
                html += "<option value=\"" + re_sublinks.getString("category_id") + "\"" + (!"".equals(re_sublinks.getString("category_id")) ? "SELECTED>" : ">") + re_sublinks.getString("category_name") + "</option>";
            }

            html += "</select></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Direct link</td>"
                    + "<td><input type=\"checkbox\" name=\"isDirect\" id=\"isDirect\" onclick=\"if(this.checked) showdiv('directlink') else hide('directlink');\" />{Link page directly to another page}"
                    + "<input name=\"directlink\" type=\"text\" id=\"directlink\" value=\"http://\" class=\"none\"/></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td align=\"right\" valign=\"top\" nowrap=\"nowrap\">Category Description</td>"
                    + "<td><textarea name=\"description\" id=\"pcontent\" ></textarea></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td align=\"right\" valign=\"top\" nowrap=\"nowrap\">Content:</td>"
                    + "<td><textarea name=\"pcontent\" id=\"textarea2\" cols=\"45\" rows=\"5\" ></textarea>"
                    + "<script language=\"javascript1.2\">"
                    + "generate_wysiwyg('textarea2');</script>"
                    + "<input type=\"hidden\" name=\"parentid\" id=\"parentid\" value=\"" + parentid + "\" /></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Allow Comment</td>"
                    + "<td><select name=\"allowcomment\">"
                    + "<option value=\"yes\" " + (!"".equals("1") ? "SELECTED" : "") + ">Yes</option>"
                    + "<option value=\"no\" " + (!"".equals("0") ? "SELECTED" : "") + ">No</option>"
                    + "</select></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">&nbsp;</td>"
                    + "<td><input type=\"submit\" value=\"Add Category\" /></td></tr>"
                    + "</table>"
                    + "<input type=\"hidden\" name=\"MM_insert\" value=\"frmcatadd\" />"
                    + "</form>";
        } else {
            html += "<style type=\"text/css\">"
                    + "#divdetails{display: none;}"
                    + "</style>";
        }

        return html;
    }

    public String categoryPageUpdate() throws SQLException {
        //MM_update=frmcatupdate&categoryid=221&categoryname=The+decisions+of+the+Examination+Board&listtype=131&ispublished=no&description=null&allowcomment=no&rss=yes&categoryicon=null&parentid=132
        initNonIndexPage();

        String mhtml = "";

        int colname_rs_up = -1;
        ResultSet re_sublinks = getSubCategoryFind(1, 0, "YES");

        if ("content".equals(page)) {
            colname_rs_up = row_rs_up.getInt("categoryid");
            itemid = row_rs_up.getString("categoryid");
        } else if (null != itemid || !"".equals(itemid)) {
            colname_rs_up = Integer.parseInt(itemid);
            //itemid = itemId;
        }

        ResultSet row_rs_cat_up = getCategoryDetails(colname_rs_up);

        //System.out.println("row_rs_cat_up.." + row_rs_cat_up.);
        //int totalRows_rs_up = findResultSetRows(row_rs_cat_up);
        if (row_rs_cat_up.next()) {
            parentid = row_rs_cat_up.getInt("category_id");
        }

        if (pid != null) {
            itemid = pid;
        }

        String f = "facebox";
        if (CommonUtils.isset(aj)) {
            f = "facebox1";
        }

        mhtml += "<div id=\"ptitle\">" + StringEscapeUtils.escapeHtml(row_rs_cat_up.getString("category_name")) + "</div>"
                + "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\">"
                + "<tr><td width=\"63%\" valign=\"top\">"
                + "<form action=\"?page=category&amp;itemid=" + itemid + "\" enctype=\"multipart/form-data\" method=\"post\" name=\"form1\" id=\"form1\">"
                + "<input type=\"hidden\" name=\"MM_update\" value=\"frmcatupdate\" />"
                + "<input type=\"hidden\" name=\"categoryid\" value=\"" + row_rs_cat_up.getInt("category_id") + "\" />"
                + "<a href=\"pages/category_add.xhtml?aj&amp;pid=" + row_rs_cat_up.getInt("category_id") + "\" rel=\"" + f + "\" >Add sublinks</a>"
                + "<table align=\"center\">"
                + "<tr valign=\"baseline\">"
                + "<td width=\"129\" align=\"left\" nowrap=\"nowrap\">Category id:</td>"
                + "<td width=\"275\">" + row_rs_cat_up.getInt("category_id") + "</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">Category Name:</td>"
                + "<td><input type=\"text\" name=\"categoryname\" value=\"" + StringEscapeUtils.escapeHtml(row_rs_cat_up.getString("category_name")) + "\" size=\"32\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">List Type</td>"
                + "<td><select name=\"listtype\" id=\"listtype\" >";

        while (re_sublinks.next()) {
            mhtml += "<option value=\"" + re_sublinks.getInt("category_id") + "\" " + (re_sublinks.getInt("category_id") == row_rs_cat_up.getInt("list_type") ? "SELECTED" : "") + ">" + re_sublinks.getString("category_name") + "</option>";
        }

        mhtml += "</select></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">Published:</td>"
                + "<td><label><select name=\"ispublished\" id=\"ispublished\">"
                + "<option value=\"yes\" " + ("yes".equals(row_rs_cat_up.getString("is_published")) ? "SELECTED" : "") + ">Yes</option>"
                + "<option value=\"no\" " + ("no".equals(row_rs_cat_up.getString("is_published")) ? "SELECTED" : "") + ">No</option>"
                + "</select></label></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td align=\"left\" valign=\"top\" nowrap=\"nowrap\">Category Description</td>"
                + "<td><label>"
                + "<textarea name=\"description\" id=\"description\">" + StringEscapeUtils.escapeHtml(row_rs_cat_up.getString("description")) + "</textarea></label></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">Allow Comment</td>"
                + "<td><select name=\"allowcomment\">"
                + "<option value=\"yes\" " + ("yes".equals(row_rs_cat_up.getString("allow_comment")) ? "SELECTED" : "") + ">Yes</option>"
                + "<option value=\"no\" " + ("no".equals(row_rs_cat_up.getString("allow_comment")) ? "SELECTED" : "") + ">No</option>"
                + "</select></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">RSS</td>"
                + "<td><select name=\"rss\">"
                + "<option value=\"yes\" " + ("yes".equals(row_rs_cat_up.getString("is_rss")) ? "SELECTED" : "") + ">Yes</option>"
                + "<option value=\"no\" " + ("no".equals(row_rs_cat_up.getString("is_rss")) ? "SELECTED" : "") + ">No</option>"
                + "</select></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">Category icon:</td>"
                + "<td><input type=\"text\" name=\"categoryicon\" value=\"" + StringEscapeUtils.escapeHtml(row_rs_cat_up.getString("category_icon")) + "\" size=\"32\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">Parent:</td>"
                + "<td><select name=\"parentid\" id=\"parentid\">"
                + "<option value=\"0\" ";

        String p = "";

        if (0 == row_rs_cat_up.getInt("parent_id")) {
            mhtml += "SELECTED" + ">None</option>";
            p = "None";
        }

        ResultSet rs = loadRelatedParent(row_rs_cat_up.getInt("parent_id"));

        while (rs.next()) {
            mhtml += "<option value=\"" + rs.getInt("category_id") + "\"";
            if (rs.getInt("category_id") == row_rs_cat_up.getInt("parent_id")) {
                mhtml += " SELECTED ";
                p = rs.getString("category_name");
            }

            mhtml += ">" + rs.getString("category_name") + "</option>";
        }

        mhtml += "</select><a href=\"pages/category_update.xhtml?aj&amp;itemid=" + row_rs_cat_up.getInt("parent_id") + "\" rel=\"facebox\">" + p + "</a></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"left\">&nbsp;</td>"
                + "<td><input type=\"submit\" value=\"Update record\" /></td></tr>"
                + "</table></form></td>"
                + "<td width=\"37%\" valign=\"top\">"
                + sublinkPage();
        mhtml += "</td>"
                + "</tr></table>";

        return mhtml;

    }

    public String sublinkPage() throws SQLException {
        initNonIndexPage();
        String currentPage = "index.xhtml";

        String f = "facebox";
        if (CommonUtils.isset(aj)) {
            f = "facebox1";
        }

        if (!CommonUtils.isset(pg)) {
            pg = "0";
        }

        int startRow_rs_sublinks = Integer.parseInt(pg) * maxRows_rs_viewcat;

        ResultSet rs_sublinks = getSubCategoryFind(parentid, startRow_rs_sublinks, "NO");
        ResultSet all_rs_sublinks = null;

        String ttt = null;

        if (tt == null) {
            all_rs_sublinks = getSubCategoryFind(parentid, startRow_rs_sublinks, "YES");
            ttt = String.valueOf(findResultSetRows(all_rs_sublinks));
        } else {
            ttt = "0";
        }

        int totalPages_rs_sublinks = Math.round(Integer.parseInt(ttt) / maxRows_rs_viewcat) - 1;

        String queryString_rs_sublinks = "";
        List<String> newParamterLists = new LinkedList<>();
        if (null != serverParameterList) {
            for (String param : serverParameters) {
                if (!param.startsWith("pg=") && !param.startsWith("tt=")) {
                    newParamterLists.add(param);
                }
            }
        }

        if (!newParamterLists.isEmpty()) {
            queryString_rs_sublinks = "&" + StringFormatter.formatListToString(newParamterLists, "&");
        }

        queryString_rs_sublinks = String.format("&tt=%d%s", Integer.parseInt(ttt), queryString_rs_sublinks);

        String shtml = "";
        if (Integer.parseInt(ttt) > 0) {
            shtml += "<table border=\"0\">"
                    + "<tr>";
            if (Integer.parseInt(pg) > 0) {
                shtml += "<td><a href=\"" + String.format("%s?pg=%d%s", currentPage, 0, queryString_rs_sublinks) + "\">First</a></td>";
            }
            if (Integer.parseInt(pg) > 0) {
                shtml += "<td><a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.max(0, Integer.parseInt(pg) - 1), queryString_rs_sublinks) + "\">Previous</a></td>";
            }
            if (Integer.parseInt(pg) < totalPages_rs_sublinks) {
                shtml += "<td><a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.min(totalPages_rs_sublinks, Integer.parseInt(pg) + 1), queryString_rs_sublinks) + "\">Next</a></td>";
            }
            if (Integer.parseInt(pg) < totalPages_rs_sublinks) {
                shtml += "<td><a href=\"" + String.format("%s?pg=%d%s", currentPage, totalPages_rs_sublinks, queryString_rs_sublinks) + "\">Last</a></td>";
            }
            shtml += "<td>Records " + (startRow_rs_sublinks + 1) + " to " + Math.min(startRow_rs_sublinks + maxRows_rs_viewcat, Integer.parseInt(ttt)) + " of " + ttt + "</td></tr></table>";

            shtml += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"allround\" id=\"datatable\">";
            shtml += "<tr>";
            shtml += "<td class=\"action\">categoryid</td>";
            shtml += "<td class=\"action\">categoryname</td>";
            shtml += "<td class=\"action\">&nbsp;</td></tr>";

            while (rs_sublinks.next()) {
                shtml += "<tr>";
                shtml += "<td>" + rs_sublinks.getString("category_id") + "</td>";
                shtml += "<td><a href=\"pages/category_update.xhtml?aj&amp;itemid=" + rs_sublinks.getString("category_id") + "&amp;pid=" + rs_sublinks.getString("parent_id") + "\" rel=\"" + f + "\">" + rs_sublinks.getString("category_name") + "</a></td>";
                shtml += "<td><a href=\"?page=content&amp;itemid=" + rs_sublinks.getString("content_id") + "&amp;pid=" + rs_sublinks.getString("category_id") + "\">pages</a></td></tr>";
            }
            shtml += "</table>";

        }

        return shtml;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Content Pages">

    public String contentPageAdd() throws SQLException {
        initNonIndexPage();
        String html = "";

        String qString = serverQueryString;

        ResultSet rs_resources = null;
        int totalRows_rs_resources = 0;
        int ccid = 0;
        if (CommonUtils.isset(cid)) {
            ccid = Integer.parseInt(cid);
            rs_resources = getContentDetails(ccid);
            totalRows_rs_resources = findResultSetRows(rs_resources);

            html += "<div id='ptitle'>Adding Items to " + rs_resources.getString("ptitle") + "</div>"
                    + "<form action=\"?" + qString + "\" enctype=\"multipart/form-data\" method=\"post\" name=\"frmaddcontent\" id=\"frmaddcontent\">"
                    + "<table align=\"center\">"
                    + "<tr><td width=\"50%\">"
                    + "<table>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Keywords:</td>"
                    + "<td><input type=\"text\" name=\"keywords\" value=\"\" size=\"32\" /></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Title:</td>"
                    + "<td><input type=\"text\" name=\"ptitle\" value=\"\" size=\"32\" /></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">Source:</td>"
                    + "<td><input type=\"text\" name=\"source\" value=\"\" size=\"32\" /></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td align=\"right\" valign=\"top\" nowrap=\"nowrap\">Content:</td>"
                    + "<td><textarea name=\"pcontent\" cols=\"32\" id=\"textarea2\"></textarea> <script language=\"javascript1.2\">"
                    + "generate_wysiwyg('textarea2');"
                    + "</script></td>"
                    + "<tr valign=\"baseline\">"
                    + "<td>Show Page</td><td><label>"
                    + "<select name=\"ispublished\" id=\"ispublished\">"
                    + "<option value=\"yes\">Yes</option>"
                    + "<option value=\"no\">No</option>"
                    + "</select></label></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td>Page Banner:</td>"
                    + "<td>";

            ResultSet rs_resources1 = getSiteBanners();
            int totalRows_rs_resources1 = findResultSetRows(rs_resources1);

            while (rs_resources1.next()) {
                html += "<input type=\"radio\" name=\"pbanner\" value=\"" + rs_resources1.getString("resourceid") + "\" id=\"grp_" + rs_resources1.getString("resourceid") + "\" " + " />" //+ (row_rs_up.getString("pbanner").equals(rs_resources1.getString("resourceid")) ? "checked='checked'" : "")
                        + "<a href=\"../resources/" + rs_resources1.getString("resourceurl") + "\" rel=\"facebox\" >"
                        + "<img name=\"\" src=\"../resources/thump.xhtml?im=" + rs_resources1.getString("resourceurl") + "\" alt=\"\" /></a>";
            }
            html += "<input type=\"radio\" name=\"pbanner\" value=\"\" />None"
                    + "</td></tr>";

            ccid = 0;
            if (CommonUtils.isset(cid)) {
                ccid = Integer.parseInt(cid);
            }

            ResultSet r = getListType(ccid);
            if (r.next()) {
            }

            if (3 == r.getInt("list_type") || 2 == r.getInt("list_type")) {

                html += "<tr valign=\"baseline\"><td>" + (3 == r.getInt("list_type") ? "Starts On" : "Date Created:") + "</td>"
                        + "<td>"
                        + "<script>var BASE='../';"
                        + "var cdate =\"\";"
                        + "var yr = " + (new SimpleDateFormat("yyyy")).format(new Date()) + "; "
                        + "var d = " + (new SimpleDateFormat("d")).format(new Date()) + "; "
                        + "var m = " + (new SimpleDateFormat("M")).format(new Date()) + "; "
                        + "cdate = yr+\"-\"+m+\"-\"+d;"
                        + "DateInput('datecreated', true, 'YYYY-MM-DD',cdate);</script>"
                        + "<select name=\"tstartH\" class=\"small\">";

                String h = (new SimpleDateFormat("h")).format(new Date());
                String m = (new SimpleDateFormat("mm")).format(new Date());

                html += CommonUtils.createComboList(24, h)
                        + "</select> Hr"
                        + "<select name=\"tstartM\"  class=\"small\">"
                        + CommonUtils.createComboList(60, m)
                        + "</select>Min"
                        + "(Time eg 18:00)</td></tr>";

                html += "<tr valign=\"baseline\"><td>" + (3 == r.getInt("list_type") ? "End On" : "Last modified:") + "</td>"
                        + "<td>"
                        + "<script>var BASE='../';"
                        + "var cdate =\"\";"
                        + "var yr = " + (new SimpleDateFormat("yyyy")).format(new Date()) + "; "
                        + "var d = " + (new SimpleDateFormat("d")).format(new Date()) + "; "
                        + "var m = " + (new SimpleDateFormat("M")).format(new Date()) + "; "
                        + "cdate = yr+\"-\"+m+\"-\"+d;"
                        + "DateInput('lastmodified', true, 'YYYY-MM-DD',cdate);</script>"
                        + "<select name=\"tendH\" class=\"small\">";

                h = (new SimpleDateFormat("h")).format(new Date());
                m = (new SimpleDateFormat("mm")).format(new Date());

                html += CommonUtils.createComboList(24, h)
                        + "</select> Hr"
                        + "<select name=\"tendM\"  class=\"small\">"
                        + CommonUtils.createComboList(60, m)
                        + "</select>Min"
                        + "(Time eg 18:00)</td></tr>";
            }

            html += "<tr valign=\"baseline\">"
                    + "<td colspan=\"2\" align=\"right\" valign=\"top\" nowrap=\"nowrap\"><input type=\"hidden\" name=\"listtype\" id=\"listtype\" value=\"" + r.getInt("list_type") + "\" /></td></tr>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">&nbsp;</td>"
                    + "<td><input type=\"submit\" value=\"Insert record\" /></td></tr>"
                    + "</table>"
                    + "<td>"
                    + "<td width=\"50%\" valign=\"top\">"
                    + "<table>"
                    + "<tr valign=\"baseline\">"
                    + "<td nowrap=\"nowrap\" align=\"right\">"
                    + "<input name=\"categoryid\" type=\"hidden\" id=\"categoryid\" value=\"0" + ccid + "\" /></td>"
                    + "<td>" + images() + "&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td></tr>"
                    + "</table>"
                    + "<input type=\"hidden\" name=\"MM_insert\" value=\"frmContentAdd\" />"
                    + "</form>"
                    + similarContent(rs_resources)
                    + "";
        } else {
            html += "<style type=\"text/css\">"
                    + "#divdetails{display: none;}"
                    + "</style>";
        }

        return html;

    }

    public String contentPageView() throws SQLException {
        initNonIndexPage();

        String currentPage = "index.xhtml";

        int pgnums = 0;
        if (CommonUtils.isset(pgnum)) {
            pgnums = Integer.parseInt(pgnum);
        }

        int startRow_rs_cateory = pgnums * maxRows_rs_viewcat;

        ResultSet rs_category = getContentList(startRow_rs_cateory, "NO");
        ResultSet all_rs_cateory = null;
        if (ttnum == null) {
            all_rs_cateory = getContentList(startRow_rs_cateory, "YES");
            ttnum = String.valueOf(findResultSetRows(all_rs_cateory));
        }
        int totalPages_rs_cateory = Math.round(Integer.parseInt(ttnum) / maxRows_rs_viewcat);

        String queryString_rs_cateory = "";
        List<String> newParamterList = new LinkedList<>();
        if (null != serverParameterList) {
            for (String param : serverParameters) {
                if (!param.startsWith("pgnum=") && !param.startsWith("ttnum=")) {
                    newParamterList.add(param);
                }
            }
        }

        if (!newParamterList.isEmpty()) {
            queryString_rs_cateory = "&" + StringFormatter.formatListToString(newParamterList, "&");
        }

        queryString_rs_cateory = String.format("&ttnum=%d%s", Integer.parseInt(ttnum), queryString_rs_cateory);

        String html = "";
        if (Integer.parseInt(ttnum) > 0) {
            html += "<form id=\"frmcontentview\" name=\"frmcontentview\" enctype=\"multipart/form-data\" method=\"post\" action=\"\">"
                    + "<table border=\"0\">"
                    + "<tr><td>";
            if (pgnums > 0) {
                html += "<a href=\"" + String.format("%s?pgnum=%d%s", currentPage, 0, queryString_rs_cateory) + "\">First</a>";
            }
            html += "</td><td>";
            if (pgnums > 0) {
                html += "<a href=\"" + String.format("%s?pgnum=%d%s", currentPage, Math.max(0, pgnums - 1), queryString_rs_cateory) + "\">Previous</a>";
            }
            html += "</td><td>";
            if (pgnums < totalPages_rs_cateory) {
                html += "<a href=\"" + String.format("%s?pgnum=%d%s", currentPage, Math.min(totalPages_rs_cateory, pgnums + 1), queryString_rs_cateory) + "\">Next</a>";
            }
            html += "</td><td>";
            if (pgnums < totalPages_rs_cateory) {
                html += "<a href=\"" + String.format("%s?pgnum=%d%s", currentPage, totalPages_rs_cateory, queryString_rs_cateory) + "\">Last</a>";
            }
            html += "</td><td>";
            html += "Records " + (startRow_rs_cateory + 1) + " - " + Math.min(startRow_rs_cateory + maxRows_rs_viewcat, Integer.parseInt(ttnum)) + " of " + ttnum + "</td>";

            html += "<td><label><input type=\"submit\" name=\"button\" id=\"button\" value=\"Approve\" onclick=\"document.getElementById('frmaction').value='approve'\" /></label>";
            html += "<input type=\"submit\" name=\"button2\" id=\"button2\" value=\"Delete\" onclick=\"document.getElementById('frmaction').value='delete'; return confirm('Do you really want to delete this Item as it might delete other sub Item');\" />";
            html += "<input type=\"hidden\" name=\"frmaction\" id=\"frmaction\" />";
            html += "<input name=\"frmCont\" type=\"hidden\" id=\"frmCont\" value=\"content\" />";
            html += "<input name=\"chkprefix\" type=\"hidden\" id=\"chkprefix\" value=\"delchk\" /></td>";
            html += "</tr></table>";

            html += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"datatable\">";
            html += "<tr align=\"left\">";
            html += "<th>Page Title</th>";
            html += "<th>Parent</th>";
            html += "<th>Show Page</th>";
            html += "<th>&nbsp;</th>";
            html += "<th>Action";
            html += "<input type=\"checkbox\" name=\"delchk_0\" id=\"delchk_0\" onchange=\"checkUncheck('frmcontentview','delchk')\" /></th></tr>";
            html += "<tbody>";

            while (rs_category.next()) {
                html += "<tr>";
                html += "<td><a href=\"?page=content&amp;itemid=" + rs_category.getString("content_id") + "\">" + rs_category.getString("ptitle") + "</a></td>";

                ResultSet rp = loadRelatedContentParent(rs_category.getInt("categoryid"), rs_category.getString("cattype"));

                if (rp.next()) {
                    html += "<td><a href=\"\">" + shortenContent(rp.getString("title"), 20) + "</a></td>";
                }

                html += "<td>" + CommonUtils.ucfirst(rs_category.getString("ispublished")) + "</td>";
                html += "<td><a href=\"?page=content&amp;cid=" + rs_category.getString("content_id") + "\">add Page</a></td>";
                html += "<td>";
                html += "<input type=\"checkbox\" name=\"delchk_" + rs_category.getString("content_id") + " id=\"delchk_" + rs_category.getString("content_id") + " value=\"" + rs_category.getString("content_id") + "\"/>";
                html += "</td></tr>";
            }
            html += "</table></form>";

        } else {
            html += "<div id=\"error\">No Content Yet</div>";
        }
        return html;
    }

    public String contentPageUpdate() throws SQLException {
        //keywords=&ptitle=The+Story+of+the+man&isDirect=on&directlink=null&pcontent=The+man+is+all+i+know<br>&ispublished=yes&categoryid=210&source=null&MM_update=frmcontentUpdate&contentid=5218&textfield=
        initNonIndexPage();

        String qString = serverQueryString;

        int colname_rs_up = -1;
        if (CommonUtils.isset(itemid)) {
            colname_rs_up = Integer.parseInt(itemid);
        }

        ResultSet rs_up = getContentDetails(colname_rs_up);
        int totalRows_rs_up = findResultSetRows(rs_up);

        int colname_rs_resources = -1;
        if (CommonUtils.isset(itemid)) {
            colname_rs_resources = Integer.parseInt(itemid);
        }

        ResultSet rs_resources = getContentResource(colname_rs_resources);
        int totalRows_rs_resources = findResultSetRows(rs_resources);

        String cat = rs_up.getString("categoryid");
        if (cat.length() < 1) {
            cat = "0";
        }

        row_rs_up = rs_up;

        String html = "";
        if ("cat".equals(rs_up.getString("cattype"))) {
            html += "<ul id=\"contentCategory\" class=\"shadetabs\" style=\"margin-top: 5px;\">"
                    + "<li><a href=\"#\" rel=\"tabcontentM\">Content</a></li>"
                    + "<li><a href=\"#\" rel=\"tabcategory\">Category</a></li></ul>"
                    + "<div class=\"allround\" style=\"margin-left: 2px;margin-right: 5px\">"
                    + "<div id=\"tabcontentM\" class=\"tabcontent\">";
        }
//enctype=\"multipart/form-data\" 
        html += "<div><a href=\"?page=content&amp;cid=" + itemid + "\"  class=\"add\">Add page</a></div>"
                + "<form action=\"?" + qString + "\" enctype=\"multipart/form-data\" method=\"POST\" name=\"frmcontentUpdate\" id=\"frmcontentUpdate\">"
                + "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\">"
                + "<tr><td width=\"50%\" valign=\"top\">"
                + "<table width=\"99%\" align=\"center\">"
                + "<tr valign=\"baseline\">"
                + "<td width=\"25%\">Page ID :</td>"
                + "<td width=\"75%\"><strong>" + rs_up.getString("content_id") + "</strong></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td>Keywords:</td>"
                + "<td><input type=\"text\" name=\"keywords\" value=\"" + StringEscapeUtils.escapeHtml(rs_up.getString("keywords")) + "\" size=\"32\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td>Page title:</td>"
                + "<td><input type=\"text\" name=\"ptitle\" value=\"" + StringEscapeUtils.escapeHtml(rs_up.getString("ptitle")) + "\" size=\"32\" /></td>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Direct link</td>"
                + "<td><input type=\"checkbox\" name=\"isDirect\" id=\"isDirect\" onclick=\"if(this.checked) showdiv('directlink') else hide('directlink');\" ";

        String t = "none";
        if (null != rs_up.getString("directlink") && rs_up.getString("directlink").length() > 10) {
            t = "block";
        }

        html += "checked='checked'/>{Link page directly to another page}<div>"
                + "<input name=\"directlink\" type=\"text\" id=\"directlink\" value=\"" + rs_up.getString("directlink") + "\" class=\"" + t + "\" /></td></tr>"
                + "<tr valign=\"baseline\"><td colspan=\"2\">Page text:</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td colspan=\"2\"><textarea name=\"pcontent\"  id=\"textarea2\">" + StringEscapeUtils.escapeHtml(rs_up.getString("pcontent")) + "</textarea><script language=\"javascript1.2\">"
                + "generate_wysiwyg('textarea2');</script></td></tr>"
                + "<tr valign=\"baseline\"><td>Page Banner:</td>"
                + "<td>";

        ResultSet rs_resources1 = getSiteBanners();
        int totalRows_rs_resources1 = findResultSetRows(rs_resources1);

        do {
            html += "<input type=\"radio\" name=\"pbanner\" value=\"" + rs_resources1.getInt("resourceid") + "\" id=\"grp_" + rs_resources1.getInt("resourceid") + "\"" + (rs_resources1.getInt("resourceid") == rs_up.getInt("pbanner") ? "checked='checked'" : "") + " />"
                    + "<a href=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resources1.getString("resourceurl") + "\" rel=\"facebox\" >"
                    + "<img name=\"\" src=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resources1.getString("resourceurl") + "\" alt=\"\" style='width:50px;height:20px'/></a>";
//                                + "<img name=\"\" src=\"../resources/thump.xhtml?im=" + rs_resources1.getString("resourceurl") + "\" alt=\"\" style='width:20px;height:10px'/></a>";
        } while (rs_resources1.next());
        html += "<input type=\"radio\" name=\"pbanner\" value=\"\" />None</td></tr>"
                + "<tr valign=\"baseline\"><td>Show Page:</td>"
                + "<td><select name=\"ispublished\">"
                + "<option value=\"yes\" " + ("yes".equals(rs_up.getString("ispublished")) ? "SELECTED" : "") + ">Yes</option>"
                + "<option value=\"no\" " + ("no".equals(rs_up.getString("ispublished")) ? "SELECTED" : "") + ">No</option>"
                + "</select>" + rs_up.getString("ispublished")
                + "</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td>Parent Category:</td>";

        cat = rs_up.getString("categoryid");

        ResultSet rsrow = loadRelatedContentParent(Integer.parseInt(cat), rs_up.getString("cattype"));

        if (rsrow.next()) {
        }

        html += "<td>"
                + "<select id=\"categoryid\" name=\"categoryid\">"
                + "<option value=\"0\">None</option>"
                + "<option value=\"" + rsrow.getInt("id") + "\"" + (Integer.parseInt(cat) == rsrow.getInt("id") ? "selected='selected'" : "") + ">" + rsrow.getString("title") + "</option>"
                + "</select></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td>Source</td>"
                + "<td><input type=\"text\" name=\"source\" value=\"" + (null == rs_up.getString("source") || "".equals(rs_up.getString("source")) || "null".equals(rs_up.getString("source")) ? "" : StringEscapeUtils.escapeHtml(rs_up.getString("source"))) + "\" size=\"32\" /></td></tr>";

        ResultSet r = getListType(Integer.parseInt(cat));
        if (r.next()) {
        }

        if (3 == r.getInt("list_type") || 2 == r.getInt("list_type")) {

            html += "<tr valign=\"baseline\"><td>" + (3 == r.getInt("list_type") ? "Starts On" : "Date Created:") + "</td>"
                    + "<td>"
                    + "<script>var BASE='../';"
                    + "var cdate =\"" + rs_up.getString("datecreated").substring(0, 10) + "\";"
                    + "var yr = " + (new SimpleDateFormat("yyyy")).format(new Date()) + "; "
                    + "var d = " + (new SimpleDateFormat("d")).format(new Date()) + "; "
                    + "var m = " + (new SimpleDateFormat("M")).format(new Date()) + "; "
                    + "DateInput('datecreated', true, 'YYYY-MM-DD',cdate)</script>"
                    + "<select name=\"tstartH\" class=\"small\">";

            String h = rs_up.getString("datecreated").substring(11, 2 + 11);
            String m = rs_up.getString("datecreated").substring(14, 2 + 14);

            html += CommonUtils.createComboList(24, h)
                    + "</select> Hr"
                    + "<select name=\"tstartM\"  class=\"small\">"
                    + CommonUtils.createComboList(60, m)
                    + "</select>Min"
                    + "(Time eg 18:00)</td></tr>";

            html += "<tr valign=\"baseline\"><td>" + (3 == r.getInt("list_type") ? "End On" : "Last modified:") + "</td>"
                    + "<td>"
                    + "<script>var BASE='../';"
                    + "var cdate =\"" + rs_up.getString("datecreated").substring(0, 10) + "\";"
                    + "var yr = " + (new SimpleDateFormat("yyyy")).format(new Date()) + "; "
                    + "var d = " + (new SimpleDateFormat("d")).format(new Date()) + "; "
                    + "var m = " + (new SimpleDateFormat("M")).format(new Date()) + "; "
                    + "DateInput('lastmodified', true, 'YYYY-MM-DD',cdate);</script>"
                    + "<select name=\"tendM\" class=\"small\">";

            h = null == rs_up.getString("lastmodified") ? "0" : rs_up.getString("lastmodified").substring(11, 2 + 11);
            m = null == rs_up.getString("lastmodified") ? "0" : rs_up.getString("lastmodified").substring(14, 2 + 14);

            html += CommonUtils.createComboList(24, h)
                    + "</select> Hr"
                    + "<select name=\"tstartM\"  class=\"small\">"
                    + CommonUtils.createComboList(60, m)
                    + "</select>Min"
                    + "(Time eg 18:00)</td></tr>";
        }

        html += "<tr valign=\"baseline\">"
                + "<td colspan=\"2\">&nbsp;</td></tr>"
                + "<tr valign=\"baseline\"><td colspan=\"2\"></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td colspan=\"2\"><input type=\"submit\" value=\"Update record\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td colspan=\"2\">&nbsp;</td></tr></table>"
                + "<input type=\"hidden\" name=\"MM_update\" value=\"frmcontentUpdate\" />"
                + "<input type=\"hidden\" name=\"contentid\" value=\"" + rs_up.getString("content_id") + "\" /></td>"
                + "<td width=\"50%\" valign=\"top\">"
                + "<ul id=\"resourceRelated\" class=\"shadetabs\">"
                + "<li><a href=\"#\" rel=\"tcontent5\" class=\"selected\">Resources </a></li>"
                + "<li><a href=\"#\" rel=\"tcontent6\">Related Pages</a></li>"
                + "<li><a href=\"#\" rel=\"tcontentsubpage\" class=\"selected\">Sub Pages </a></li>"
                + "<li><a href=\"#\" rel=\"tcontentcomment\">Page Comments</a></li>"
                + "</ul>"
                + "<div class=\"allround\">"
                + "<div id=\"tcontent5\" class=\"tabcontent\" style=\"padding:5px\">";

        if (totalRows_rs_resources > 0) {
            html += "No of Resources: <strong class=\"redtxt\">" + totalRows_rs_resources + "</strong>"
                    + "<div id=\"res_action\"></div>"
                    + "<div class=\"data_list\">"
                    + "<table width=\"99%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" id=\"datatable\">"
                    + "<tr><th>category</th>"
                    + "<th>Description</th>"
                    + "<th>Click</th>"
                    + "<th>Pos</th></tr>";

            do {
                html += "<tr><td>"
                        + ("Images".equals(rs_resources.getString("category_name")) ? "<a href=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resources.getString("resourceurl") + "\" rel=\"facebox\" ><img name=\"\" src=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resources.getString("resourceurl") + "\" alt=\"\" style='width:50px;height:30px'/></a>" : "")
                        + rs_resources.getString("category_name")
                        + "</td>"
                        + "<td><a href=\"pages/resource_edit.xhtml?aj&itemid=" + rs_resources.getString("resourceid") + "\" rel=\"facebox\" title=\"Edit : " + rs_resources.getString("resourcedesc") + "\">" + ((null != rs_resources.getString("resourcedesc") || !"null".equalsIgnoreCase(rs_resources.getString("resourcedesc"))) ? rs_resources.getString("resourcedesc") : "No description") + " [edit]</a></td>"
                        + "<td>"
                        + "<input type=\"button\" name=\"button\" id=\"del" + rs_resources.getString("resourceid") + "\" value=\"del\" onclick=\"processResourceDeleteClick('del" + rs_resources.getString("resourceid") + "'," + rs_resources.getString("resourceid") + "," + rs_up.getString("content_id") + ",'d','res_action');\" />"
                        + "</td>"
                        + "<td><input name=\"resoursepos" + rs_resources.getString("resourceid") + "\" type=\"text\" id=\"rpos" + rs_resources.getString("resourceid") + "\" onclick=\"\" value=\"" + rs_resources.getString("resourceorder") + "\" size=\"3\" />"
                        + "</td></tr>";

            } while (rs_resources.next());

            html += "</table></div>";
        } else {
            html += "<span class=\"redtxt\">No Resource Found</span> ";
        }
        html += "<div id=\"data_list\">"
                + images()
                + "</div></div>"
                + "<div id=\"tcontent6\" class=\"tabcontent\">"
                + relatedpages(rs_up)
                + "<div id=\"relpage\">"
                + "<div class=\"action\" style=\"border-top:solid 1px #CCC\">Related Page</div>"
                + "<input type=\"text\" name=\"textfield\" id=\"textfield\" onkeyup=\"getData('pages/searchrelatedpage.xhtml?aj&rel='+this.value,'srelatedpage');\" />(Please type the page title of related page)"
                + "<div id=\"srelatedpage\"></div></div></div>"
                + "<div id=\"tcontentcomment\" class=\"tabcontent\">"
                + "<div id=\"relpage\">"
                + page_comments()
                + "</div></div>"
                + "<div id=\"tcontentsubpage\" class=\"tabcontent\">"
                + "<div id=\"relpage\">"
                + subpages(rs_resources)
                + "</div></div></div>"
                + "</td></tr></table></form>"
                + "";

        if ("cat".equals(rs_up.getString("cattype"))) {
            html += "</div>";
            html += "<div class=\"tabcontent\" id=\"tabcategory\">"
                    + categoryPageUpdate()
                    + "</div></div>";
        }

        html += "<script type=\"text/javascript\">"
                + "";

        if ("cat".equals(rs_up.getString("cattype"))) {
            html += "var myflowers=new ddtabcontent(\"contentCategory\");"
                    + "myflowers.setpersist(true);"
                    + "myflowers.setselectedClassTarget(\"link\");"
                    + "myflowers.init();";
        }

        html += "var mytab=new ddtabcontent(\"resourceRelated\");"
                + "mytab.setpersist(true);"
                + "mytab.setselectedClassTarget(\"link\");"
                + "mytab.init();"
                + "</script>";
        return html;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Resource Pages">

    public String resourcePageView() throws SQLException {
        String currentPage = "index.xhtml";
        int maxRows_rs_resource = 50;
        int pageNum_rs_resource_sm = 0;

        if (CommonUtils.isset(pageNum_rs_resource)) {
            pageNum_rs_resource_sm = Integer.parseInt(pageNum_rs_resource);
        }

        int startRow_rs_resource = pageNum_rs_resource_sm * maxRows_rs_resource;

        ResultSet rs_resource = getResourceListDesc(startRow_rs_resource, "NO", "common");

        ResultSet all_rs_resource = null;
        if (tt == null) {
            all_rs_resource = getCategoryFindList(startRow_rs_resource, "YES");
            tt = String.valueOf(findResultSetRows(all_rs_resource));
        }
        int totalPages_rs_resource = Math.round(Integer.parseInt(tt) / maxRows_rs_viewcat);

        String queryString_rs_resource = "";
        List<String> newParamterList = new LinkedList<String>();
        if (null != serverParameterList) {
            for (String param : serverParameters) {
                if (!param.startsWith("pg=") && !param.startsWith("tt=")) {
                    newParamterList.add(param);
                }
            }
        }

        if (!newParamterList.isEmpty()) {
            queryString_rs_resource = "&" + StringFormatter.formatListToString(newParamterList, "&");
        }

        queryString_rs_resource = String.format("&tt=%d%s", Integer.parseInt(tt), queryString_rs_resource);

        String html = "";
        if (Integer.parseInt(tt) > 0) {
            html += "<form id=\"resource_view\" name=\"resource_view\" enctype=\"multipart/form-data\" method=\"post\" action=\"\">"
                    + "<table border=\"0\">"
                    + "<tr>";
            html += "<td>";
            if (Integer.parseInt(pg) > 0) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, 0, queryString_rs_resource) + "\">First</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) > 0) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.max(0, Integer.parseInt(pg) - 1), queryString_rs_resource) + "\">Previous</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) < totalPages_rs_resource) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, Math.min(totalPages_rs_resource, Integer.parseInt(pg) + 1), queryString_rs_resource) + "\">Next</a>";
            }
            html += "</td><td>";
            if (Integer.parseInt(pg) < totalPages_rs_resource) {
                html += "<a href=\"" + String.format("%s?pg=%d%s", currentPage, totalPages_rs_resource, queryString_rs_resource) + "\">Last</a>";
            }
            html += "</td><td>";
            html += "Records " + (startRow_rs_resource + 1) + " to " + Math.min(startRow_rs_resource + maxRows_rs_viewcat, Integer.parseInt(tt)) + " of " + tt + "</td>";

            html += "<td><label><input type=\"submit\" name=\"button\" id=\"button\" value=\"Approve\" onclick=\"document.getElementById('frmaction').value='approve'\" /></label>";
            html += "<input type=\"submit\" name=\"button2\" id=\"button2\" value=\"Delete\" onclick=\"document.getElementById('frmaction').value='delete'; return confirm('Do you really want to delete this Item as it might delete other sub Item');\" />";
            html += "<input type=\"hidden\" name=\"frmaction\" id=\"frmaction\" />";
            html += "<input name=\"frmCont\" type=\"hidden\" id=\"frmCont\" value=\"resource\" />";
            html += "<input name=\"chkprefix\" type=\"hidden\" id=\"chkprefix\" value=\"delchk\" /></td>";
            html += "</tr></table>";

            html += "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"datatable\">";
            html += "<tr>";
            html += "<th align=\"left\">ID</th>";
            html += "<th align=\"left\">Category</th>";
            html += "<th align=\"left\">Location </th>";
            html += "<th align=\"left\">Description </th>";
            html += "<th valign=\"middle\">Action";
            html += "<input type=\"checkbox\" name=\"delchk_0\" id=\"delchk_0\" onchange=\"checkUncheck('resource_view','delchk')\" /></th></tr>";
            //html += "<tbody>";

            while (rs_resource.next()) {
                html += "<tr>";
                html += "<td>" + rs_resource.getString("resourceid") + "</td>";
                html += "<td><a href=\"pages/resource_edit.xhtml?itemid=" + rs_resource.getString("resourceid") + "\" rel=\"facebox\">" + rs_resource.getString("category_name") + "</a></td>";
                html += "<td><a href=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resource.getString("resourceurl") + "\" rel=\"facebox\">" + rs_resource.getString("resourceurl") + "</td>";

                if (7 == rs_resource.getInt("category_id")) {
                    html += "<img name=\"\" src=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resource.getString("resourceurl") + "\" alt=\"\" />";
                }
                html += "</a></td>";

                html += "<td>" + rs_resource.getString("resourcedesc") + "</td>";
                html += "<td valign=\"middle\"> <input type=\"checkbox\" name=\"delchk_" + rs_resource.getString("resourceid") + "\" id=\"delchk_" + rs_resource.getString("resourceid") + "\" value=\"" + rs_resource.getString("resourceid") + "\" /> </td>";
                html += "</tr>";
            }
            html += "</table></form>";

        }

        return html;

    }

    public String resourcePageAdd() {

        return "";
    }

    public String resourceEdit() throws SQLException {
        initNonIndexPage();
        int colname_rs_resource = -1;
        if (CommonUtils.isset(itemid)) {
            colname_rs_resource = Integer.parseInt(itemid);
        }

        ResultSet rs_resource = getResourceDetails(colname_rs_resource);
        int totalRows_rs_resource = findResultSetRows(rs_resource);

        String html = "";

        html += "<form id=\"form1\" name=\"form1\"  method=\"post\" action=\"\">"
                + "</form>"
                + "<form action=\"\" method=\"post\" enctype=\"multipart/form-data\" name=\"form2\" id=\"form2\">"
                + "<table align=\"center\">"
                + "<tr valign=\"baseline\">"
                + "<td width=\"148\" align=\"right\" nowrap=\"nowrap\">Resource ID:</td>"
                + "<td width=\"441\">" + rs_resource.getString("resourceid") + "</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Category:</td>"
                + "<td>" + rs_resource.getString("category_name") + "</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Resourceurl:</td>"
                + "<td>" + (7 == rs_resource.getInt("category") ? "<img name=\"\" src=\"" + ApplicationConstant.RESOURCES.substring(2) + rs_resource.getString("resourceurl") + "\" alt=\"\" style='width:50px;height:30px' />" : "")
                + rs_resource.getString("resourceurl") + "</td>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Description:</td>"
                + "<td><textarea name=\"resourcedesc\" cols=\"32\">" + StringEscapeUtils.escapeHtml(rs_resource.getString("resourcedesc")) + "</textarea>"
                + "A description of the (Image, Audio or resource)</td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Show:</td>"
                + "<td><label>"
                + "<select name=\"ispublished\" id=\"ispublished\">"
                + "<option value=1>Yes</option>"
                + "<option value=0>No</option>"
                + "</select>"
                + "(yes would make it visible for all the users)</label></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Source:</td>"
                + " <td><input type=\"text\" name=\"source\" value=\"" + StringEscapeUtils.escapeHtml(rs_resource.getString("source")) + "\" size=\"32\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">Other Attribute:</td>"
                + " <td><input type=\"text\" name=\"otherattri\" value=\"" + StringEscapeUtils.escapeHtml(rs_resource.getString("otherattri")) + "\" size=\"32\" /></td></tr>"
                + "<tr valign=\"baseline\">"
                + "<td nowrap=\"nowrap\" align=\"right\">&nbsp;</td>"
                + "<td><input type=\"submit\" value=\"Update record\" /></td></tr>"
                + "</table>"
                + "<input type=\"hidden\" name=\"MM_update\" value=\"updateResourceFrm\" />"
                + "<input type=\"hidden\" name=\"resourceid\" value=\"" + rs_resource.getString("resourceid") + "\" />"
                + "</form>"
                + "<p>&nbsp;</p>";

        return html;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Other Utilities">
    private String similarContent(ResultSet rs_resources) throws SQLException {
        int pageNum_rs = 0;
        int maxRows_rs_sim = 50;
        if (CommonUtils.isset(pageNum_rs_sim)) {
            pageNum_rs = Integer.parseInt(pageNum_rs_sim);
        }

        int startRow_rs_sim = pageNum_rs * maxRows_rs_sim;
        int totalRows_rs = 0;
        ResultSet rs_sim = getContentByCat(Integer.parseInt(cid), startRow_rs_sim, "NO");
        ResultSet all_rs_sim = null;
        if (CommonUtils.isset(totalRows_rs_sim)) {
            totalRows_rs = Integer.parseInt(totalRows_rs_sim);
        } else {
            all_rs_sim = getContentByCat(Integer.parseInt(cid), startRow_rs_sim, "NO");
            totalRows_rs = findResultSetRows(all_rs_sim);
        }

        int totalPages_rs_sim = Math.round(totalRows_rs / maxRows_rs_sim) - 1;

        String html = "";
        html += "Members of the category " + rs_resources.getString("ptitle")
                + "<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" id=\"datatable\">"
                + "<tr>"
                + "<th>Page ID</th>"
                + "<th>Title</th>"
                + "<th>Date</th>"
                + "<th>Show</th>"
                + "</tr>";

        while (rs_sim.next()) {
            html += "<tr>"
                    + "<td>" + rs_sim.getString("content_id") + "</td>"
                    + "<td><a href=\"?page=content&amp;itemid=" + rs_sim.getString("content_id") + "\">" + rs_sim.getString("ptitle") + "</a></td>"
                    + "<td>" + rs_sim.getString("datecreated") + "</td>"
                    + "<td>" + rs_sim.getString("ispublished") + "</td>"
                    + "</tr>";
        }

        html += "</table>";

        return html;

    }

    private String ajaxincludes() {
        String f = "facebox1";
        String whtml = ""
                + "<script src=\"../../js/facefiles/jquery-1.2.2.pack.js\" type=\"text/javascript\"></script>"
                + "<link href=\"../../js/facefiles/facebox.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />"
                + "<script src=\"../../js/facefiles/facebox.js\" type=\"text/javascript\"></script>"
                + "<script type=\"text/javascript\">"
                + "jQuery(document).ready(function($) {"
                + "$('a[rel*=facebox1]').facebox() "
                + "})"
                + "</script>";
        return whtml;
    }

    private String addimages() {
        String ahtml = ""
                + "<script language=\"JavaScript\" type=\"text/javascript\">"
                + "function addUploadFiles(){"
                + "var i =  document.getElementById(\"uploadnum\").value;"
                + "i =i*1;"
                + "i=i+1;"
                + "var  newRowIndex = i + 1;"
                + "document.getElementById(\"uploadnum\").value = i;"
                + "var x=document.getElementById('imagesTabAdd').insertRow(newRowIndex);"
                + "var y=x.insertCell(0);"
                + "var z=x.insertCell(1);"
                + "var a=x.insertCell(2);"
                + "y.innerHTML=i;"
                + "z.innerHTML=\"<input type='file' name='file\"+i+\"' id='file\"+i+\"' /></td>\";"
                + "a.innerHTML=\"<input type='text' name='pdesc\"+i+\"' id='pdesc\"+i+\"' />\";"
                + "}</script>"
                + "<table width=\"100%\" border=\"0\" cellpadding=\"4\" cellspacing=\"0\" id=\"imagesTabAdd\">"
                + "<tr>"
                + "<th>Upload Directory</th>"
                + "<th align=\"left\" colspan=\"2\"><input type=\"text\" name=\"dirname\" /></th>"
                + "</tr><tr>"
                + "<th>&nbsp;</th>"
                + "<th align=\"left\">File</th>"
                + "<th>Description</th>"
                + "</tr>";

        for (int i = 1; i <= 4; i++) {
            ahtml += "<tr id=\"id_" + i + "\">"
                    + "<td>" + i + "</td>"
                    + "<td><input type=\"file\" name=\"file" + i + "\" id=\"file" + i + "\" /></td>"
                    + "<td><input type=\"text\" name=\"pdesc" + i + "\" id=\"pdesc" + i + "\" /></td>"
                    + "</tr>";
        }

        ahtml += "<input type=\"hidden\" value=\"4\" name=\"uploadnum\" id=\"uploadnum\" />"
                + "<div id=\"moreuploads\"></div>"
                + "</table>"
                + "<a href=\"#\" onclick=\"addUploadFiles();\">Add more image space</a>";

        return ahtml;
    }
    String f = "facebox";

    public String imagesSearch() throws SQLException {
        initNonIndexPage();
        ResultSet rs_resources = searchResources(q);

        String bhtml = "";
        int totalRows_rs_resources = findResultSetRows(rs_resources);
        if (totalRows_rs_resources > 0) {
            bhtml += "<div class=\"resource_max\">"
                    + "<table width=\"100%\" border=\"0\" cellpadding=\"4\" cellspacing=\"0\" id=\"datatable\">"
                    + "<tr>"
                    + "<th>resourceid</th>"
                    + "<th>category</th>"
                    + "<th><label>"
                    + "<input type=\"checkbox\" name=\"delchk123_0\" id=\"delchk123_0\">"
                    + "</label></th></tr>";

            do {
                bhtml += "<tr>"
                        + "<td><span style='float:left'>" + rs_resources.getString("resource_id") + "</span>";
                if (7 == rs_resources.getInt("category_id")) {
                    bhtml += "<a rel=\"" + f + "\" href=\"../resources/" + rs_resources.getString("resource_url") + "\" >"
                            + "<img name=\"\" src=\"../resources/thump.xhtml?im=" + rs_resources.getString("resource_url") + "\" alt=\"\" /> </a>";
                }
                bhtml += "</td><td>" + rs_resources.getString("category_name") + " - " + rs_resources.getString("resource_desc") + "</td>"
                        + "<td><input type=\"checkbox\" name=\"delchk123_" + rs_resources.getString("resource_id") + "\" id=\"delchk123_" + rs_resources.getString("resource_id") + "\" value=\"" + rs_resources.getString("resource_id") + "\"></td></td>";
            } while (rs_resources.next());
            bhtml += "</table></div>";
        } else {
            bhtml += "<div id=\"error\">NO match found for <strong>" + q + "</strong></div>";
        }

        return bhtml;
    }

    public String createThumb() {
        return "";//CommonUtils.createThumb("", 0);
    }

    private String imageexist() {
        String shtml = ""
                + "<input type=\"text\" name=\"imge\" id=\"imge\" onKeyUp=\"getData('pages/imagessearch.xhtml?aj&q='+this.value,'imageexist')\" />"
                + "<div id=\"imageexist\">"
                + "</div>";
        return shtml;
    }

    private String images() {
        String shtml = "";
        shtml += "<ul id=\"flowertabs\" class=\"shadetabs\">"
                + "<li><a href=\"#\" rel=\"tcontent1\" class=\"selected\">Upload Resource </a></li>"
                + "<li><a href=\"#\" rel=\"tcontent2\">Search for Resource</a></li>"
                + "</ul>"
                + "<div  class=\"allround\" style=\"padding:5px;\">"
                + "<div id=\"tcontent1\" class=\"tabcontent\">"
                + addimages()
                + "</div>"
                + "<div id=\"tcontent2\" class=\"tabcontent\">"
                + imageexist()
                + "</div></div>"
                + "<script type=\"text/javascript\">"
                + "var myflowers=new ddtabcontent(\"flowertabs\");"
                + "myflowers.setpersist(true);"
                + "myflowers.setselectedClassTarget(\"link\");"
                + "myflowers.init();"
                + "</script>"
                + "<input type=\"hidden\" name=\"addimgs\" id=\"addimgs\">";
        return shtml;
    }

    private String relatedpages(ResultSet rs) throws SQLException {
        String shtml = "";
        ResultSet rt = getRelatedPages(rs.getInt("content_id"));
        int num_of_row = findResultSetRows(rs);
        shtml += "<strong class='redtxt'>" + num_of_row + " Related Pages</strong>";
        if (num_of_row > 0) {
            shtml += "<div id=\"pg_action\"></div>"
                    + "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\" style=\"margin:5px\" id=\"datatable\" class=\"allround\">";
            while (rt.next()) {
                int p = rt.getInt("primary_page");
                if (p == rs.getInt("content_id")) {
                    p = rt.getInt("secondary_page");
                }
                ResultSet rdata = getContentDetails(p);
                shtml += "<tr><td><a href=\"?page=content&itemid=" + p + "\">1" + rdata.getString("ptitle") + "</a></td>"
                        + "<td><input type=\"button\" name=\"button\" id=\"del" + p + "\" value=\"remove page\" class=\"btn\" onclick=\"processResourceDeleteClick('del" + p + "'," + rt.getInt("secondary_page") + "," + rt.getInt("primary_page") + ",'rp','pg_action'); \">"
                        + "</td></tr>";
            }
            shtml += "</table>";
        }

        return shtml;
    }

    private String page_comments() throws SQLException {
        int offset = 0;
        ResultSet commentList = getUserCommentList(Integer.parseInt(itemid), offset, "no");

        ResultSet rs = getUserCommentList(Integer.parseInt(itemid), offset, "no");
        int cTotal = 0;
        if (rs.next()) {
            cTotal = rs.getInt("cnt");
        }

        String shtml = "";
        shtml += "Total Comments <strong class=\"bold redtxt\">" + cTotal + "</strong>"
                + "<div id=\"cmessage\"></div>";

        if (cTotal > 0) {
            shtml += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"datatable\">"
                    + "<tr><th width=\"3%\">&nbsp;</th>"
                    + "<th width=\"90%\" align=\"left\">Comment</th>"
                    + "<th width=\"7%\">&nbsp;</th>"
                    + "</tr>";

            while (commentList.next()) {
                shtml += "<tr><td><span class=\"dates\">" + commentList.getString("user_comment_id") + "</span></td>"
                        + "<td><a href=\"pages/comment_details.xhtml?itemid=" + commentList.getString("user_comment_id") + "&aj\" rel=\"facebox\">" + commentList.getString("user_comment_message")
                        + "<span class=\"dates\">" + commentList.getString("comment_date").substring(0, 10) + "</span></a></td>"
                        + "<td><input type=\"button\" name=\"Button\" value=\"Del\" id=\"comDel" + commentList.getString("user_comment_id") + "\" onclick=\"processResourceDeleteClick('comDel" + commentList.getString("user_comment_id") + "','" + commentList.getString("user_comment_id") + "','" + commentList.getString("user_comment_id") + "','com','cmessage')\" />"
                        + "</td></tr>";
            }
            shtml += "</table></div>";
        }

        return shtml;
    }

    private String subpages(ResultSet rs_resource) throws SQLException {
        String shtml = "";
        shtml += "<div class=\"data_list\">";

        int maxRows_rs_sim = 50;
        int pageNum_rs_sims = 0;

        if (CommonUtils.isset(pageNum_rs_sim)) {
            pageNum_rs_sims = Integer.parseInt(pageNum_rs_sim);
        }
        int startRow_rs_sim = pageNum_rs_sims * maxRows_rs_sim;
        ResultSet rs = getContentByCat(Integer.parseInt(itemid), startRow_rs_sim, "yes");
        int totals = 0;
        if (rs.next()) {
            totals = rs.getInt("pg");
        }

        ResultSet rs_sim = getContentByCat(Integer.parseInt(itemid), startRow_rs_sim, "no");
        //        if (rs_sim.next()){} //Fetch Rows

        shtml += "Sub Pages <strong class=\"bold redtxt\">" + totals + "</strong>"
                + " Members of the category " + "rs_resource.getString(\"category_name\")"
                + "<table border=\"0\" cellpadding=\"4\" cellspacing=\"0\" id=\"datatable\" width=\"100%\">"
                + "<tr><th>Page ID</th>"
                + "<th>Title</th>"
                + "<th>Date</th>"
                + "<th>Show</th></tr>";

        while (rs_sim.next()) {
            shtml += "<tr><td>" + rs_sim.getString("content_id") + "</td>"
                    + "<td><a href=\"?page=content&amp;itemid=" + rs_sim.getString("content_id") + "\">" + rs_sim.getString("ptitle") + "</a></td>"
                    + "<td>" + rs_sim.getString("ispublished") + "</td>"
                    + "</tr>";
        }
        shtml += "</table></div>";
        return shtml;
    }
    //</editor-fold>

    /**
     * @return the p_offset
     */
    public int getP_offset() {
        return p_offset;
    }

    /**
     * @param p_offset the p_offset to set
     */
    public void setP_offset(int p_offset) {
        this.p_offset = p_offset;
    }

    /**
     * @return the c_offset
     */
    public int getC_offset() {
        return c_offset;

    }

    /**
     * @param c_offset the c_offset to set
     */
    public void setC_offset(int c_offset) {
        this.c_offset = c_offset;
    }

    /**
     * @return the r_offset
     */
    public int getR_offset() {

        return r_offset;
    }

    /**
     * @param r_offset the r_offset to set
     */
    public void setR_offset(int r_offset) {
        this.r_offset = r_offset;
    }

    public String getHereAreWe() {
        return hereAreWe;
    }

    public void setHereAreWe(String hereAreWe) {
        this.hereAreWe = hereAreWe;
    }

    public Page getPp() {
        return pp;
    }

    public void setPp(Page pp) {
        this.pp = pp;
    }

    public String getMainPageName() {
        return mainPageName;
    }

    public void setMainPageName(String mainPageName) {
        this.mainPageName = mainPageName;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPageFile() {
        return pageFile;
    }

    public void setPageFile(String pageFile) {
        this.pageFile = pageFile;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getAj() {
        return aj;
    }

    public void setAj(String aj) {
        this.aj = aj;
    }

    private String rattrayResourceDetails(int resourceId, boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getPubp() {
        return pubp;
    }

    public void setPubp(int pubp) {
        this.pubp = pubp;
    }

    public int getPube() {
        return pube;
    }

    public void setPube(int pube) {
        this.pube = pube;
    }

    public int getPubq() {
        return pubq;
    }

    public void setPubq(int pubq) {
        this.pubq = pubq;
    }
 
    

}
