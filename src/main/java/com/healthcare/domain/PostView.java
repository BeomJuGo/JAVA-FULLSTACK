package com.healthcare.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name="post_view")
@IdClass(PostView.PK.class)
public class PostView {

    @Id @Column(name="post_id")
    private Long postId;

    @Id @Column(name="account_id")
    private Long accountId;

    @Id @Column(name="view_date")
    private LocalDate viewDate;

    @Column(name="cnt", nullable = false)
    private Integer cnt = 1;

    public static class PK implements Serializable {
        public Long postId; public Long accountId; public LocalDate viewDate;
        public PK() {}
        public PK(Long p, Long a, LocalDate d){ this.postId=p; this.accountId=a; this.viewDate=d; }
        @Override public int hashCode(){ return (postId+"-"+accountId+"-"+viewDate).hashCode(); }
        @Override public boolean equals(Object o){
            if(this==o) return true; if(!(o instanceof PK p)) return false;
            return postId.equals(p.postId) && accountId.equals(p.accountId) && viewDate.equals(p.viewDate);
        }
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public LocalDate getViewDate() { return viewDate; }
    public void setViewDate(LocalDate viewDate) { this.viewDate = viewDate; }
    public Integer getCnt() { return cnt; }
    public void setCnt(Integer cnt) { this.cnt = cnt; }
}
