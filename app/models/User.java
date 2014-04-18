package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public String password;
    public String email;
    public String phone;
    public String photo;

    public boolean gender;
    public boolean is_business;

    @Column(name = "created")
    public java.util.Date created;

    public int checkin_count;
    public int follower_count;
    public int friend_count;

    public User() {
    }

    public User(String username, String password, String email) {
        this.name = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, String phone,
            String photo, boolean gender, int checkinCount, int followerCount,
            int friendCount) {
        this.name = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.photo = photo;
        this.gender = gender;
        this.checkin_count = checkinCount;
        this.follower_count = followerCount;
        this.friend_count = friendCount;
    }

    public static Finder<Long, User> find = new Finder<Long, User>(Long.class,
            User.class);

    public static User authenticate(String username, String password) {
        return find.where().eq("name", username).eq("password", password)
                .findUnique();
    }

    public static User checkExistence(String username) {
        return find.where().eq("name", username).findUnique();
    }

    public static List<User> all() {
        return find.all();
    }

    public static void create(User user) {
        user.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
