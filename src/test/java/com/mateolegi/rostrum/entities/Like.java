package com.mateolegi.rostrum.entities;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "likes", schema = "social_network", catalog = "")
public class Like {
    private Long id;
    private Timestamp createdAt;
    private Post postsByIdPost;
    private User usersByIdUser;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Like that = (Like) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "id_post", referencedColumnName = "id", nullable = false)
    public Post getPostsByIdPost() {
        return postsByIdPost;
    }

    public void setPostsByIdPost(Post postsByIdPost) {
        this.postsByIdPost = postsByIdPost;
    }

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id", nullable = false)
    public User getUsersByIdUser() {
        return usersByIdUser;
    }

    public void setUsersByIdUser(User usersByIdUser) {
        this.usersByIdUser = usersByIdUser;
    }
}
