package com.mateolegi.rostrum.entities;

import com.mateolegi.rostrum.Rostrum;
import com.mateolegi.rostrum.annotation.Crypt;
import com.mateolegi.rostrum.annotation.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "social_network", catalog = "")
public class User {
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "username")
    private String username;
    @Basic
    @Column(name = "password")
    @Crypt(type = Type.ONE_WAY)
    private String password;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "last_name")
    private String lastName;
    @Basic
    @Column(name = "active")
    private Boolean active;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    private Collection<Post> postsById;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        if (Objects.nonNull(id) ? !id.equals(that.id) : Objects.nonNull(that.id)) return false;
        if (Objects.nonNull(username) ? !username.equals(that.username) : Objects.nonNull(that.username)) return false;
        if (Objects.nonNull(password) ? !password.equals(that.password) : Objects.nonNull(that.password)) return false;
        if (Objects.nonNull(name) ? !name.equals(that.name) : Objects.nonNull(that.name)) return false;
        if (Objects.nonNull(lastName) ? !lastName.equals(that.lastName) : Objects.nonNull(that.lastName)) return false;
        if (Objects.nonNull(active) ? !active.equals(that.active) : Objects.nonNull(that.active)) return false;
        if (Objects.nonNull(createdAt) ? !createdAt.equals(that.createdAt) : Objects.nonNull(that.createdAt))
            return false;
        if (Objects.nonNull(updatedAt) ? !updatedAt.equals(that.updatedAt) : Objects.nonNull(that.updatedAt))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "usersByIdUser")
    public Collection<Post> getPostsById() {
        return postsById;
    }

    public void setPostsById(Collection<Post> postsById) {
        this.postsById = postsById;
    }
}
