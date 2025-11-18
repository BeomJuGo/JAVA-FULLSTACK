package com.healthcare.domain;

import jakarta.persistence.*;

@Entity
@Table(name="hashtag")
public class Hashtag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tag", nullable = false, unique = true, length = 100)
    private String tag;

    public Long getId() { return id; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
}
