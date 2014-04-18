package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Business extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Column(name = "created")
    public java.util.Date created;

    public Long user_id;
    public Long loc_id;

    public int checkin_count;

    public Business() {
    }

    public Business(String name, long userId, long locId) {
        this.name = name;
        this.user_id = userId;
        this.loc_id = locId;
    }

    public static Finder<Long, Business> find = new Finder<Long, Business>(
            Long.class, Business.class);

    public static void create(Business business) {
        business.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static long findMyLocId(long userId) {
        // TODO: user can have multiple location
        Business b = find.where().eq("user_id", userId).findUnique();
        return b.loc_id;
    }
}
