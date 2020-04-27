package com.tokmeninov.springboot1.models;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Please fill the title")
    @Length(max = 255, message = "Title too long")
    private String title;
    @NotBlank(message = "Please fill the anons")
    @Length(max = 255, message = "Title too long")
    private String anons;
    @NotBlank(message = "Please fill the main text")
    @Length(max = 2048, message = "Title too long")
    private String full_text;
    private int views;

    @ManyToOne(fetch = FetchType.EAGER)
    private User author;

    private String filename;

    public Post() {
    }

    public Post(String title, String anons, String full_text, User user) {
        this.title=title;
        this.anons=anons;
        this.full_text=full_text;
        this.author=user;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAuthorName(){
        return author!=null ? author.getUsername() : "<none>";
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnons() {
        return anons;
    }

    public void setAnons(String anons) {
        this.anons = anons;
    }

    public String getFull_text() {
        return full_text;
    }

    public void setFull_text(String full_text) {
        this.full_text = full_text;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
