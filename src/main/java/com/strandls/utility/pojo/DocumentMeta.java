package com.strandls.utility.pojo;

import java.util.List;
import java.util.Map;

public class DocumentMeta {
    private String title;
    private String user;
    private String pic;
    private Long id;

    // Constructors
    public DocumentMeta() {
        super();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    
    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

}