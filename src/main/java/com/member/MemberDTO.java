package com.member;

public class MemberDTO {
    private String id;
    private String pwd;
    private String user_name;
    private int user_level;

    public MemberDTO() {}
    public MemberDTO(String id, String pwd, String user_name, int user_level) {
        this.id = id;
        this.pwd = pwd;
        this.user_name = user_name;
        this.user_level = user_level;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }

    public int getUser_level() { return user_level; }
    public void setUser_level(int user_level) { this.user_level = user_level; }
    @Override
    public String toString() {
        return user_name; // DTO에서 getUser_name() 값 반환
    }
}
